package gov.va.api.health.dataquery.tests.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Local.class)
public class UrlReplacementRequestQueueTest {
  UrlReplacementRequestQueue rq =
      UrlReplacementRequestQueue.builder()
          .replaceUrl("https://dev-api.va.gov/services/argonaut/v0/")
          .withUrl("https://staging-argonaut.lighthouse.va.gov/api/")
          .requestQueue(new ConcurrentResourceBalancingRequestQueue())
          .build();

  @Test(expected = IllegalArgumentException.class)
  public void emptyBaseUrlThrowsIllegalStateException() {
    UrlReplacementRequestQueue emptyBaseUrl =
        UrlReplacementRequestQueue.builder()
            .replaceUrl("")
            .withUrl("https://staging-argonaut.lighthouse.va.gov/api/")
            .requestQueue(new ConcurrentResourceBalancingRequestQueue())
            .build();
    emptyBaseUrl.add("Empty forceUrl");
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyForceUrlThrowsIllegalStateException() {
    UrlReplacementRequestQueue emptyBaseUrl =
        UrlReplacementRequestQueue.builder()
            .replaceUrl("https://dev-api.va.gov/services/argonaut/v0/")
            .withUrl("")
            .requestQueue(new ConcurrentResourceBalancingRequestQueue())
            .build();
    emptyBaseUrl.add("Empty forceUrl");
  }

  @Test(expected = IllegalStateException.class)
  public void exceptionIsThrownWhenAttemptingToGetNextFromEmptyQueue() {
    rq.add("x");
    rq.next();
    rq.next();
  }

  @Test(expected = IllegalStateException.class)
  public void exceptionIsThrownWhenAttemptingToGetNextQueueThatWasNeverUsed() {
    rq.next();
  }

  @Test
  public void forceUrlReplacesBaseUrl() {
    rq.add(
        "https://dev-api.va.gov/services/argonaut/v0/AllergyIntolerance/3be00408-b0ff-598d-8ba1-1e0bbfb02b99");
    String expected =
        "https://staging-argonaut.lighthouse.va.gov/api/AllergyIntolerance/3be00408-b0ff-598d-8ba1-1e0bbfb02b99";
    assertThat(rq.next()).isEqualTo(expected);
  }

  @Test
  public void hasNextReturnsFalseForEmptyQueue() {
    assertThat(rq.hasNext()).isFalse();
    rq.add("x");
    rq.next();
    assertThat(rq.hasNext()).isFalse();
  }

  @Test
  public void itemsAreRemovedFromQueueInOrderOfAddition() {
    rq.add("a");
    rq.add("b");
    rq.add("c");
    assertThat(rq.hasNext()).isTrue();
    assertThat(rq.next()).isEqualTo("a");
    assertThat(rq.hasNext()).isTrue();
    assertThat(rq.next()).isEqualTo("b");
    assertThat(rq.hasNext()).isTrue();
    assertThat(rq.next()).isEqualTo("c");
    assertThat(rq.hasNext()).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullBaseUrlThrowsIllegalStateException() {
    UrlReplacementRequestQueue emptyBaseUrl =
        UrlReplacementRequestQueue.builder()
            .replaceUrl(null)
            .withUrl("https://staging-argonaut.lighthouse.va.gov/api/")
            .requestQueue(new ConcurrentResourceBalancingRequestQueue())
            .build();
    emptyBaseUrl.add("Empty baseUrl");
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullForceUrlThrowsIllegalStateException() {
    UrlReplacementRequestQueue emptyBaseUrl =
        UrlReplacementRequestQueue.builder()
            .replaceUrl("https://dev-api.va.gov/services/argonaut/v0/")
            .withUrl(null)
            .requestQueue(new ConcurrentResourceBalancingRequestQueue())
            .build();
    emptyBaseUrl.add("Empty forceUrl");
  }

  @Test
  public void replaceUrlAppendsSlash() {
    UrlReplacementRequestQueue appendToReplaceUrlRq =
        UrlReplacementRequestQueue.builder()
            .replaceUrl("https://dev-api.va.gov/services/argonaut/v0")
            .withUrl("https://staging-argonaut.lighthouse.va.gov/api/")
            .requestQueue(new ConcurrentResourceBalancingRequestQueue())
            .build();
    appendToReplaceUrlRq.add(
        "https://dev-api.va.gov/services/argonaut/v0/AllergyIntolerance/3be00408-b0ff-598d-8ba1-1e0bbfb02b99");
    String expected =
        "https://staging-argonaut.lighthouse.va.gov/api/AllergyIntolerance/3be00408-b0ff-598d-8ba1-1e0bbfb02b99";
    assertThat(appendToReplaceUrlRq.next()).isEqualTo(expected);
  }

  @Test
  public void theSameItemCannotBeAddedToTheQueueTwice() {
    rq.add("a");
    rq.add("b");
    rq.add("c");
    // ignored
    rq.add("a");
    assertThat(rq.hasNext()).isTrue();
    assertThat(rq.next()).isEqualTo("a");
    // ignored
    rq.add("a");
    assertThat(rq.hasNext()).isTrue();
    assertThat(rq.next()).isEqualTo("b");
    assertThat(rq.hasNext()).isTrue();
    assertThat(rq.next()).isEqualTo("c");
    assertThat(rq.hasNext()).isFalse();
  }

  @Test
  public void withUrlAppendsSlash() {
    UrlReplacementRequestQueue appendToWithUrlRq =
        UrlReplacementRequestQueue.builder()
            .replaceUrl("https://dev-api.va.gov/services/argonaut/v0/")
            .withUrl("https://staging-argonaut.lighthouse.va.gov/api")
            .requestQueue(new ConcurrentResourceBalancingRequestQueue())
            .build();
    appendToWithUrlRq.add(
        "https://dev-api.va.gov/services/argonaut/v0/AllergyIntolerance/3be00408-b0ff-598d-8ba1-1e0bbfb02b99");
    String expected =
        "https://staging-argonaut.lighthouse.va.gov/api/AllergyIntolerance/3be00408-b0ff-598d-8ba1-1e0bbfb02b99";
    assertThat(appendToWithUrlRq.next()).isEqualTo(expected);
  }
}
