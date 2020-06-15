package gov.va.api.health.dataquery.service.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

@Builder
@Value
public class TokenParameter<T> {
  String system;

  String code;

  @NonNull Mode mode;

  /** Create a QueryToken from a token search parameter. */
  @SneakyThrows
  public static <T> TokenParameter<T> parse(String parameter) {
    if (isBlank(parameter) || parameter.equals("|")) {
      throw new ResourceExceptions.BadSearchParameter(parameter);
    }
    if (parameter.startsWith("|")) {
      return TokenParameter.<T>builder()
          .code(parameter.substring(1))
          .mode(Mode.NO_SYSTEM_EXPLICIT_CODE)
          .build();
    }
    if (parameter.endsWith("|")) {
      return TokenParameter.<T>builder()
          .system(parameter.substring(0, parameter.length() - 1))
          .mode(Mode.EXPLICIT_SYSTEM_ANY_CODE)
          .build();
    }
    if (parameter.contains("|")) {
      return TokenParameter.<T>builder()
          .system(parameter.substring(0, parameter.indexOf("|")))
          .code(parameter.substring((parameter.indexOf("|") + 1)))
          .mode(Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE)
          .build();
    }
    return TokenParameter.<T>builder().code(parameter).mode(Mode.ANY_SYSTEM_EXPLICIT_CODE).build();
  }

  public Behavior.BehaviorBuilder<T> behavior() {
    return Behavior.<T>builder().tokenParameter(this);
  }

  public boolean hasAllowedSystem(String allowedSystem) {
    return allowedSystem.equals(system);
  }

  public boolean hasAnyCode() {
    return mode == Mode.EXPLICIT_SYSTEM_ANY_CODE;
  }

  public boolean hasAnySystem() {
    return mode == Mode.ANY_SYSTEM_EXPLICIT_CODE;
  }

  public boolean hasExplicitCode() {
    return mode == Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE || mode == Mode.NO_SYSTEM_EXPLICIT_CODE;
  }

  public boolean hasExplicitSystem() {
    return mode == Mode.EXPLICIT_SYSTEM_ANY_CODE || mode == Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE;
  }

  public boolean hasNoSystem() {
    return mode == Mode.NO_SYSTEM_EXPLICIT_CODE;
  }

  public enum Mode {
    EXPLICIT_SYSTEM_EXPLICIT_CODE,
    EXPLICIT_SYSTEM_ANY_CODE,
    ANY_SYSTEM_EXPLICIT_CODE,
    NO_SYSTEM_EXPLICIT_CODE
  }

  @Value
  @Builder
  public static class Behavior<T> {
    private BiFunction<String, String, T> onExplicitSystemAndExplicitCode;

    private Function<String, T> onAnySystemAndExplicitCode;

    private Function<String, T> onExplicitSystemAndAnyCode;

    private Function<String, T> onNoSystemAndExplicitCode;

    private TokenParameter<T> tokenParameter;

    /** Execute correct behavior based on the mode of the token. */
    @SneakyThrows
    public T execute() {
      switch (tokenParameter.mode) {
        case ANY_SYSTEM_EXPLICIT_CODE:
          return onAnySystemAndExplicitCode.apply(tokenParameter.code);
        case EXPLICIT_SYSTEM_ANY_CODE:
          return onExplicitSystemAndAnyCode.apply(tokenParameter.system);
        case EXPLICIT_SYSTEM_EXPLICIT_CODE:
          return onExplicitSystemAndExplicitCode.apply(tokenParameter.system, tokenParameter.code);
        case NO_SYSTEM_EXPLICIT_CODE:
          return onNoSystemAndExplicitCode.apply(tokenParameter.code);
        default:
          throw new IllegalStateException(
              "QueryToken in unsupported mode : " + tokenParameter.mode);
      }
    }
  }
}
