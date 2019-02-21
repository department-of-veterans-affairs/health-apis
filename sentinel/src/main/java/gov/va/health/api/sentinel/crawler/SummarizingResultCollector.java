package gov.va.health.api.sentinel.crawler;

import gov.va.health.api.sentinel.crawler.Result.Outcome;
import gov.va.health.api.sentinel.crawler.Result.Summary;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

/** This collector will provide a text summary upon completion. */
@RequiredArgsConstructor(staticName = "wrap")
public class SummarizingResultCollector implements ResultCollector {

  private final ResultCollector delegate;

  private final Collection<Result.Summary> summaries = new ConcurrentLinkedQueue<>();

  private final AtomicInteger failures = new AtomicInteger(0);

  private static boolean isSearch(String url) {
    int apiIndex = url.indexOf("/api/");
    if (apiIndex <= -1) {
      return false;
    }
    String query = url.substring(apiIndex + "/api/".length());
    return query.contains("?");
  }

  static String resource(String url) {
    Matcher matcher = Pattern.compile(".*/api/([^/^?]+).*").matcher(url);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      // log.warn("Failed to extract resource from url '{}'.", url);
      return url;
    }
  }

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
  }

  public int failures() {
    return failures.get();
  }

  @Override
  public void init() {
    delegate.init();
  }

  /** Return a message suitable to being printed to the console. */
  public String message() {
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
    for (final Summary s : summaries) {
      String resource = resource(s.query());
      if (!isSearch(s.query())) {
        counts.put(resource, counts.getOrDefault(resource, 0) + 1);
      }
    }
    return counts;
  }

  private Map<String, Integer> resourceSearchCounts() {
    Map<String, Integer> counts = new HashMap<>();
    for (final Summary s : summaries) {
      String resource = resource(s.query());
      if (isSearch(s.query())) {
        counts.put(resource, counts.getOrDefault(resource, 0) + 1);
      }
    }
    return counts;
  }
}
