package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public class CountParameterResolverTest {

  CountParameterResolver r = CountParameterResolver.builder().defaultCount(10).maxCount(20).build();

  @Mock MethodParameter methodParameter;
  @Mock NativeWebRequest nativeWebRequest;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
  }

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

  public void reflectMe(@CountParameter Void here) {}

  @Test(expected = MethodArgumentTypeMismatchException.class)
  public void throwsExceptionWhenCountCannotBeParsed() {
    useCount("nope");
    r.resolveArgument(methodParameter, null, nativeWebRequest, null);
  }

  private void useCount(String count) {
    when(nativeWebRequest.getParameter("_count")).thenReturn(count);
  }

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
