package gov.va.api.health.dataquery.service.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

/** Avoid usage. Use Vulcan instead. */
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

  /** Determines if the token has an explicit code. */
  public boolean hasExplicitCode() {
    return mode == Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE
        || mode == Mode.NO_SYSTEM_EXPLICIT_CODE
        || mode == Mode.ANY_SYSTEM_EXPLICIT_CODE;
  }

  public boolean hasExplicitSystem() {
    return mode == Mode.EXPLICIT_SYSTEM_ANY_CODE || mode == Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE;
  }

  public boolean hasExplicitlyNoSystem() {
    return mode == Mode.NO_SYSTEM_EXPLICIT_CODE;
  }

  public boolean hasSupportedCode(String supportedCode) {
    return supportedCode.equals(code);
  }

  public boolean hasSupportedCode(String... codes) {
    return Arrays.stream(codes).anyMatch(this::hasSupportedCode);
  }

  public boolean hasSupportedSystem(String supportedSystem) {
    return supportedSystem.equals(system);
  }

  public boolean hasSupportedSystem(String... systems) {
    return Arrays.stream(systems).anyMatch(this::hasSupportedSystem);
  }

  public boolean isCodeExplicitAndUnsupported(String... codes) {
    return hasExplicitCode() && !hasSupportedCode(codes);
  }

  public boolean isCodeExplicitlySetAndOneOf(String... codes) {
    return hasExplicitCode() && hasSupportedCode(codes);
  }

  public boolean isSystemExplicitAndUnsupported(String... systems) {
    return hasExplicitSystem() && !hasSupportedSystem(systems);
  }

  public boolean isSystemExplicitlySetAndOneOf(String... systems) {
    return hasExplicitSystem() && hasSupportedSystem(systems);
  }

  /** Avoid usage. */
  public enum Mode {
    /** e.g. cool */
    ANY_SYSTEM_EXPLICIT_CODE,
    /** e.g. http://fonzy.com| */
    EXPLICIT_SYSTEM_ANY_CODE,
    /** e.g. http://fonzy.com|cool */
    EXPLICIT_SYSTEM_EXPLICIT_CODE,
    /** e.g. |cool */
    NO_SYSTEM_EXPLICIT_CODE
  }

  /** Avoid usage. */
  @Value
  @Builder
  public static class Behavior<T> {
    @NonNull TokenParameter token;

    Function<String, T> onAnySystemAndExplicitCode;

    Function<String, T> onExplicitSystemAndAnyCode;

    BiFunction<String, String, T> onExplicitSystemAndExplicitCode;

    Function<String, T> onNoSystemAndExplicitCode;

    /** Check if behavior is specified before executing it. */
    public <T1> T1 check(T1 n) {
      if (n == null) {
        throw new IllegalStateException("no handler specified for " + token.mode);
      }
      return n;
    }

    /** Execute correct behavior based on the mode of the token. */
    @SuppressWarnings("UnnecessaryDefault")
    @SneakyThrows
    public T execute() {
      switch (token.mode) {
        case ANY_SYSTEM_EXPLICIT_CODE:
          return check(onAnySystemAndExplicitCode).apply(token.code);
        case EXPLICIT_SYSTEM_ANY_CODE:
          return check(onExplicitSystemAndAnyCode).apply(token.system);
        case EXPLICIT_SYSTEM_EXPLICIT_CODE:
          return check(onExplicitSystemAndExplicitCode).apply(token.system, token.code);
        case NO_SYSTEM_EXPLICIT_CODE:
          return check(onNoSystemAndExplicitCode).apply(token.code);
        default:
          throw new IllegalStateException("TokenParameter in unsupported mode : " + token.mode);
      }
    }
  }

  /** Avoid usage. */
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

    @SuppressWarnings("unused")
    public <T> Behavior.BehaviorBuilder<T> onNoSystemAndExplicitCode(Function<String, T> f) {
      return Behavior.<T>builder().token(TokenParameter.this).onNoSystemAndExplicitCode(f);
    }
  }
}
