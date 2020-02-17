package gov.va.api.health.dataquery.service.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.MethodExecutionLogger;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Narrative;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.OperationOutcome.Issue;
import gov.va.api.health.ids.client.IdEncoder.BadId;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Instant;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.beans.factory.annotation.Autowired;
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
  @Autowired MethodExecutionLogger mel;

  private boolean isJsonError(Exception e) {
    Throwable cause = e.getCause();
    while (cause != null) {
      if (JsonProcessingException.class.isAssignableFrom(cause.getClass())) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }

  private OperationOutcome asOperationOutcome(
      String code, Exception e, HttpServletRequest request, List<String> diagnostics) {
    Issue issue =
        OperationOutcome.Issue.builder()
            .severity(OperationOutcome.Issue.IssueSeverity.fatal)
            .code(code)
            .build();
    String d = diagnostics.stream().collect(Collectors.joining(", "));
    if (isNotBlank(d)) {
      issue.diagnostics(d);
    }

    return OperationOutcome.builder()
        .id(UUID.randomUUID().toString())
        .resourceType("OperationOutcome")
        .extension(extensions(e, request))
        .text(
            Narrative.builder()
                .status(Narrative.NarrativeStatus.additional)
                .div("<div>Failure: " + request.getRequestURI() + "</div>")
                .build())
        .issue(singletonList(issue))
        .build();
  }

  private List<Extension> extensions(Exception e, HttpServletRequest request) {
    List<Extension> extensions = new ArrayList<>(5);

    extensions.add(
        Extension.builder().url("timestamp").valueInstant(Instant.now().toString()).build());

    extensions.add(
        Extension.builder().url("type").valueString(e.getClass().getSimpleName()).build());

    if (isNotBlank(e.getMessage())) {
      extensions.add(Extension.builder().url("message").valueString(e.getMessage()).build());
    }

    String cause =
        causes(e)
            .stream()
            .map(t -> t.getClass().getSimpleName() + " " + t.getMessage())
            .collect(Collectors.joining(", "));
    if (isNotBlank(cause)) {
      extensions.add(Extension.builder().url("cause").valueString(cause).build());
    }

    extensions.add(Extension.builder().url("request").valueString(reconstructUrl(request)).build());

    return extensions;
  }

  private List<Throwable> causes(Throwable t) {
    List<Throwable> results = new ArrayList<>();
    Throwable throwable = t;
    while (true) {
      throwable = throwable.getCause();
      if (throwable == null) {
        return results;
      }
      results.add(throwable);
    }
  }

  @ExceptionHandler({
    BindException.class,
    ResourceExceptions.BadSearchParameter.class,
    ResourceExceptions.MissingSearchParameters.class,
    UnsatisfiedServletRequestParameterException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public OperationOutcome handleBadRequest(Exception e, HttpServletRequest request) {
    return responseFor("structure", e, request, emptyList(), true);
  }

  @ExceptionHandler({
    HttpClientErrorException.NotFound.class,
    ResourceExceptions.NotFound.class,
    ResourceExceptions.UnknownIdentityInSearchParameter.class,
    ResourceExceptions.UnknownResource.class,
    BadId.class
  })
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public OperationOutcome handleNotFound(Exception e, HttpServletRequest request) {
    return responseFor("not-found", e, request, emptyList(), true);
  }

  @ExceptionHandler({ResourceExceptions.NotImplemented.class})
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  public OperationOutcome handleNotImplemented(Exception e, HttpServletRequest request) {
    return responseFor("not-implemented", e, request, emptyList(), true);
  }

  /**
   * For exceptions relating to unmarshalling json, we want to make sure no PII is being logged.
   * Therefore, when we encounter these exceptions, we will not print the stacktrace to prevent PII
   * showing up in our logs.
   */
  @ExceptionHandler({
    Exception.class,
    ResourceExceptions.InvalidDatamartPayload.class,
    UndeclaredThrowableException.class
  })
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public OperationOutcome handleSnafu(Exception e, HttpServletRequest request) {
    if (isJsonError(e)) {
      return responseFor("database", e, request, emptyList(), false);
    }
    return responseFor("exception", e, request, emptyList(), true);
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

  /** Reconstruct a sanitized URL based on the request. */
  private String reconstructUrl(HttpServletRequest request) {
    return request.getRequestURI()
        + (request.getQueryString() == null ? "" : "?" + request.getQueryString())
            .replaceAll("[\r\n]", "");
  }

  @SneakyThrows
  private OperationOutcome responseFor(
      String code,
      Exception e,
      HttpServletRequest request,
      List<String> problems,
      boolean printStackTrace) {
    OperationOutcome response = asOperationOutcome(code, e, request, problems);
    if (printStackTrace) {
      log.error("Response {}", JacksonConfig.createMapper().writeValueAsString(response), e);
    } else {
      log.error("Response {}", JacksonConfig.createMapper().writeValueAsString(response));
    }
    return response;
  }

  String sanitize(JsonProcessingException jsonError) {
    StringBuilder safe = new StringBuilder(jsonError.getClass().getSimpleName());
    if (jsonError instanceof MismatchedInputException) {
      MismatchedInputException mie = (MismatchedInputException) jsonError;
      safe.append(" path: ").append(mie.getPathReference());
    } else if (jsonError instanceof JsonEOFException) {
      JsonEOFException eofe = (JsonEOFException) jsonError;
      if (eofe.getLocation() != null) {
        safe.append(" line: ")
            .append(eofe.getLocation().getLineNr())
            .append(", column: ")
            .append(eofe.getLocation().getColumnNr());
      }
    } else if (jsonError instanceof JsonMappingException) {
      JsonMappingException jme = (JsonMappingException) jsonError;
      safe.append(" path: ").append(jme.getPathReference());
    } else if (jsonError instanceof JsonParseException) {
      JsonParseException jpe = (JsonParseException) jsonError;
      if (jpe.getLocation() != null) {
        safe.append(" line: ")
            .append(jpe.getLocation().getLineNr())
            .append(", column: ")
            .append(jpe.getLocation().getColumnNr());
      }
    }
    return safe.toString();
  }
}
