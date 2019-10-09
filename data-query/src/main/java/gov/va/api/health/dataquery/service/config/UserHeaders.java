package gov.va.api.health.dataquery.service.config;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.argonaut.api.resources.Patient.Bundle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@ControllerAdvice
public class UserHeaders implements ResponseBodyAdvice<Object> {

  @Override
  public Object beforeBodyWrite(
      Object o,
      MethodParameter methodParameter,
      MediaType mediaType,
      Class<? extends HttpMessageConverter<?>> thisClass,
      ServerHttpRequest serverHttpRequest,
      ServerHttpResponse serverHttpResponse) {
    log.info("HEY WE GOT HERE");
    return o;
  }

  @Override
  public boolean supports(
      MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> thisClass) {
    return Bundle.class.equals(methodParameter.getParameterType())
        || Patient.class.equals(methodParameter.getParameterType());
  }
}
