package gov.va.api.health.dataquery.tests.crawler;

import java.util.regex.Pattern;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/** This request queue wraps another and provides filter capability. */
@Slf4j
public class FilteringRequestQueue implements RequestQueue {

  private final Pattern allowQueryUrlPattern;

  private final RequestQueue requestQueue;

  @Builder
  private FilteringRequestQueue(String allowQueryUrlPattern, RequestQueue requestQueue) {
    this.requestQueue = requestQueue;
    this.allowQueryUrlPattern = Pattern.compile(allowQueryUrlPattern);
  }

  @Override
  public void add(String url) {
    if (allowQueryUrlPattern.matcher(url).matches()) {
      requestQueue.add(url);
    } else {
      log.info("Ignoring {}", url);
    }
  }

  @Override
  public boolean hasNext() {
    return requestQueue.hasNext();
  }

  @Override
  public String next() {
    return requestQueue.next();
  }
}
