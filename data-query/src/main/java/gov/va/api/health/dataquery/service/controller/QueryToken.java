package gov.va.api.health.dataquery.service.controller;

import java.util.function.BiFunction;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;

@Builder
public class QueryToken {
  public String system;

  public String code;

  public Mode mode;

  /** Create a QueryToken from a token search parameter. */
  @SneakyThrows
  public static QueryToken parse(String parameter) {
    if (parameter == null || parameter.isBlank() || parameter.equals("|")) {
      throw new ResourceExceptions.BadSearchParameter(parameter);
    }
    if (parameter.startsWith("|")) {
      return QueryToken.builder()
          .code(parameter.substring(1))
          .mode(Mode.NO_SYSTEM_EXPLICIT_CODE)
          .build();
    }
    if (parameter.endsWith("|")) {
      return QueryToken.builder()
          .system(parameter.substring(0, parameter.length() - 1))
          .mode(Mode.EXPLICIT_SYSTEM_ANY_CODE)
          .build();
    }
    if (parameter.contains("|")) {
      return QueryToken.builder()
          .system(parameter.substring(0, parameter.indexOf("|") - 1))
          .code(parameter.substring((parameter.indexOf("|") + 1)))
          .mode(Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE)
          .build();
    }
    return QueryToken.builder().code(parameter).mode(Mode.ANY_SYSTEM_EXPLICIT_CODE).build();
  }

  public <T> Behavior behavior() {
    return new Behavior();
  }

  public boolean hasExplicitSystem() {
    return mode.equals(Mode.EXPLICIT_SYSTEM_ANY_CODE)
        || mode.equals(Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE);
  }

  public boolean hasNoSystem() {
    return mode.equals(Mode.NO_SYSTEM_EXPLICIT_CODE);
  }

  public boolean isAnySystemAndExplicitCode() {
    return mode.equals(Mode.ANY_SYSTEM_EXPLICIT_CODE);
  }

  public boolean isExplicitSystemAndAnyCode() {
    return mode.equals(Mode.EXPLICIT_SYSTEM_ANY_CODE);
  }

  public boolean isExplicitSystemAndExplicitCode() {
    return mode.equals(Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE);
  }

  public boolean isNoSystemAndExplicitCode() {
    return mode.equals(Mode.NO_SYSTEM_EXPLICIT_CODE);
  }

  private enum Mode {
    EXPLICIT_SYSTEM_EXPLICIT_CODE,
    EXPLICIT_SYSTEM_ANY_CODE,
    ANY_SYSTEM_EXPLICIT_CODE,
    NO_SYSTEM_EXPLICIT_CODE
  }

  public class Behavior {
    public BiFunction<String, String, Specification> explicitSystemAndExplicitCode;

    public Specification execute() {
      return explicitSystemAndExplicitCode.apply(code, system);
    }

    public Behavior onExplicitSystemAndExplicitCode(
        BiFunction<String, String, Specification> explicitSystemAndExplicitCode) {
      this.explicitSystemAndExplicitCode = explicitSystemAndExplicitCode;
      return this;
    }
  }
}
