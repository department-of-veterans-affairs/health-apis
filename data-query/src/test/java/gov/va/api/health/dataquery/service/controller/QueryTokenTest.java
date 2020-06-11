package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.Test;

public class QueryTokenTest {
  QueryToken noSystemExplicitCodeToken =
      QueryToken.builder()
          .code("code")
          .system(null)
          .mode(QueryToken.Mode.NO_SYSTEM_EXPLICIT_CODE)
          .build();

  QueryToken explicitSystemAnyCodeToken =
      QueryToken.builder()
          .code(null)
          .system("system")
          .mode(QueryToken.Mode.EXPLICIT_SYSTEM_ANY_CODE)
          .build();

  QueryToken explicitSystemExplicitCodeToken =
      QueryToken.builder()
          .code("code")
          .system("system")
          .mode(QueryToken.Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE)
          .build();

  QueryToken anySystemExplicitCodeToken =
      QueryToken.builder()
          .code("code")
          .system(null)
          .mode(QueryToken.Mode.ANY_SYSTEM_EXPLICIT_CODE)
          .build();

  @Test
  public void booleanSupport() {
    assertThat(noSystemExplicitCodeToken.hasAnySystemAndExplicitCode()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitSystemAndAnyCode()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasExplicitSystemAndExplicitCode()).isEqualTo(false);
    assertThat(noSystemExplicitCodeToken.hasNoSystem()).isEqualTo(true);
    assertThat(noSystemExplicitCodeToken.hasNoSystemAndExplicitCode()).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasAnySystemAndExplicitCode()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitSystemAndAnyCode()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasExplicitSystemAndExplicitCode()).isEqualTo(true);
    assertThat(explicitSystemExplicitCodeToken.hasNoSystem()).isEqualTo(false);
    assertThat(explicitSystemExplicitCodeToken.hasNoSystemAndExplicitCode()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasAnySystemAndExplicitCode()).isEqualTo(true);
    assertThat(anySystemExplicitCodeToken.hasExplicitSystem()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasExplicitSystemAndAnyCode()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasExplicitSystemAndExplicitCode()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasNoSystem()).isEqualTo(false);
    assertThat(anySystemExplicitCodeToken.hasNoSystemAndExplicitCode()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasAnySystemAndExplicitCode()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasExplicitSystem()).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasExplicitSystemAndAnyCode()).isEqualTo(true);
    assertThat(explicitSystemAnyCodeToken.hasExplicitSystemAndExplicitCode()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasNoSystem()).isEqualTo(false);
    assertThat(explicitSystemAnyCodeToken.hasNoSystemAndExplicitCode()).isEqualTo(false);
  }

  @Test
  public void execute() {
    Function<String, String> anySystemAndExplicitCode = c -> "c is for " + c;
    Function<String, String> explicitSystemAndAnyCode = s -> "s is for " + s;
    BiFunction<String, String, String> explicitSystemAndExplicitCode =
        (c, s) -> "s is for " + s + ", c is for " + c;
    assertThat(
            anySystemExplicitCodeToken
                .behavior()
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .execute())
        .isEqualTo("c is for code");
    assertThat(
            explicitSystemExplicitCodeToken
                .behavior()
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .execute())
        .isEqualTo("s is for system, c is for code");
    assertThat(
            explicitSystemAnyCodeToken
                .behavior()
                .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
                .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
                .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
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
        .behavior()
        .onAnySystemAndExplicitCode(anySystemAndExplicitCode)
        .onExplicitSystemAndAnyCode(explicitSystemAndAnyCode)
        .onExplicitSystemAndExplicitCode(explicitSystemAndExplicitCode)
        .execute();
  }

  @Test(expected = ResourceExceptions.BadSearchParameter.class)
  public void parseBlank() {
    QueryToken.parse("");
  }

  @Test(expected = ResourceExceptions.BadSearchParameter.class)
  public void parseNull() {
    QueryToken.parse(null);
  }

  @Test(expected = ResourceExceptions.BadSearchParameter.class)
  public void parsePipe() {
    QueryToken.parse("|");
  }

  @Test
  public void validParse() {
    assertThat(QueryToken.parse("|code")).isEqualTo(noSystemExplicitCodeToken);
    assertThat(QueryToken.parse("system|")).isEqualTo(explicitSystemAnyCodeToken);
    assertThat(QueryToken.parse("system|code")).isEqualTo(explicitSystemExplicitCodeToken);
    assertThat(QueryToken.parse("code")).isEqualTo(anySystemExplicitCodeToken);
  }
}
