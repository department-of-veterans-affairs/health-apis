package gov.va.api.health.dataquery.tests.crawler;

/**
 * The request queue manages fully qualified URLs for reading Data Query resources. It prohibits
 * entries from being added multiple times. Implementations should make provisions for
 * multi-threaded environments.
 */
public interface RequestQueue {
  /** Add a fully qualified URL if it has not been previously added. */
  void add(String url);

  /** Return true if there is at least one more item in the queue. */
  boolean hasNext();

  /**
   * Return the next item in the queue if available. If the queue has been depleted, an
   * IllegalStateException will be thrown.
   */
  String next();
}
