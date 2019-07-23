package gov.va.api.health.dataquery.tests.mockcdw;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.tests.mockcdw.MockEntries.Entry;
import gov.va.api.health.dataquery.tests.mockcdw.MockResponses.FailedMockResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Function;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

/** The look up engine matches stored procedure calls to indexed samples. */
@Builder
@Slf4j
class MockResponseSource {
  private final File index;
  private MockEntries cachedEntries;
  private long timestamp;

  private File baseDirectory() {
    return index.getParentFile();
  }

  @SneakyThrows
  @Synchronized
  private MockEntries cachedEntries() {
    if (cachedEntries == null || timestamp < index.lastModified()) {
      log.info("Loading index {}", index);
      timestamp = index.lastModified();
      MockEntries entries =
          JacksonConfig.createMapper(new YAMLFactory()).readValue(index, MockEntries.class);
      cachedEntries = entries;
    }
    return cachedEntries;
  }

  /** Return the XML sample contents for the call. */
  Optional<String> responseFor(MockCall call) {
    MockEntries entries = cachedEntries();
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
            Files.readAllBytes(new File(baseDirectory(), entry.file()).toPath()),
            StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new FailedMockResponse("Failed to read file contents: " + entry, e);
      }
    };
  }
}
