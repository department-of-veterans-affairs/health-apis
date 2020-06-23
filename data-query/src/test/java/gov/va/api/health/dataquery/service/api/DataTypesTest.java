package gov.va.api.health.dataquery.service.api;

import gov.va.api.health.dataquery.service.api.Dstu2DataQueryService.SearchFailed;
import gov.va.api.health.dataquery.service.api.Dstu2DataQueryService.UnknownResource;
import org.junit.jupiter.api.Test;

public class DataTypesTest {
  @SuppressWarnings({"ThrowableNotThrown", "unused"})
  @Test
  public void exceptionConstructors() {
    new UnknownResource("some id");
    new SearchFailed("some id", "some reason");
  }
}
