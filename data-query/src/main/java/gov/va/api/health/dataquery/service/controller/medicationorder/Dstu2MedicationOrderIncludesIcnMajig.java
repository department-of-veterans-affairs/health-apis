package gov.va.api.health.dataquery.service.controller.medicationorder;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type MedicationOrder.class or Bundle.class. Extract
 * ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class Dstu2MedicationOrderIncludesIcnMajig {
  @Delegate
  private final IncludesIcnMajig<MedicationOrder, MedicationOrder.Bundle> delegate =
      IncludesIcnMajig.<MedicationOrder, MedicationOrder.Bundle>builder()
          .type(MedicationOrder.class)
          .bundleType(MedicationOrder.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.ofNullable(Dstu2Transformers.asReferenceId(body.patient())))
          .build();
}
