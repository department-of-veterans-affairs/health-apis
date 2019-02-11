package gov.va.api.health.mranderson.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Utility method for preconditions. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Checks {
  /**
   * If the given value is null or does not match the regular expression an IllegalArgumentException
   * is thrown. Otherwise, the value is returned. Use this method for clean inline argument checks.
   */
  public static String argumentMatches(String value, String regex) {
    if (value == null || !value.matches(regex)) {
      throw new IllegalArgumentException("Expected string to match '" + regex + "'. Got " + value);
    }
    return value;
  }

  /**
   * If the given value is null, an IllegalStateException is thrown. Otherwise, the value is
   * returned. Use this method for clean inline null checks.
   */
  public static <T> T notNull(T maybe) {
    if (maybe == null) {
      throw new IllegalStateException("Expected non-null value");
    }
    return maybe;
  }
}
