package gov.va.api.health.dataquery.service.controller.medicationdispense;

import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.resources.MedicationDispense;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type MedicationDispense.class or Bundle.class. Extract
 * ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class MedicationDispenseIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<MedicationDispense, MedicationDispense.Bundle>builder()
          .type(MedicationDispense.class)
          .bundleType(MedicationDispense.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.ofNullable(Dstu2Transformers.asReferenceId(body.patient())))
          .build();
}
