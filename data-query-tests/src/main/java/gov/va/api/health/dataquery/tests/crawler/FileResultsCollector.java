package gov.va.api.health.dataquery.tests.crawler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.crawler.Result.Outcome;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;

/**
 * A implementation of the results collector that prints each result to a file and creates a
 * separate summary file to give information about all of the results.
 */
@Slf4j
@RequiredArgsConstructor
public class FileResultsCollector implements ResultCollector {
  private final File directory;
  private final Set<String> summary = new ConcurrentSkipListSet<>();
  private final AtomicInteger failures = new AtomicInteger(0);

  @Override
  public void add(Result result) {
    String filename = createFilename(result.query());
    String basicInfo =
        filename
            + ","
            + result.outcome()
            + ","
            + result.httpStatus()
            + ","
            + result.duration().toMillis()
            + ","
            + result.query();
    summary.add(basicInfo);
    log.info(
        "{} {} {} {}",
        result.query(),
        result.outcome(),
        result.httpStatus(),
        result.duration().toMillis());
    if (result.outcome() != Outcome.OK) {
      log.error("{}", result.body());
      if (StringUtils.isNotBlank(result.additionalInfo())) {
        log.error("{}", result.additionalInfo());
      }
      failures.incrementAndGet();
    }
    printBody(filename, result);
    printMetadata(filename, result);
  }

  /**
   * Creates small filename from a query, uses slashes to split for reads and question marks for
   * searches.
   */
  private String createFilename(String query) {
    String[] splitQuery = query.split("/");
    String resourceName = splitQuery[splitQuery.length - 2];
    String params = splitQuery[splitQuery.length - 1];
    if (params.contains("?")) {
      String[] searchParts = params.split("\\?");
      resourceName = searchParts[0];
      params = searchParts[1].replaceAll("patient", "P");
    }
    resourceName = resourceName.replaceAll("([a-z]{2})([a-z]+)", "$1");
    String filename = resourceName + params;
    return filename.replaceAll("[^A-Za-z0-9]", "");
  }

  @Override
  @SneakyThrows
  public void done() {
    String csv = summary.stream().sorted().collect(Collectors.joining("\n"));
    log.info("Summary:\n{}", csv);
    log.info("Made {} requests, {} failures", summary.size(), failures.get());
    Files.write(
        new File(directory, "summary.csv").toPath(),
        summary,
        UTF_8,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE);
  }

  @SneakyThrows
  @Override
  public void init() {
    if (directory.exists()) {
      FileUtils.deleteDirectory(directory);
    }
    assertThat(directory.mkdirs()).withFailMessage("Failed to create %s", directory).isTrue();
    log.info("Collecting results to {}", directory.getAbsolutePath());
  }

  @SneakyThrows
  private void printBody(String filename, Result result) {
    String json =
        StringUtils.isBlank(result.body())
            ? "{ \"message\":\"No body for request.\"}"
            : result.body();
    Files.write(
        new File(directory, filename + ".json").toPath(),
        json.getBytes(UTF_8),
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE);
  }

  @SneakyThrows
  private void printMetadata(String filename, Result result) {
    try (PrintWriter text = new PrintWriter(new File(directory, filename + ".txt"), "UTF-8")) {
      text.print("QUERY: ");
      text.println(result.query());
      text.print("TIMESTAMP: ");
      text.println(result.timestamp());
      text.print("HTTP_STATUS: ");
      text.println(result.httpStatus());
      text.print("OUTCOME: ");
      text.println(result.outcome());
      if (StringUtils.isNotBlank(result.additionalInfo())) {
        text.println("----");
        text.println(result.additionalInfo());
      }
    }
  }
}
