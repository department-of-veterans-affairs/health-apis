package gov.va.api.health.dataquery.service.controller.medicationstatement;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type MedicationStatement.class or Bundle.class. Extract
 * ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class MedicationStatementIncludesIcnMajig
    extends AbstractIncludesIcnMajig<
        MedicationStatement, MedicationStatement.Entry, MedicationStatement.Bundle> {
  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public MedicationStatementIncludesIcnMajig() {
    super(
        MedicationStatement.class,
        MedicationStatement.Bundle.class,
        body -> Stream.ofNullable(Dstu2Transformers.asReferenceId(body.patient())));
  }
}
