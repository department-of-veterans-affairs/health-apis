package gov.va.health.api.sentinel.crawler;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public final class ConcurrentResourceBalancingRequestQueueTest {
  ConcurrentResourceBalancingRequestQueue q = new ConcurrentResourceBalancingRequestQueue();

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
  public void prioritizeSearchOverRead() {
    q.add("foo/api/Immunization/abc");
    q.add("foo/api/Immunization");
    q.add("foo/api/Immunization?patient=bobnelson");
    assertThat(q.next()).isEqualTo("foo/api/Immunization?patient=bobnelson");
    assertThat(q.next()).isEqualTo("foo/api/Immunization");
    assertThat(q.next()).isEqualTo("foo/api/Immunization/abc");
  }

  @Test
  public void searchesAreWeightedMoreHeavilyThanReads() {
    final List<String> urls =
        asList(
            "foo/api/AllergyIntolerance/1",
            "foo/api/Condition/1",
            "foo/api/Condition?patient=bobnelson",
            "foo/api/Condition",
            "foo/api/Location/3",
            "foo/api/Encounter/2",
            "foo/api/Location/1",
            "foo/api/Encounter/1",
            "foo/api/Location/2");
    for (String url : urls) {
      q.add(url);
    }
    assertThat(q.next()).isEqualTo("foo/api/Condition?patient=bobnelson");

    // As a result of the condition search, add page 2 and several condition reads.
    q.add("foo/api/Condition?patient=bobnelson&page=2");
    q.add("foo/api/Condition/2");
    q.add("foo/api/Condition/3");
    q.add("foo/api/Condition/4");

    // Reads for all of the other resources should now be exhausted before returning to condition.
    final List<String> expectedOrder =
        asList(
            "foo/api/AllergyIntolerance/1",
            "foo/api/Encounter/1",
            "foo/api/Location/1",
            "foo/api/Encounter/2",
            "foo/api/Location/2",
            "foo/api/Location/3",
            "foo/api/Condition?patient=bobnelson&page=2",
            "foo/api/Condition",
            "foo/api/Condition/1",
            "foo/api/Condition/2",
            "foo/api/Condition/3",
            "foo/api/Condition/4");
    for (String expectedUrl : expectedOrder) {
      assertThat(q.next()).isEqualTo(expectedUrl);
    }
    assertThat(q.hasNext()).isFalse();
  }
}
