package gov.va.api.health.dataquery.service.controller.procedure;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Procedure.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class ProcedureIncludesIcnMajig
    extends IncludesIcnMajig<Procedure, Procedure.Entry, Procedure.Bundle> {
  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public ProcedureIncludesIcnMajig() {
    super(
        Procedure.class,
        Procedure.Bundle.class,
        body -> Stream.ofNullable(Dstu2Transformers.asReferenceId(body.subject())));
  }
}
