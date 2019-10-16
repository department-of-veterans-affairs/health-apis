package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.Medication.Bundle;
import gov.va.api.health.argonaut.api.resources.Medication.Entry;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Medication.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class MedicationIncludesIcnMajig
    extends AbstractIncludesIcnMajig<Medication, Entry, Bundle> {

  /** Returns empty to send the value "NONE" back to Kong. */
  public MedicationIncludesIcnMajig() {
    super(Medication.class, Bundle.class, (body) -> Stream.empty());
  }
}
