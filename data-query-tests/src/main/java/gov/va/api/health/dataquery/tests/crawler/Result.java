package gov.va.api.health.dataquery.tests.crawler;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/** The result of the query. */
@AllArgsConstructor
@Builder
@Value
public class Result {
  @NonNull Instant timestamp;
  @NonNull String query;
  /**
   * HTTP status will be 0 if we failed to communicate with the server, otherwise it will be result
   * of the call.
   */
  int httpStatus;

  String body;
  @NonNull Outcome outcome;
  String additionalInfo;

  public Summary summarize() {
    return new Summary(query, outcome);
  }

  @SuppressWarnings("unused")
  public enum Outcome {
    OK,
    INVALID_PAYLOAD,
    INVALID_STATUS,
    INVALID_URL,
    REQUEST_FAILED
  }

  @Value
  @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
  public static class Summary {
    @NonNull String query;
    @NonNull Outcome outcome;
  }
}
