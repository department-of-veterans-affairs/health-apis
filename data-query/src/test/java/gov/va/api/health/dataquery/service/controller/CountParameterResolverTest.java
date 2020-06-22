package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public class CountParameterResolverTest {

  CountParameterResolver r = CountParameterResolver.builder().defaultCount(10).maxCount(20).build();

  MethodParameter methodParameter = mock(MethodParameter.class);
  NativeWebRequest nativeWebRequest = mock(NativeWebRequest.class);

  @Test
  public void appliesToMethodWithCountParametersInt() {
    useParameter(countParameter(), int.class);
    assertThat(r.supportsParameter(methodParameter)).isTrue();
  }

  @Test
  public void clampsWhenCountSpecifiedTooHigh() {
    useCount("999");
    assertThat(r.resolveArgument(methodParameter, null, nativeWebRequest, null)).isEqualTo(20);
  }

  @SneakyThrows
  private CountParameter countParameter() {
    return (CountParameter)
        getClass().getMethod("reflectMe", Void.class).getParameterAnnotations()[0][0];
  }

  @Test
  public void defaultsWhenCountNotSpecified() {
    useCount(null);
    assertThat(r.resolveArgument(methodParameter, null, nativeWebRequest, null)).isEqualTo(10);
  }

  @Test
  public void doesNotApplyToMethodNotAnInt() {
    useParameter(countParameter(), Integer.class);
    assertThat(r.supportsParameter(methodParameter)).isFalse();
  }

  @Test
  public void doesNotApplyToMethodWithoutCountParameters() {
    useParameter(null, int.class);
    assertThat(r.supportsParameter(methodParameter)).isFalse();
  }

  @SuppressWarnings("unused")
  public void reflectMe(@CountParameter Void here) {}

  @Test
  public void throwsExceptionWhenCountCannotBeParsed() {
    useCount("nope");
    Assertions.assertThrows(
        MethodArgumentTypeMismatchException.class,
        () -> r.resolveArgument(methodParameter, null, nativeWebRequest, null));
  }

  private void useCount(String count) {
    when(nativeWebRequest.getParameter("_count")).thenReturn(count);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void useParameter(CountParameter annotation, Class type) {
    when(methodParameter.getParameterType()).thenReturn(type);
    when(methodParameter.getParameterAnnotation(CountParameter.class)).thenReturn(annotation);
  }

  @Test
  public void usesCountWhenSpecifiedWithinRange() {
    useCount("7");
    assertThat(r.resolveArgument(methodParameter, null, nativeWebRequest, null)).isEqualTo(7);
  }
}
