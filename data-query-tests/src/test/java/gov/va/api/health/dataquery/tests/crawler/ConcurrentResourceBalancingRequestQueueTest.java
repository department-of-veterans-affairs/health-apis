package gov.va.api.health.dataquery.tests.crawler;

import static gov.va.api.health.dataquery.tests.EnvironmentAssumptions.assumeLocal;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class ConcurrentResourceBalancingRequestQueueTest {
  ConcurrentResourceBalancingRequestQueue q = new ConcurrentResourceBalancingRequestQueue();

  @Test
  public void balanceResources() {
    final List<String> urls =
        asList(
            "foo/api/AllergyIntolerance/1",
            "foo/api/Condition/1",
            "foo/api/Condition?patient=bobnelson",
            "foo/api/Malformed",
            "foo/api/Location/3",
            "foo/api/Organization/2",
            "foo/api/Location/1",
            "foo/api/Organization/1",
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
            "foo/api/Location/1",
            "foo/api/Malformed",
            "foo/api/Organization/1",
            "foo/api/Location/2",
            "foo/api/Organization/2",
            "foo/api/Location/3",
            "foo/api/Condition?patient=bobnelson&page=2",
            "foo/api/Condition/1",
            "foo/api/Condition/2",
            "foo/api/Condition/3",
            "foo/api/Condition/4");
    for (String expectedUrl : expectedOrder) {
      assertThat(q.next()).isEqualTo(expectedUrl);
    }
    assertThat(q.hasNext()).isFalse();
  }

  @Test
  public void duplicateItemsIgnored() {
    q.add("foo/api/AllergyIntolerance/1");
    q.add("foo/api/Condition/2");
    q.add("foo/api/Location/3");
    // ignored
    q.add("foo/api/AllergyIntolerance/1");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("foo/api/AllergyIntolerance/1");
    // ignored
    q.add("foo/api/AllergyIntolerance/1");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("foo/api/Condition/2");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("foo/api/Location/3");
    assertThat(q.hasNext()).isFalse();
  }

  @Test
  public void exceptionIsThrownWhenAttemptingToAddMalformedUrlToQueue() {
    assertThrows(IllegalArgumentException.class, () -> q.add("x"));
  }

  @Test
  public void exceptionIsThrownWhenAttemptingToGetNextFromEmptyQueue() {
    assertThrows(
        IllegalStateException.class,
        () -> {
          q.add("foo/api/AllergyIntolerance/1");
          q.next();
          q.next();
        });
  }

  @Test
  public void exceptionIsThrownWhenAttemptingToGetNextQueueThatWasNeverUsed() {
    assertThrows(IllegalStateException.class, () -> q.next());
  }

  @Test
  public void hasNextReturnsFalseForEmptyQueue() {
    assertThat(q.hasNext()).isFalse();
    q.add("foo/api/AllergyIntolerance");
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
}
