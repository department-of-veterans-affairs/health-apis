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
public class TokenParameter {
  String system;

  String code;

  @NonNull Mode mode;

  /** Create a QueryToken from a token search parameter. */
  @SneakyThrows
  public static TokenParameter parse(String parameter) {
    if (isBlank(parameter) || parameter.equals("|")) {
      throw new ResourceExceptions.BadSearchParameter(parameter);
    }
    if (parameter.startsWith("|")) {
      return TokenParameter.builder()
          .code(parameter.substring(1))
          .mode(Mode.NO_SYSTEM_EXPLICIT_CODE)
          .build();
    }
    if (parameter.endsWith("|")) {
      return TokenParameter.builder()
          .system(parameter.substring(0, parameter.length() - 1))
          .mode(Mode.EXPLICIT_SYSTEM_ANY_CODE)
          .build();
    }
    if (parameter.contains("|")) {
      return TokenParameter.builder()
          .system(parameter.substring(0, parameter.indexOf("|")))
          .code(parameter.substring((parameter.indexOf("|") + 1)))
          .mode(Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE)
          .build();
    }
    return TokenParameter.builder().code(parameter).mode(Mode.ANY_SYSTEM_EXPLICIT_CODE).build();
  }

  public BehaviorStemCell behavior() {
    return new BehaviorStemCell();
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

  public class BehaviorStemCell {
    public <T> Behavior<T> onAnySystemAndExplicitCode(Function<String, T> action) {
      return new Behavior<T>().onAnySystemAndExplicitCode(action);
    }

    public <T> Behavior<T> onExplicitSystemAndAnyCode(Function<String, T> action) {
      return new Behavior<T>().onExplicitSystemAndAnyCode(action);
    }

    public <T> Behavior<T> onExplicitSystemAndExplicitCode(BiFunction<String, String, T> action) {
      return new Behavior<T>().onExplicitSystemAndExplicitCode(action);
    }
  }

  public class Behavior<T> {
    private BiFunction<String, String, T> explicitSystemAndExplicitCode;

    private Function<String, T> anySystemAndExplicitCode;

    private Function<String, T> explicitSystemAndAnyCode;

    /** Execute correct behavior based on the mode of the token. */
    @SneakyThrows
    public T execute() {
      switch (mode) {
        case ANY_SYSTEM_EXPLICIT_CODE:
          return anySystemAndExplicitCode.apply(code);
        case EXPLICIT_SYSTEM_ANY_CODE:
          return explicitSystemAndAnyCode.apply(system);
        case EXPLICIT_SYSTEM_EXPLICIT_CODE:
          return explicitSystemAndExplicitCode.apply(system, code);
        default:
          throw new IllegalStateException("QueryToken in unsupported mode : " + mode);
      }
    }

    public Behavior<T> onAnySystemAndExplicitCode(Function<String, T> action) {
      anySystemAndExplicitCode = action;
      return this;
    }

    public Behavior<T> onExplicitSystemAndAnyCode(Function<String, T> action) {
      explicitSystemAndAnyCode = action;
      return this;
    }

    public Behavior<T> onExplicitSystemAndExplicitCode(BiFunction<String, String, T> action) {
      explicitSystemAndExplicitCode = action;
      return this;
    }
  }
}
