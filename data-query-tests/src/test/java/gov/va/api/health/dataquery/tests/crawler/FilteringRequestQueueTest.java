package gov.va.api.health.dataquery.tests.crawler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;

public class FilteringRequestQueueTest {

  @Test
  public void addOnlyMatchingUrl() {
    RequestQueue mock = mock(RequestQueue.class);
    FilteringRequestQueue filter =
        FilteringRequestQueue.builder()
            .allowQueryUrlPattern(".*magic.*")
            .requestQueue(mock)
            .build();
    filter.add("http://nope.com");
    filter.add("http://ok.com/magic/stuff");
    filter.add("http://also.com/nope");
    filter.add("http://magic.com/ok");
    filter.hasNext();
    filter.next();
    verify(mock).add("http://ok.com/magic/stuff");
    verify(mock).add("http://magic.com/ok");
    verify(mock).hasNext();
    verify(mock).next();
    verifyNoMoreInteractions(mock);
  }
}
