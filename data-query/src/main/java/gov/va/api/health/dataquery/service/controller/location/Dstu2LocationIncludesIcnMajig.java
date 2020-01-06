package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class Dstu2LocationIncludesIcnMajig
    extends AbstractIncludesIcnMajig<Location, Location.Entry, Location.Bundle> {
  public Dstu2LocationIncludesIcnMajig() {
    super(Location.class, Location.Bundle.class, (body) -> Stream.empty());
  }
}
