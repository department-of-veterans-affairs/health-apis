package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Transformers;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Intercept all RequestMapping payloads of Type Immunization.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class ImmunizationIncludesIcnMajig extends AbstractIncludesIcnMajig<Immunization, Immunization.Entry, Immunization.Bundle> {
    /** Converts the reference to a Datamart Reference to pull out the patient id. */
    public ImmunizationIncludesIcnMajig() {
        super(
                Immunization.class,
                Immunization.Bundle.class,
                body -> Stream.of(Transformers.asReferenceId(body.patient())));
    }
}
