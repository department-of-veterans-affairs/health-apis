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

/** Provides a thread safe implementation of the request queue. */
@Slf4j
public class ConcurrentResourceBalancingRequestQueue implements RequestQueue {
  /** All items that have ever been in the queue. This prevents duplicate entries. */
  private final Collection<String> allRequests = new HashSet<>();

  /** The current items in the queue. */
  private final Collection<String> requests = new HashSet<>();

  private final Map<String, Integer> scores = new HashMap<>();

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

    final String next =
        Collections.min(
            requests,
            Comparator.<String>comparingInt(str -> score(str))
                .thenComparing(
                    (left, right) -> -1 * Boolean.compare(isSearch(left), isSearch(right)))
                .thenComparing(Comparator.naturalOrder()));
    log.info("Dequeued {}.", next);
    requests.remove(next);

    if (isSearch(next)) {
      scores.put(resource(next), score(next) + 15);
    } else {
      scores.put(resource(next), score(next) + 1);
    }
    return next;
  }

  private int score(String url) {
    return scores.getOrDefault(resource(url), 0);
  }
}
