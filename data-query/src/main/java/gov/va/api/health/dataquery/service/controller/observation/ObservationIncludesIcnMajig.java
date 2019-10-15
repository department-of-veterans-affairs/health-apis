package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Transformers;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Observation.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class ObservationIncludesIcnMajig
    extends AbstractIncludesIcnMajig<Observation, Observation.Entry, Observation.Bundle> {
  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public ObservationIncludesIcnMajig() {
    super(
        Observation.class,
        Observation.Bundle.class,
        (body) -> Stream.ofNullable(Transformers.asReferenceId(body.subject())));
  }
}
