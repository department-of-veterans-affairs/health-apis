package gov.va.api.health.dataquery.service.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.BadSearchParameter;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dstu2.api.elements.Narrative;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.ids.client.IdEncoder.BadId;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Exceptions that escape the rest controllers will be processed by this handler. It will convert
 * exception into different HTTP status codes and produce an error response payload.
 */
@Slf4j
@RestControllerAdvice
@RequestMapping(produces = {"application/json"})
public class WebExceptionHandler {
  @ExceptionHandler({
    BindException.class,
    MrAndersonClient.BadRequest.class,
    ResourceExceptions.MissingSearchParameters.class,
    UnsatisfiedServletRequestParameterException.class,
    BadSearchParameter.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public OperationOutcome handleBadRequest(Exception e, HttpServletRequest request) {
    return responseFor("structure", e, request);
  }

  @ExceptionHandler({Transformers.MissingPayload.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public OperationOutcome handleMissingPayload(Exception e, HttpServletRequest request) {
    request.getParameter("_id");
    return responseFor("not-found", e, request);
  }

  @ExceptionHandler({
    HttpClientErrorException.NotFound.class,
    MrAndersonClient.NotFound.class,
    ResourceExceptions.NotFound.class,
    ResourceExceptions.UnknownIdentityInSearchParameter.class,
    ResourceExceptions.UnknownResource.class,
    BadId.class
  })
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public OperationOutcome handleNotFound(Exception e, HttpServletRequest request) {
    return responseFor("not-found", e, request);
  }

  /**
   * For exceptions relating to unmarshalling json, we want to make sure no PII is being logged.
   * Therefore, when we encounter these exceptions, we will not print the stacktrace in an effort to
   * decrease the likelihood PII shows up in our logs.
   */
  @ExceptionHandler({Exception.class, UndeclaredThrowableException.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public OperationOutcome handleSnafu(Exception e, HttpServletRequest request) {
    if (e.getCause() != null
        && e.getCause().getClass().isAssignableFrom(InvalidFormatException.class)) {
      String requestPath =
          request.getRequestURI()
              + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
      log.error("Requested Resource: {}", requestPath.replaceAll("[\r\n]", ""));
      return responseFor("database", e, request, false);
    }
    return responseFor("exception", e, request);
  }

  /**
   * For constraint violation exceptions, we want to add a little more information in the exception
   * to present what exactly is wrong. We will distill which properties are wrong and why, but we
   * will not leak any values.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public OperationOutcome handleValidationException(
      ConstraintViolationException e, HttpServletRequest request) {
    List<String> problems =
        e.getConstraintViolations()
            .stream()
            .map(v -> v.getPropertyPath() + " " + v.getMessage())
            .collect(Collectors.toList());

    return responseFor("structure", e, request, problems, true);
  }

  private OperationOutcome responseFor(String code, Exception e, HttpServletRequest request) {
    return responseFor(code, e, request, Collections.emptyList(), true);
  }

  private OperationOutcome responseFor(
      String code, Exception e, HttpServletRequest request, Boolean printException) {
    return responseFor(code, e, request, Collections.emptyList(), printException);
  }

  private OperationOutcome responseFor(
      String code,
      Exception e,
      HttpServletRequest request,
      List<String> problems,
      Boolean printException) {
    StringBuilder diagnostics = new StringBuilder();
    diagnostics
        .append("Error: ")
        .append(e.getClass().getSimpleName())
        .append(" Timestamp:")
        .append(Instant.now());
    problems.forEach(p -> diagnostics.append('\n').append(p));

    OperationOutcome response =
        OperationOutcome.builder()
            .id(UUID.randomUUID().toString())
            .resourceType("OperationOutcome")
            .text(
                Narrative.builder()
                    .status(Narrative.NarrativeStatus.additional)
                    .div("<div>Failure: " + request.getRequestURI() + "</div>")
                    .build())
            .issue(
                Collections.singletonList(
                    OperationOutcome.Issue.builder()
                        .severity(OperationOutcome.Issue.IssueSeverity.fatal)
                        .code(code)
                        .diagnostics(diagnostics.toString())
                        .build()))
            .build();
    if (!printException) {
      log.error("Response {}", response);
    } else {
      log.error("Response {}", response, e);
    }
    return response;
  }
}
