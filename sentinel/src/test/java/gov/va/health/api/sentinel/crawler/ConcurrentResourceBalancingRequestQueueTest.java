package gov.va.health.api.sentinel.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public final class ConcurrentResourceBalancingRequestQueueTest {
  ConcurrentResourceBalancingRequestQueue q = new ConcurrentResourceBalancingRequestQueue();

  @Test(expected = IllegalStateException.class)
  public void exceptionIsThrownWhenAttemptingToGetNextFromEmptyQueue() {
    q.add("x");
    q.next();
    q.next();
  }

  @Test(expected = IllegalStateException.class)
  public void exceptionIsThrownWhenAttemptingToGetNextQueueThatWasNeverUsed() {
    q.next();
  }

  @Test
  public void hasNextReturnsFalseForEmptyQueue() {
    assertThat(q.hasNext()).isFalse();
    q.add("x");
    q.next();
    assertThat(q.hasNext()).isFalse();
  }

  @Test
  public void itemsAreRemovedFromQueueInOrderOfAddition() {
    q.add("a");
    q.add("b");
    q.add("c");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("a");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("b");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("c");
    assertThat(q.hasNext()).isFalse();
  }

  @Test
  public void duplicateItemsIgnored() {
    q.add("a");
    q.add("b");
    q.add("c");
    // ignored
    q.add("a");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("a");
    // ignored
    q.add("a");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("b");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("c");
    assertThat(q.hasNext()).isFalse();
  }

  @Test
  public void asdf() {
	  // PETERTODO
    q.add("foo.bar/api/apple/1");
    q.next();
    
    q.add("foo.bar/api/apple/2");
    q.add("foo.bar/api/banana/1");
    q.add("foo.bar/api/apple/3");
    System.out.println(q.next());
  }
}
