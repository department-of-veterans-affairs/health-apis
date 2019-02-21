package gov.va.health.api.sentinel.crawler;

import gov.va.health.api.sentinel.crawler.Result.Outcome;
import gov.va.health.api.sentinel.crawler.Result.Summary;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
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
    message.append("\n--------------------\nReads");
    for (Map.Entry<String, Integer> countEntry : readCounts().entrySet()) {
      message.append("\n").append(countEntry.getKey()).append(": ").append(countEntry.getValue());
    }
    message.append("\n--------------------\nSearches");
    for (Map.Entry<String, Integer> countEntry : searchCounts().entrySet()) {
      message.append("\n").append(countEntry.getKey()).append(": ").append(countEntry.getValue());
    }
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

  private Map<String, Integer> readCounts() {
    Map<String, Integer> counts = new TreeMap<>();
    for (final Summary s : summaries) {
      String resource = resource(s.query());
      if (!isSearch(s.query())) {
        counts.put(resource, counts.getOrDefault(resource, 0) + 1);
      }
    }
    return counts;
  }

  private Map<String, Integer> searchCounts() {
    Map<String, Integer> counts = new TreeMap<>();
    for (final Summary s : summaries) {
      String resource = resource(s.query());
      if (isSearch(s.query())) {
        counts.put(resource, counts.getOrDefault(resource, 0) + 1);
      }
    }
    return counts;
  }
}
