package gov.va.api.health.sentinel.mockcdw;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * The index file model.
 *
 * <p>See {@link MockEntityReturnDriver}
 */
@Value
@Builder(toBuilder = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class MockEntries {
  List<Entry> entries;

  @Value
  @Builder(toBuilder = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static class Entry {
    String file;
    String query;
    int count;
    int page;
  }
}
