package gov.va.api.health.dataquery.service.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EnumSearcher<T extends Enum<T>> {

  private final Class<T> desiredType;

  /** Start a builder chain to query for a given type. */
  public static <T extends Enum<T>> EnumSearcher<T> of(Class<T> type) {
    return new EnumSearcher<T>(type);
  }

  /**
   * Provided a String, search the enum by name, and by JsonProperty annotation for the matching
   * enum.
   */
  @SneakyThrows
  public T find(String s) {
    return Arrays.stream(desiredType.getEnumConstants())
        .filter(e -> isMe(e, s))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "No enum constant " + desiredType.getCanonicalName() + "." + s));
  }

  @SneakyThrows
  private boolean isMe(T e, String s) {
    if (e.name().equals(s)) {
      return true;
    }
    JsonProperty jp = e.getClass().getField(e.name()).getAnnotation(JsonProperty.class);
    if (jp != null) {
      return jp.value().equals(s);
    }
    return false;
  }
}
