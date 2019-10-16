package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.resources.Location;
import java.util.List;
import org.junit.Test;

public class LocationIncludesIcnMajigTest {

  @Test
  public void extractIcs() {
    ExtractIcnValidator.<LocationIncludesIcnMajig, Location>builder()
        .majig(new LocationIncludesIcnMajig())
        .body(Location.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }
}
