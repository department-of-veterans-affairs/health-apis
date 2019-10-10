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

@AllArgsConstructor
public abstract class AbstractIncludesIcnMajig<
        T extends Resource, E extends AbstractEntry<T>, B extends AbstractBundle<E>>
    implements ResponseBodyAdvice<Object> {

  private Class<T> type;
  private Class<B> bundleType;
  private Function<T, Stream<String>> extractIcns;

  @SuppressWarnings("unchecked")
  @Override
  public Object beforeBodyWrite(
      Object o,
      MethodParameter methodParameter,
      MediaType mediaType,
      Class<? extends HttpMessageConverter<?>> thisClass,
      ServerHttpRequest serverHttpRequest,
      ServerHttpResponse serverHttpResponse) {

    String users = "";
    if (type.isInstance(o)) {
      users = extractIcns.apply((T) o).collect(Collectors.joining());
    } else if (bundleType.isInstance(o)) {
      users =
          ((B) o)
              .entry().stream()
                  .map(AbstractEntry::resource)
                  .flatMap(resource -> extractIcns.apply(resource))
                  .collect(Collectors.joining(","));
    } else {
      throw new InvalidParameterException("Payload type does not match ControllerAdvice type.");
    }
    serverHttpResponse.getHeaders().add("X-VA-RECORDS-FOR-USERS", users);
    return o;
  }

  @Override
  public boolean supports(
      MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> thisClass) {
    return type.equals(methodParameter.getParameterType())
        || bundleType.equals(methodParameter.getParameterType());
  }
}
