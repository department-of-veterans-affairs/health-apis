package gov.va.health.api.sentinel.crawler;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.health.api.sentinel.crawler.ResourceDiscovery.isSearch;
import static gov.va.health.api.sentinel.crawler.ResourceDiscovery.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe implementation of {@link RequestQueue} that gives higher priority to less-visited
 * resources.
 */
@Slf4j
public class ConcurrentResourceBalancingRequestQueue implements RequestQueue {
  /** All items that have ever been in the queue. This prevents duplicate entries. */
  private final Collection<String> allRequests = new HashSet<>();

  /** The current items in the queue. */
  private final Collection<String> requests = new HashSet<>();

  /**
   * An indication of how often a resource has been dequeued. Higher priority is given to resources
   * with lower scores.
   */
  private final Map<String, Integer> resourceScores = new HashMap<>();

  @Override
  public synchronized void add(@NonNull String url) {
    if (!allRequests.contains(url)) {
      requests.add(url);
      allRequests.add(url);
      log.info("Enqueued {}.", url);
    }
  }

  @Override
  public boolean hasNext() {
    return !requests.isEmpty();
  }

  @Override
  public synchronized String next() {
    checkState(hasNext());

    // First consider the requests with the lowest scores.
    // Then favor searches over reads.
    // Natural string order is tiebreaker.
    final String next =
        Collections.min(
            requests,
            Comparator.<String>comparingInt(str -> resourceScore(str))
                .thenComparing(
                    (left, right) -> -1 * Boolean.compare(isSearch(left), isSearch(right)))
                .thenComparing(Comparator.naturalOrder()));
    log.info("Dequeued {}.", next);
    requests.remove(next);

    if (isSearch(next)) {
      resourceScores.put(resource(next), resourceScore(next) + 15);
    } else {
      resourceScores.put(resource(next), resourceScore(next) + 1);
    }
    return next;
  }

  private int resourceScore(String url) {
    return resourceScores.getOrDefault(resource(url), 0);
  }
}
