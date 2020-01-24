package gov.va.api.health.dataquery.tests.crawler;

import gov.va.api.health.dataquery.tests.crawler.Result.Outcome;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** This collector will provide a text summary upon completion. */
@Slf4j
@RequiredArgsConstructor(staticName = "wrap")
public class SummarizingResultCollector implements ResultCollector {
  private final ResultCollector delegate;

  private final Collection<Result> summaries = new ConcurrentLinkedQueue<>();

  private final AtomicInteger failures = new AtomicInteger(0);

  @Override
  public void add(Result result) {
    summaries.add(result);
    if (result.outcome() != Outcome.OK) {
      failures.incrementAndGet();
    }
    delegate.add(result);
  }

  @Override
  public void done() {
    delegate.done();
    log.info(message());
  }

  int failures() {
    return failures.get();
  }

  @Override
  public void init() {
    delegate.init();
  }

  /** Return a message suitable to being printed to the console. */
  private String message() {
    StringBuilder message = new StringBuilder();
    message.append("Outcomes");
    message.append("\n--------------------");
    message.append(resourceCountsSummary());
    message.append("\n--------------------");
    for (Outcome outcome : Outcome.values()) {
      message.append("\n").append(outcome).append(": ").append(queriesWithOutcome(outcome).count());
    }
    message.append("\n--------------------");
    message.append("\nTotal: ").append(summaries.size());
    message.append("\nFailures: ").append(failures.get());
    if (failures.get() > 0) {
      message.append("\nFAILURE");
    } else {
      message.append("\nSUCCESS");
    }
    return message.toString();
  }

  private Stream<String> queriesWithOutcome(Result.Outcome outcome) {
    return summaries.stream().filter(s -> s.outcome() == outcome).map(Result::query);
  }

  private String resourceCountsSummary() {

    Map<String, SummaryStats> readStats = summarize(r -> !ResourceDiscovery.isSearch(r.query()));
    Map<String, SummaryStats> searchStats = summarize(r -> ResourceDiscovery.isSearch(r.query()));
    SortedSet<String> keys = new TreeSet<>();
    keys.addAll(readStats.keySet());
    keys.addAll(searchStats.keySet());
    final StringBuilder result = new StringBuilder();
    result
        .append("\n                      ______ READS ______ ____ SEARCHES _____")
        .append("\nRESOURCE            ")
        .append(" ")
        .append("COUNT")
        .append(" ERRORS AVG_MS ")
        .append("COUNT")
        .append(" ERRORS AVG_MS\n");

    String rowFormat =
        "%-20s " // resource
            + "%5d" // reads
            + " %6d %6d " // failures and avg time
            + "%5d" // searches
            + " %6d %6d"; // failures and avg time

    for (String key : keys) {
      SummaryStats read = readStats.getOrDefault(key, new SummaryStats());
      SummaryStats search = searchStats.getOrDefault(key, new SummaryStats());

      result
          .append(
              String.format(
                  rowFormat,
                  key,
                  read.count(),
                  read.failures(),
                  read.averageTime(),
                  search.count(),
                  search.failures(),
                  search.averageTime()))
          .append("\n");
    }
    return result.toString();
  }

  private Map<String, SummaryStats> summarize(Predicate<Result> include) {
    Map<String, SummaryStats> statsByResource = new HashMap<>();

    summaries.stream()
        .filter(include)
        .forEach(
            result -> {
              var resource = ResourceDiscovery.resource(result.query());
              var stats = statsByResource.getOrDefault(resource, new SummaryStats());
              stats.add(result);
              statsByResource.put(resource, stats);
            });
    return statsByResource;
  }

  @Getter
  private static class SummaryStats {
    private int count;
    private int totalTime;
    private int failures;

    void add(Result result) {
      count += 1;
      totalTime += result.duration().toMillis();
      if (result.outcome() != Outcome.OK) {
        failures += 1;
      }
    }

    long averageTime() {
      return Math.round(totalTime / (double) count);
    }
  }
}
