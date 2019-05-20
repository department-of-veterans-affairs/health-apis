package gov.va.api.health.dataquery.tests.mockcdw;

import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
class MockResponses {
  @Singular List<MockResponseSource> sources;

  String responseFor(MockCall call) {
    return sources
        .stream()
        .map(source -> source.responseFor(call))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElseThrow(() -> new FailedMockResponse("No sample found: " + call.toString()));
  }

  static class FailedMockResponse extends RuntimeException {
    FailedMockResponse(String message) {
      super(message);
    }

    public FailedMockResponse(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
