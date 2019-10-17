package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.resources.Resource;
import java.security.InvalidParameterException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public abstract class AbstractIncludesIcnMajig<
        T extends Resource, E extends AbstractEntry<T>, B extends AbstractBundle<E>>
    implements ResponseBodyAdvice<Object> {

  private final Class<T> type;
  private final Class<B> bundleType;
  private final Function<T, Stream<String>> extractIcns;

  public static void addHeader(ServerHttpResponse serverHttpResponse, String usersCsv) {
    serverHttpResponse.getHeaders().add("X-VA-INCLUDES-ICN", usersCsv);
  }

  @SuppressWarnings("unchecked")
  @Override
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

    String users = "";
    if (type.isInstance(payload)) {
      users = extractIcns.apply((T) payload).collect(Collectors.joining());
    } else if (bundleType.isInstance(payload)) {
      users =
          ((B) payload)
              .entry().stream()
                  .map(AbstractEntry::resource)
                  .flatMap(resource -> extractIcns.apply(resource))
                  .distinct()
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
}
