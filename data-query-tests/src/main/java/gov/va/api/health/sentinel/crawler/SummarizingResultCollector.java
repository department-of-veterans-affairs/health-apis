package gov.va.api.health.sentinel.crawler;

import gov.va.api.health.sentinel.crawler.Result.Outcome;
import gov.va.api.health.sentinel.crawler.Result.Summary;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** This collector will provide a text summary upon completion. */
@Slf4j
@RequiredArgsConstructor(staticName = "wrap")
public class SummarizingResultCollector implements ResultCollector {
  private final ResultCollector delegate;

  private final Collection<Result.Summary> summaries = new ConcurrentLinkedQueue<>();

  private final AtomicInteger failures = new AtomicInteger(0);

  @Override
  public void add(Result result) {
    summaries.add(result.summarize());
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

  public int failures() {
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
    return summaries.stream().filter(s -> s.outcome() == outcome).map(Result.Summary::query);
  }

  private String resourceCountsSummary() {
    Map<String, Integer> readCounts = resourceReadCounts();
    Map<String, Integer> searchCounts = resourceSearchCounts();
    SortedSet<String> keys = new TreeSet<>();
    keys.addAll(readCounts.keySet());
    keys.addAll(searchCounts.keySet());
    int maxKeyLength = keys.stream().mapToInt(key -> key.length()).max().orElse(0);
    String readHeader = "Reads";
    String searchHeader = "Searches";
    final StringBuilder result = new StringBuilder();
    result
        .append("\n")
        .append(String.format("%-" + maxKeyLength + "s", "Resource"))
        .append(" ")
        .append(readHeader)
        .append(" ")
        .append(searchHeader);
    for (String key : keys) {
      result.append("\n").append(String.format("%-" + maxKeyLength + "s", key));
      String readCount =
          String.format("%" + readHeader.length() + "d", readCounts.getOrDefault(key, 0));
      result.append(" ").append(readCount);
      String searchCount =
          String.format("%" + searchHeader.length() + "d", searchCounts.getOrDefault(key, 0));
      result.append(" ").append(searchCount);
    }
    return result.toString();
  }

  private Map<String, Integer> resourceReadCounts() {
    Map<String, Integer> counts = new HashMap<>();
    for (final Summary summary : summaries) {
      String resource = ResourceDiscovery.resource(summary.query());
      if (!ResourceDiscovery.isSearch(summary.query())) {
        counts.put(resource, counts.getOrDefault(resource, 0) + 1);
      }
    }
    return counts;
  }

  private Map<String, Integer> resourceSearchCounts() {
    Map<String, Integer> counts = new HashMap<>();
    for (final Summary summary : summaries) {
      String resource = ResourceDiscovery.resource(summary.query());
      if (ResourceDiscovery.isSearch(summary.query())) {
        counts.put(resource, counts.getOrDefault(resource, 0) + 1);
      }
    }
    return counts;
  }
}
