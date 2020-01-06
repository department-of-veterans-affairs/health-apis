package gov.va.api.health.dataquery.service.controller.medicationdispense;

import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.resources.MedicationDispense;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type MedicationDispense.class or Bundle.class. Extract
 * ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class MedicationDispenseIncludesIcnMajig
    extends IncludesIcnMajig<
        MedicationDispense, MedicationDispense.Entry, MedicationDispense.Bundle> {
  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public MedicationDispenseIncludesIcnMajig() {
    super(
        MedicationDispense.class,
        MedicationDispense.Bundle.class,
        body -> Stream.ofNullable(Dstu2Transformers.asReferenceId(body.patient())));
  }
}
