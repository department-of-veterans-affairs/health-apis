package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Condition.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class Dstu2ConditionIncludesIcnMajig
    extends IncludesIcnMajig<Condition, Condition.Entry, Condition.Bundle> {
  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public Dstu2ConditionIncludesIcnMajig() {
    super(
        Condition.class,
        Condition.Bundle.class,
        body -> Stream.ofNullable(Dstu2Transformers.asReferenceId(body.patient())));
  }
}
