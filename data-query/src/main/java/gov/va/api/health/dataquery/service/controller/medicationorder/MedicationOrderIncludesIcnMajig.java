package gov.va.api.health.dataquery.service.controller.medicationorder;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Transformers;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type MedicationOrder.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class MedicationOrderIncludesIcnMajig
        extends AbstractIncludesIcnMajig<MedicationOrder, MedicationOrder.Entry, MedicationOrder.Bundle> {
    /** Converts the reference to a Datamart Reference to pull out the patient id. */
    public MedicationOrderIncludesIcnMajig() {
        super(
                MedicationOrder.class,
                MedicationOrder.Bundle.class,
                body -> Stream.ofNullable(Transformers.asReferenceId(body.patient())));
    }
}