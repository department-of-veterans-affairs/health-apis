package gov.va.health.api.sentinel.crawler;

import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.Builder;

/**
 * The url replacement queue replaces the default url with a provided url to ensure the references
 * are correct when crawling.
 */
public class UrlReplacementRequestQueue implements RequestQueue {
  private final String replaceUrl;

  private final String withUrl;

  private final RequestQueue requestQueue;

  @Builder
  UrlReplacementRequestQueue(String replaceUrl, String withUrl, RequestQueue requestQueue) {
    if (isBlank(withUrl)) {
      throw new IllegalStateException("withUrl not specified.");
    }
    if (isBlank(replaceUrl)) {
      throw new IllegalStateException("replaceUrl not specified.");
    }
    this.replaceUrl = replaceUrl.endsWith("/") ? replaceUrl : replaceUrl + "/";
    this.withUrl = withUrl.endsWith("/") ? withUrl : withUrl + "/";
    this.requestQueue = requestQueue;
  }

  @Override
  public void add(String url) {
    requestQueue.add(url.replace(replaceUrl, withUrl));
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
