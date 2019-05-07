package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.dataquery.service.api.DataQueryService.SearchFailed;
import gov.va.api.health.dataquery.service.api.DataQueryService.UnknownResource;
import org.junit.Test;

public class DataTypesTest {

  @SuppressWarnings("ThrowableNotThrown")
  @Test
  public void exceptionConstructors() {
    new UnknownResource("some id");
    new SearchFailed("some id", "some reason");
  }
}
