package gov.va.api.health.dataquery.service.mranderson.client;

import java.net.URLEncoder;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.util.MultiValueMap;

/**
 * Type safe model for searching Mr. Anderson.
 *
 * <pre>
 *   Query.forType(Patient103Root.class)
 *     .profile(Profile.ARGONAUT)
 *     .resource(Patient.class.getSimpleName())
 *     .version("1.03")
 *     .parameters(params)
 *     .type(Patient.class)
 *     .build();
 * </pre>
 */
@Value
@Builder(toBuilder = true)
public class Query<T> {
  Profile profile;

  String resource;

  String version;

  MultiValueMap<String, String> parameters;

  Class<T> type;

  @SneakyThrows
  private static String encode(String value) {
    return URLEncoder.encode(value, "UTF-8");
  }

  /** Start a builder chain to query for a given type. */
  public static <R> QueryBuilder<R> forType(Class<R> forType) {
    return Query.<R>builder().type(forType);
  }

  private static Stream<String> toKeyValueString(Map.Entry<String, List<String>> entry) {
    return entry.getValue().stream().map((value) -> entry.getKey() + '=' + encode(value));
  }

  /**
   * Returns a Mr. Anderson formatted query string, /profile/resource/version?key=value&key=value.
   * The query parameters will be sorted alphabetically.
   */
  String toQueryString() {
    StringBuilder msg = new StringBuilder();
    msg.append('/')
        .append(profile().toPathString())
        .append('/')
        .append(resource())
        .append('/')
        .append(version());
    if (parameters() != null && !parameters().isEmpty()) {
      String params =
          parameters()
              .entrySet()
              .stream()
              .sorted(Comparator.comparing(Entry::getKey))
              .flatMap(Query::toKeyValueString)
              .collect(Collectors.joining("&"));
      msg.append('?').append(params);
    }
    return msg.toString();
  }

  /** The Argonaut profile to request. */
  @SuppressWarnings("unused")
  public enum Profile {
    ARGONAUT,
    DSTU2,
    STU3;

    public String toPathString() {
      return toString().toLowerCase(Locale.ENGLISH);
    }
  }
}
