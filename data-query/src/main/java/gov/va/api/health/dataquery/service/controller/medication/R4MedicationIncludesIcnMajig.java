package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.uscorer4.api.resources.Medication;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.stream.Stream;

/**
 * Intercept all RequestMapping payloads of Type Medication.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class R4MedicationIncludesIcnMajig implements ResponseBodyAdvice<Object> {
    @Delegate
    private final ResponseBodyAdvice<Object> delegate =
            IncludesIcnMajig.<Medication, Medication.Bundle>builder()
                .type(Medication.class)
                .bundleType(Medication.Bundle.class)
                .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
                .extractIcns(body -> Stream.empty())
                .build();
}
