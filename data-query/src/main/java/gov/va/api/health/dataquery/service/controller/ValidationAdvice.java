package gov.va.api.health.dataquery.service.controller;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@ControllerAdvice
public final class ValidationAdvice implements ResponseBodyAdvice<Object> {
  @Override
  public Object beforeBodyWrite(
      Object payload,
      MethodParameter method,
      MediaType unused1,
      Class<? extends HttpMessageConverter<?>> unused2,
      ServerHttpRequest request,
      ServerHttpResponse unused3) {
    Set<ConstraintViolation<Object>> violations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(payload);
    if (violations.isEmpty()) {
      return payload;
    }

    StringBuilder sb = new StringBuilder().append(method.getExecutable().getName());

    if (request instanceof ServletServerHttpRequest) {
      ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
      sb.append("(")
          .append(
              servletRequest.getServletRequest().getParameterMap().entrySet().stream()
                  .map(e -> e.getKey() + "=" + Arrays.toString(e.getValue()))
                  .collect(Collectors.joining(",")))
          .append(")");
    }

    sb.append(" response ")
        .append(payload.getClass().getName())
        .append(" failed validation: ")
        .append(
            violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", ")));

    log.warn(sb.toString());

    return payload;
  }

  @Override
  public boolean supports(
      MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> unused) {
    return true;
  }
}
