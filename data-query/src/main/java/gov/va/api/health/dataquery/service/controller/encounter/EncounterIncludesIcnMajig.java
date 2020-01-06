package gov.va.api.health.dataquery.service.controller.encounter;

import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.resources.Encounter;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type Encounter.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class EncounterIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final IncludesIcnMajig<Encounter, Encounter.Bundle> delegate =
      IncludesIcnMajig.<Encounter, Encounter.Bundle>builder()
          .type(Encounter.class)
          .bundleType(Encounter.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.ofNullable(Dstu2Transformers.asReferenceId(body.patient())))
          .build();
}
