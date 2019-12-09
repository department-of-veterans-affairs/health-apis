package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Bundle;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Entry;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type AllergyIntolerance.class or Bundle.class. Extract
 * ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class Dstu2AllergyIntoleranceIncludesIcnMajig
    extends AbstractIncludesIcnMajig<AllergyIntolerance, Entry, Bundle> {

  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public Dstu2AllergyIntoleranceIncludesIcnMajig() {
    super(
        AllergyIntolerance.class,
        Bundle.class,
        (body) -> Stream.ofNullable(Dstu2Transformers.asReferenceId(body.patient())));
  }
}
