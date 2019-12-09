package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Transformers;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Immunization.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class Dstu2ImmunizationIncludesIcnMajig
    extends AbstractIncludesIcnMajig<Immunization, Immunization.Entry, Immunization.Bundle> {
  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public Dstu2ImmunizationIncludesIcnMajig() {
    super(
        Immunization.class,
        Immunization.Bundle.class,
        body -> Stream.ofNullable(Transformers.asReferenceId(body.patient())));
  }
}
