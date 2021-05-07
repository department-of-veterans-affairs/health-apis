package gov.va.api.health.dataquery.service.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * This class contains the logic for implementing, on a per-resource basis, a ResponseBodyAdvice as
 * an @ControllerAdvice.
 *
 * <p>The @ControllerAdvice's intercept all responses from Controller @RequestMappings. The advice
 * then checks the return type of the @RequestMapping's payload. If it is "supported", (see the
 * supports() method), then beforeBodyWrite() logic fires. It will search the payload using a
 * supplied ICN extraction function. We then populate an internal header of X-VA-INCLUDES-ICN with
 * the corresponding ICN(s) in the payload. This header will be used by Kong to do Authorization via
 * Patient Matching.
 */
@Builder
public final class IncludesIcnMajig<T, B> implements ResponseBodyAdvice<Object> {
  public static final String INCLUDES_ICN_HEADER = "X-VA-INCLUDES-ICN";

  private final Class<T> type;

  private final Class<B> bundleType;

  private final Function<B, Stream<T>> extractResources;

  private final Function<T, Stream<String>> extractIcns;

  /** Add the X-VA-INCLUDES-ICN header if it does not already exist. */
  public static void addHeader(@NonNull ServerHttpResponse serverHttpResponse, String usersCsv) {
    List<String> includesIcnHeaders = serverHttpResponse.getHeaders().get(INCLUDES_ICN_HEADER);
    if (includesIcnHeaders == null || includesIcnHeaders.isEmpty()) {
      serverHttpResponse.getHeaders().add(INCLUDES_ICN_HEADER, usersCsv);
    }
  }

  /** Add the X-VA-INCLUDES-ICN header if it does not already exist. */
  public static void addHeader(@NonNull HttpServletResponse serverHttpResponse, String usersCsv) {
    if (StringUtils.isBlank(serverHttpResponse.getHeader(INCLUDES_ICN_HEADER))) {
      serverHttpResponse.addHeader(INCLUDES_ICN_HEADER, usersCsv);
    }
  }

  public static void addHeaderForNoPatients(@NonNull HttpServletResponse serverHttpResponse) {
    addHeader(serverHttpResponse, "NONE");
  }

  public static String encodeHeaderValue(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object beforeBodyWrite(
      Object payload,
      MethodParameter unused1,
      MediaType unused2,
      Class<? extends HttpMessageConverter<?>> unused3,
      ServerHttpRequest unused4,
      ServerHttpResponse serverHttpResponse) {
    // In the case where extractIcns is null, let Kong deal with it
    if (extractIcns == null) {
      return payload;
    }
    String users;
    Pattern patientIcnRegex = Pattern.compile("^([0-9V]+)$");
    if (type.isInstance(payload)) {
      users =
          extractIcns
              .apply((T) payload)
              .distinct()
              .peek(icn -> verifyPatientIcn(patientIcnRegex, icn))
              .collect(Collectors.joining(","));
    } else if (bundleType.isInstance(payload)) {
      users =
          extractResources
              .apply((B) payload)
              .flatMap(extractIcns)
              .distinct()
              .peek(icn -> verifyPatientIcn(patientIcnRegex, icn))
              .collect(Collectors.joining(","));
    } else {
      throw new InvalidParameterException("Payload type does not match ControllerAdvice type.");
    }
    if (users.isBlank()) {
      users = "NONE";
    }
    addHeader(serverHttpResponse, users);
    return payload;
  }

  @Override
  public boolean supports(
      MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> unused) {
    return type.equals(methodParameter.getParameterType())
        || bundleType.equals(methodParameter.getParameterType());
  }

  private void verifyPatientIcn(Pattern patientIcnPattern, String maybeIcn) {
    if (!patientIcnPattern.matcher(maybeIcn).matches()) {
      throw new IllegalArgumentException(
          "ICN was invalid and could not be added to " + INCLUDES_ICN_HEADER);
    }
  }
}
