package gov.va.api.health.sentinel.crawler;

import gov.va.api.health.sentinel.crawler.Result.Outcome;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * This collector will provide a text summary upon completion and also allows failures to be marked
 * as ignored one by one with an ignore filter. An filter applies if it any of the failed query
 * strings end with one of the filter strings.
 */
@Slf4j
@RequiredArgsConstructor(staticName = "wrap")
public class IgnoreFilterResultCollector implements ResultCollector {

  private final ResultCollector delegate;

  private final AtomicInteger failures = new AtomicInteger(0);

  private final Set<String> ignoredFailureSummaries = new ConcurrentSkipListSet<>();

  private final List<String> failuresToIgnore = new ArrayList<>();

  @Override
  public void add(Result result) {
    if (result.outcome() != Outcome.OK) {
      // If this failure is in one or more of the ignore filters then record it but don't fail.
      if (failuresToIgnore.stream().anyMatch(s -> result.query().endsWith(s))) {
        ignoredFailureSummaries.add(result.query() + " " + result.outcome());
      } else {
        failures.incrementAndGet();
      }
    }
    delegate.add(result);
  }

  @Override
  public void done() {
    delegate.done();
    log.info(message());
  }

  public int failures() {
    return failures.get();
  }

  public int ignoredFailures() {
    return ignoredFailureSummaries.size();
  }

  @Override
  public void init() {
    delegate.init();
  }

  private String message() {
    StringBuilder message = new StringBuilder();
    message.append("Ignored Failures Summary");
    message.append("\n--------------------");
    message.append("\nConfigured to ignore these failures:\n");
    message.append(
        failuresToIgnore.size() > 0 ? Arrays.toString(failuresToIgnore.toArray()) : "None");
    message.append("\nWhich resulted in the following failures being ignored:\n");
    if (ignoredFailureSummaries.size() > 0) {
      message.append(ignoredFailureSummaries.stream().sorted().collect(Collectors.joining("\n")));
      message
          .append("\n\nIgnored ")
          .append(ignoredFailureSummaries.size())
          .append(" failures which should be cleaned up!\n");
    } else {
      message.append("None");
    }
    return message.toString();
  }

  /**
   * A comma separated filter list used to exclude any errors in queries that end with the items in
   * the list.
   */
  public void useFilter(String filter) {
    if (StringUtils.isNotEmpty(filter)) {
      failuresToIgnore.addAll(Stream.of(filter.split(",")).collect(Collectors.toList()));
    }
  }
}
