package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.server.ServletServerHttpRequest;

public class ValidationAdviceTest {
  @Test
  @SneakyThrows
  public void message() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    when(servletRequest.getParameterMap())
        .thenReturn(ImmutableMap.of("v", new String[] {"w", "x"}, "y", new String[] {"z"}));

    ServletServerHttpRequest servletServerRequest = mock(ServletServerHttpRequest.class);
    when(servletServerRequest.getServletRequest()).thenReturn(servletRequest);

    MethodParameter method =
        new MethodParameter(Controller.class.getDeclaredMethod("read", String.class), -1);

    assertThat(ValidationAdvice.buildMessage(new Foo(), method, servletServerRequest))
        .isEqualTo(
            "read(v=[w, x],y=[z]) response gov.va.api.health.dataquery.service.controller.ValidationAdviceTest$Foo failed validation: "
                + "bar must not be null, foo must not be blank, num must be greater than or equal to 1");
  }

  @Test
  public void noViolations() {
    assertThat(ValidationAdvice.buildMessage("x", null, null)).isNull();
  }

  private static final class Controller {
    @SuppressWarnings("unused")
    Foo read(String unused) {
      return new Foo();
    }
  }

  private static final class Foo {
    @NotBlank String foo;

    @NotNull String bar;

    @Min(1)
    int num;
  }
}
