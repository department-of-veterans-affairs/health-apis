package gov.va.api.health.mranderson.cdw;

import java.net.URLDecoder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.util.MultiValueMap;

/** The database query. */
@Value
@Builder(toBuilder = true)
public class Query {
  Profile profile;

  String resource;

  String version;

  MultiValueMap<String, String> parameters;

  int page;

  int count;

  @Builder.Default boolean raw = false;

  @SneakyThrows
  private static String decode(String value) {
    return URLDecoder.decode(value, "UTF-8");
  }

  private static Stream<String> toKeyValueString(Map.Entry<String, List<String>> entry) {
    return entry.getValue().stream().map((value) -> entry.getKey() + '=' + decode(value));
  }

  /**
   * Returns a CDW formatted query string, also called a "FHIRString" in the format of
   * /resource:version?key=value&key=value. The query parameters will be sorted alphabetically.
   */
  public String toQueryString() {
    StringBuilder msg = new StringBuilder();
    msg.append('/').append(resource).append(':').append(version);
    if (parameters != null && !parameters.isEmpty()) {
      String params =
          parameters
              .entrySet()
              .stream()
              .sorted(Comparator.comparing(Entry::getKey))
              .flatMap(Query::toKeyValueString)
              .collect(Collectors.joining("&"));
      msg.append('?').append(params);
    }
    return msg.toString();
  }

  /** Returns an abbreviated query string that just contains the /resource:version part. */
  String toResourceString() {
    return '/' + resource + ':' + version;
  }
}
