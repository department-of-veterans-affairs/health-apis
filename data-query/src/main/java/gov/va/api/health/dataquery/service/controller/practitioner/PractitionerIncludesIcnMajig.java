package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.api.health.dstu2.api.resources.Practitioner.Bundle;
import gov.va.api.health.dstu2.api.resources.Practitioner.Entry;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Practitioner.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class PractitionerIncludesIcnMajig extends IncludesIcnMajig<Practitioner, Entry, Bundle> {

  /** Returns empty to send the value "NONE" back to Kong. */
  public PractitionerIncludesIcnMajig() {
    super(Practitioner.class, Bundle.class, (body) -> Stream.empty());
  }
}
