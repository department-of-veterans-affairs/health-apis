package gov.va.api.health.dataquery.tests.mockcdw;

import gov.va.api.health.dataquery.tests.mockcdw.MockEntries.Entry;
import gov.va.api.health.dataquery.tests.mockcdw.MockResponses.FailedMockResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Function;
import lombok.Builder;
import lombok.Value;

/** The look up engine matches stored procedure calls to indexed samples. */
@Value
@Builder
class MockResponseSource {
  File baseDirectory;
  MockEntries entries;

  /** Return the XML sample contents for the call. */
  Optional<String> responseFor(MockCall call) {
    Optional<String> exactMatch =
        entries.entries().stream().filter(call::matches).findFirst().map(toFileContents());
    return exactMatch.isPresent()
        ? exactMatch
        : entries.entries().stream().filter(call::matchesMostly).findFirst().map(toFileContents());
  }

  private Function<Entry, String> toFileContents() {
    return entry -> {
      try {
        return new String(
            Files.readAllBytes(new File(baseDirectory, entry.file()).toPath()),
            StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new FailedMockResponse("Failed to read file contents: " + entry, e);
      }
    };
  }
}
