package gov.va.api.health.dataquery.service.controller;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.MethodExecutionLogger;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Narrative;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.ids.client.IdEncoder.BadId;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
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
  private static final String CRYPTO_KEY = "yo_dawg_i_herd_u_like_encryption";

  @Autowired MethodExecutionLogger mel;

  private OperationOutcome asOperationOutcome(
      String code, Exception e, HttpServletRequest request, List<String> diagnostics) {
    OperationOutcome.Issue issue =
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

  @SneakyThrows
  private String encrypt(String plainText) {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    Key key = new SecretKeySpec(CRYPTO_KEY.getBytes("UTF-8"), "AES");
    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
    byte[] iv = new byte[cipher.getBlockSize()];
    secureRandom.nextBytes(iv);
    cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
    byte[] enBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
    byte[] combined = ArrayUtils.addAll(iv, enBytes);
    return Base64.getEncoder().encodeToString(combined);
  }

  private List<Extension> extensions(Exception e, HttpServletRequest request) {
    List<Extension> extensions = new ArrayList<>(5);
    extensions.add(
        Extension.builder().url("timestamp").valueInstant(Instant.now().toString()).build());
    extensions.add(
        Extension.builder().url("type").valueString(e.getClass().getSimpleName()).build());
    if (isNotBlank(sanitizedMessage(e))) {
      extensions.add(
          Extension.builder().url("message").valueString(encrypt(sanitizedMessage(e))).build());
    }
    String cause =
        causes(e).stream()
            .map(t -> t.getClass().getSimpleName() + " " + sanitizedMessage(t))
            .collect(Collectors.joining(", "));
    if (isNotBlank(cause)) {
      extensions.add(Extension.builder().url("cause").valueString(encrypt(cause)).build());
    }
    extensions.add(
        Extension.builder().url("request").valueString(encrypt(reconstructUrl(request))).build());
    return extensions;
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
        e.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + " " + v.getMessage())
            .collect(Collectors.toList());
    return responseFor("structure", e, request, problems, true);
  }

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

  private String sanitizedMessage(Throwable throwable) {
    if (throwable instanceof MismatchedInputException) {
      MismatchedInputException mie = (MismatchedInputException) throwable;
      return new StringBuilder().append(" path: ").append(mie.getPathReference()).toString();
    }
    if (throwable instanceof JsonEOFException) {
      JsonEOFException eofe = (JsonEOFException) throwable;
      if (eofe.getLocation() != null) {
        return new StringBuilder()
            .append(" line: ")
            .append(eofe.getLocation().getLineNr())
            .append(", column: ")
            .append(eofe.getLocation().getColumnNr())
            .toString();
      }
    }
    if (throwable instanceof JsonMappingException) {
      JsonMappingException jme = (JsonMappingException) throwable;
      return new StringBuilder().append(" path: ").append(jme.getPathReference()).toString();
    }
    if (throwable instanceof JsonParseException) {
      JsonParseException jpe = (JsonParseException) throwable;
      if (jpe.getLocation() != null) {
        return new StringBuilder()
            .append(" line: ")
            .append(jpe.getLocation().getLineNr())
            .append(", column: ")
            .append(jpe.getLocation().getColumnNr())
            .toString();
      }
    }
    return throwable.getMessage();
  }
}
