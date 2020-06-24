package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

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

  Function<String, String> anySystemAndExplicitCode = c -> "c is for " + c;

  Function<String, String> explicitSystemAndAnyCode = s -> "s is for " + s;

  Function<String, String> noSystemAndExplicitCode = c -> "c is for " + c;

  BiFunction<String, String, String> explicitSystemAndExplicitCode =
      (s, c) -> "s is for " + s + ", c is for " + c;

  @Test
  public void booleanSupport() {
    assertThat(noSystemExplicitCodeToken.hasAnySystem()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasSupportedSystem("system")).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitlyNoSystem()).isEqualTo(true);
    assertThat(noSystemExplicitCodeToken.hasAnyCode()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitCode()).isEqualTo(true);
    assertThat(noSystemExplicitCodeToken.hasSupportedCode("code")).isEqualTo(true);
    assertThat(noSystemExplicitCodeToken.hasSupportedSystem("system")).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.isSystemExplicitAndUnsupported("system")).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.isCodeExplicitAndUnsupported("code")).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasAnySystem()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasSupportedSystem("system")).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasSupportedSystem("notsystem")).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitlyNoSystem()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasAnyCode()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitCode()).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasSupportedCode(("code"))).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasSupportedSystem("system")).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.isSystemExplicitAndUnsupported(("system")))
        .isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.isCodeExplicitAndUnsupported("code"))
        .isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasAnySystem()).isEqualTo(true);
    assertThat(anySystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasSupportedSystem("system")).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasExplicitlyNoSystem()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasAnyCode()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasExplicitCode()).isEqualTo(true);
    assertThat(anySystemExplicitCodeToken.hasSupportedCode("code")).isEqualTo(true);
    assertThat(noSystemExplicitCodeToken.hasSupportedSystem("system")).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.isSystemExplicitAndUnsupported("notsystem"))
        .isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.isCodeExplicitAndUnsupported("notcode")).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasAnySystem()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasExplicitSystem()).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasSupportedSystem("system")).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasSupportedSystem("notsystem")).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasExplicitlyNoSystem()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasAnyCode()).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasExplicitCode()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasSupportedCode("code")).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasSupportedSystem("system")).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.isSystemExplicitAndUnsupported("notsystem"))
        .isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.isCodeExplicitAndUnsupported("notcode")).isEqualTo(false);
  }

  @Test
  public void checkNullThrowsIllegalState() {
    assertThrows(
        IllegalStateException.class,
        () ->
            anySystemExplicitCodeToken
                .behavior()
                .onAnySystemAndExplicitCode(null)
                .onExplicitSystemAndAnyCode(null)
                .onExplicitSystemAndExplicitCode(null)
                .onNoSystemAndExplicitCode(null)
                .build()
                .execute());
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

  @Test
  public void parseBlank() {
    assertThrows(ResourceExceptions.BadSearchParameter.class, () -> TokenParameter.parse(""));
  }

  @Test
  public void parseNull() {
    assertThrows(ResourceExceptions.BadSearchParameter.class, () -> TokenParameter.parse(null));
  }

  @Test
  public void parsePipe() {
    assertThrows(ResourceExceptions.BadSearchParameter.class, () -> TokenParameter.parse("|"));
  }

  @Test
  public void validParse() {
    assertThat(TokenParameter.parse("|code")).isEqualTo(noSystemExplicitCodeToken);
    assertThat(TokenParameter.parse("system|")).isEqualTo(explicitSystemAnyCodeToken);
    assertThat(TokenParameter.parse("system|code")).isEqualTo(explicitSystemExplicitCodeToken);
    assertThat(TokenParameter.parse("code")).isEqualTo(anySystemExplicitCodeToken);
  }
}
