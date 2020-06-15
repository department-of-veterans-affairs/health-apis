package gov.va.api.health.dataquery.service.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

@Value
@Builder
public class TokenParameter {
  String system;

  String code;

  @NonNull Mode mode;

  /** Create a TokenParameter from a token search parameter. */
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

  public boolean hasSupportedSystem(String allowedSystem) {
    return allowedSystem.equals(system);
  }

  public enum Mode {
    ANY_SYSTEM_EXPLICIT_CODE,
    EXPLICIT_SYSTEM_ANY_CODE,
    EXPLICIT_SYSTEM_EXPLICIT_CODE,
    NO_SYSTEM_EXPLICIT_CODE
  }

  @Value
  @Builder
  public static final class Behavior<T> {
    @NonNull private TokenParameter token;

    private Function<String, T> onAnySystemAndExplicitCode;

    private Function<String, T> onExplicitSystemAndAnyCode;

    private BiFunction<String, String, T> onExplicitSystemAndExplicitCode;

    private Function<String, T> onNoSystemAndExplicitCode;

    /** Execute correct behavior based on the mode of the token. */
    @SneakyThrows
    public T execute() {
      switch (token.mode) {
        case ANY_SYSTEM_EXPLICIT_CODE:
          return onAnySystemAndExplicitCode.apply(token.code);
        case EXPLICIT_SYSTEM_ANY_CODE:
          return onExplicitSystemAndAnyCode.apply(token.system);
        case EXPLICIT_SYSTEM_EXPLICIT_CODE:
          return onExplicitSystemAndExplicitCode.apply(token.system, token.code);
        case NO_SYSTEM_EXPLICIT_CODE:
          return onNoSystemAndExplicitCode.apply(token.code);
        default:
          throw new IllegalStateException("TokenParameter in unsupported mode : " + token.mode);
      }
    }
  }

  public final class BehaviorStemCell {
    public <T> Behavior.BehaviorBuilder<T> onAnySystemAndExplicitCode(Function<String, T> f) {
      return Behavior.<T>builder().token(TokenParameter.this).onAnySystemAndExplicitCode(f);
    }

    public <T> Behavior.BehaviorBuilder<T> onExplicitSystemAndAnyCode(Function<String, T> f) {
      return Behavior.<T>builder().token(TokenParameter.this).onExplicitSystemAndAnyCode(f);
    }

    public <T> Behavior.BehaviorBuilder<T> onExplicitSystemAndExplicitCode(
        BiFunction<String, String, T> f) {
      return Behavior.<T>builder().token(TokenParameter.this).onExplicitSystemAndExplicitCode(f);
    }

    public <T> Behavior.BehaviorBuilder<T> onNoSystemAndExplicitCode(Function<String, T> f) {
      return Behavior.<T>builder().token(TokenParameter.this).onNoSystemAndExplicitCode(f);
    }
  }
}
