package gov.va.health.api.sentinel.crawler;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/** Provides a thread safe implementation of the request queue. */
@Slf4j
public class ConcurrentResourceBalancingRequestQueue implements RequestQueue {
  private static final Pattern URL_RESOURCE_PATTERN = Pattern.compile(".*/api/([^/^?]+).*");

  /** All items that have ever been in the queue. This prevents duplicate entries. */
  private final Collection<String> allRequests = new HashSet<>();

  /** The current items in the queue. */
  private final Collection<String> requests = new HashSet<>();

  private final Multiset<String> resources = HashMultiset.create();

  private static String resource(String url) {
    // PETERTODO unit tests for this
    Matcher matcher = URL_RESOURCE_PATTERN.matcher(url);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      log.warn("Failed to extract resource from url '{}'.", url);
      return url;
    }
  }

  @Override
  public synchronized void add(@NonNull String url) {
    if (!allRequests.contains(url)) {
      requests.add(url);
      allRequests.add(url);
      log.info("Added {}", url);
    }
  }

  @Override
  public boolean hasNext() {
    return !requests.isEmpty();
  }

  @Override
  public synchronized String next() {
    // PETERTODO confirm that regular priority queue doesn't work
    checkState(hasNext());
    final String next =
        Collections.min(
            requests,
            Comparator.<String>comparingInt(str -> resources.count(resource(str)))
                .thenComparing(Comparator.naturalOrder()));
    requests.remove(next);
    resources.add(resource(next));
    return next;
  }
}
