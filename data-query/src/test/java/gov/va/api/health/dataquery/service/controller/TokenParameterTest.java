package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.Test;

public class TokenParameterTest {
  TokenParameter<String> noSystemExplicitCodeToken =
      TokenParameter.<String>builder()
          .code("code")
          .system(null)
          .mode(TokenParameter.Mode.NO_SYSTEM_EXPLICIT_CODE)
          .build();

  TokenParameter<String> explicitSystemAnyCodeToken =
      TokenParameter.<String>builder()
          .code(null)
          .system("system")
          .mode(TokenParameter.Mode.EXPLICIT_SYSTEM_ANY_CODE)
          .build();

  TokenParameter<String> explicitSystemExplicitCodeToken =
      TokenParameter.<String>builder()
          .code("code")
          .system("system")
          .mode(TokenParameter.Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE)
          .build();

  TokenParameter<String> anySystemExplicitCodeToken =
      TokenParameter.<String>builder()
          .code("code")
          .system(null)
          .mode(TokenParameter.Mode.ANY_SYSTEM_EXPLICIT_CODE)
          .build();

  Function<String, String> anySystemAndExplicitCode = c -> "c is for " + c;
  Function<String, String> explicitSystemAndAnyCode = s -> "s is for " + s;
  Function<String, String> noSystemAndExplicitCode = c -> "c is for " + c;
  BiFunction<String, String, String> explicitSystemAndExplicitCode =
      (s, c) -> "s is for " + s + ", c is for " + c;

  @Test
  public void booleanSupport() {
    assertThat(noSystemExplicitCodeToken.hasAnySystem()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasAllowedSystem("system")).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasNoSystem()).isEqualTo(true);
    assertThat(noSystemExplicitCodeToken.hasAnyCode()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitCode()).isEqualTo(true);

    assertThat(explicitSystemExplicitCodeToken.hasAnySystem()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasAllowedSystem("system")).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasAllowedSystem("notsystem")).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasNoSystem()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasAnyCode()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitCode()).isEqualTo(true);

    assertThat(anySystemExplicitCodeToken.hasAnySystem()).isEqualTo(true);
    assertThat(anySystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasAllowedSystem("system")).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasNoSystem()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasAnyCode()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasExplicitCode()).isEqualTo(false);

    assertThat(explicitSystemAnyCodeToken.hasAnySystem()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasExplicitSystem()).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasAllowedSystem("system")).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasAllowedSystem("notsystem")).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasNoSystem()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasAnyCode()).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasExplicitCode()).isEqualTo(false);
  }

  @Test
  public void execute() {
    assertThat(
            anySystemExplicitCodeToken
                .behavior()
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .onNoSystemAndExplicitCode(noSystemAndExplicitCode)
                .build()
                .execute())
        .isEqualTo("c is for code");
    assertThat(
            explicitSystemExplicitCodeToken
                .behavior()
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .onNoSystemAndExplicitCode(noSystemAndExplicitCode)
                .build()
                .execute())
        .isEqualTo("s is for system, c is for code");
    assertThat(
            explicitSystemAnyCodeToken
                .behavior()
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
                .onNoSystemAndExplicitCode(noSystemAndExplicitCode)
                .build()
                .execute())
        .isEqualTo("s is for system");
    assertThat(
            noSystemExplicitCodeToken
                .behavior()
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .onNoSystemAndExplicitCode(noSystemAndExplicitCode)
                .build()
                .execute())
        .isEqualTo("c is for code");
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
