package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dstu2.api.resources.Location;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Location.class or Bundle.class. Extract ICN(s) from
 * these payloads with the provided function. This will lead to populating the X-VA-INCLUDES-ICN
 * header.
 */
@ControllerAdvice
public class LocationIncludesIcnMajig
    extends AbstractIncludesIcnMajig<Location, Location.Entry, Location.Bundle> {
  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public LocationIncludesIcnMajig() {
    super(Location.class, Location.Bundle.class, body -> Stream.empty());
  }
}
