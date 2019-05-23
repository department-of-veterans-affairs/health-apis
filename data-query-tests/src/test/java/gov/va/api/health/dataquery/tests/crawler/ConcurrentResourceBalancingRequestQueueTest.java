package gov.va.api.health.dataquery.tests.crawler;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.categories.Local;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Local.class)
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
            "foo/api/Malformed",
            "foo/api/Encounter/2",
            "foo/api/Location/2",
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
    q.add("foo/api/Encounter/3");
    // ignored
    q.add("foo/api/AllergyIntolerance/1");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("foo/api/AllergyIntolerance/1");
    // ignored
    q.add("foo/api/AllergyIntolerance/1");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("foo/api/Condition/2");
    assertThat(q.hasNext()).isTrue();
    assertThat(q.next()).isEqualTo("foo/api/Encounter/3");
    assertThat(q.hasNext()).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void exceptionIsThrownWhenAttemptingToAddMalformedUrlToQueue() {
    q.add("x");
  }

  @Test(expected = IllegalStateException.class)
  public void exceptionIsThrownWhenAttemptingToGetNextFromEmptyQueue() {
    q.add("foo/api/AllergyIntolerance/1");
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
