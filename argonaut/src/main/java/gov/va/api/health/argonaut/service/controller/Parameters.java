package gov.va.api.health.argonaut.service.controller;

import java.util.Arrays;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Provides utilities for working with MultiValueMap typically used for request parameters. */
@NoArgsConstructor(staticName = "builder")
public class Parameters {

  private final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

  /** Create an empty, immutable map. */
  public static MultiValueMap<String, String> empty() {
    return builder().build();
  }

  /** Create a new parameter map with single 'identity' entry. */
  public static MultiValueMap<String, String> forIdentity(String identity) {
    return Parameters.builder().add("identifier", identity).build();
  }

  /** Add a single key/value entry. */
  public Parameters add(String key, String value) {
    params.add(key, value);
    return this;
  }

  /** Add a single key/value entry. */
  public Parameters add(String key, int value) {
    params.add(key, String.valueOf(value));
    return this;
  }

  /** Add a repeated key/value entry, where multiple values are associated to the given key. */
  public Parameters addAll(String key, List<String> values) {
    if (values != null && !values.isEmpty()) {
      params.addAll(key, values);
    }
    return this;
  }

  /** Add a repeated key/value entry, where multiple values are associated to the given key. */
  public Parameters addAll(String key, String... values) {
    if (values != null && values.length > 0) {
      params.addAll(key, Arrays.asList(values));
    }
    return this;
  }

  /** Create an immutable map. */
  public MultiValueMap<String, String> build() {
    return CollectionUtils.unmodifiableMultiValueMap(params);
  }
}
