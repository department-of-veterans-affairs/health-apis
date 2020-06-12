package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.Test;

public class TokenParameterTest {
  TokenParameter noSystemExplicitCodeToken =
      TokenParameter.builder()
          .code("code")
          .system(null)
          .mode(TokenParameter.Mode.NO_SYSTEM_EXPLICIT_CODE)
          .build();

  TokenParameter explicitSystemAnyCodeToken =
      TokenParameter.builder()
          .code(null)
          .system("system")
          .mode(TokenParameter.Mode.EXPLICIT_SYSTEM_ANY_CODE)
          .build();

  TokenParameter explicitSystemExplicitCodeToken =
      TokenParameter.builder()
          .code("code")
          .system("system")
          .mode(TokenParameter.Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE)
          .build();

  TokenParameter anySystemExplicitCodeToken =
      TokenParameter.builder()
          .code("code")
          .system(null)
          .mode(TokenParameter.Mode.ANY_SYSTEM_EXPLICIT_CODE)
          .build();

  @Test
  public void booleanSupport() {
    assertThat(noSystemExplicitCodeToken.hasAnySystem()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasNoSystem()).isEqualTo(true);
    assertThat(noSystemExplicitCodeToken.hasAnyCode()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitCode()).isEqualTo(true);

    assertThat(explicitSystemExplicitCodeToken.hasAnySystem()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasNoSystem()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasAnyCode()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitCode()).isEqualTo(true);

    assertThat(anySystemExplicitCodeToken.hasAnySystem()).isEqualTo(true);
    assertThat(anySystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasNoSystem()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasAnyCode()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasExplicitCode()).isEqualTo(false);

    assertThat(explicitSystemAnyCodeToken.hasAnySystem()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasExplicitSystem()).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasNoSystem()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasAnyCode()).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasExplicitCode()).isEqualTo(false);
  }

  @Test
  public void execute() {
    Function<String, String> anySystemAndExplicitCode = c -> "c is for " + c;
    Function<String, String> explicitSystemAndAnyCode = s -> "s is for " + s;
    BiFunction<String, String, String> explicitSystemAndExplicitCode =
        (s, c) -> "c is for " + c + ", s is for " + s;
    assertThat(
            anySystemExplicitCodeToken
                .<String>behavior()
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .build()
                .execute())
        .isEqualTo("c is for code");
    assertThat(
            explicitSystemExplicitCodeToken
                .<String>behavior()
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .build()
                .execute())
        .isEqualTo("c is for code, s is for system");
    assertThat(
            explicitSystemAnyCodeToken
                .<String>behavior()
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
                .build()
                .execute())
        .isEqualTo("s is for system");
  }

  @Test(expected = IllegalStateException.class)
  public void invalidStateExecute() {
    Function<String, String> anySystemAndExplicitCode = c -> "c is for " + c;
    Function<String, String> explicitSystemAndAnyCode = s -> "s is for " + s;
    BiFunction<String, String, String> explicitSystemAndExplicitCode =
        (c, s) -> "s is for " + s + ", c is for " + c;
    noSystemExplicitCodeToken
        .<String>behavior()
        .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
        .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
        .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
        .build()
        .execute();
  }

  @Test(expected = ResourceExceptions.BadSearchParameter.class)
  public void parseBlank() {
    TokenParameter.parse("");
  }

  @Test(expected = ResourceExceptions.BadSearchParameter.class)
  public void parseNull() {
    TokenParameter.parse(null);
  }

  @Test(expected = ResourceExceptions.BadSearchParameter.class)
  public void parsePipe() {
    TokenParameter.parse("|");
  }

  @Test
  public void validParse() {
    assertThat(TokenParameter.parse("|code")).isEqualTo(noSystemExplicitCodeToken);
    assertThat(TokenParameter.parse("system|")).isEqualTo(explicitSystemAnyCodeToken);
    assertThat(TokenParameter.parse("system|code")).isEqualTo(explicitSystemExplicitCodeToken);
    assertThat(TokenParameter.parse("code")).isEqualTo(anySystemExplicitCodeToken);
  }
}
