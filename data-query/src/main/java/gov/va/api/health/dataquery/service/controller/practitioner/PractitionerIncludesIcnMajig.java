package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Practitioner.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class PractitionerIncludesIcnMajig {
  @Delegate
  private final IncludesIcnMajig<Practitioner, Practitioner.Bundle> delegate =
      IncludesIcnMajig.<Practitioner, Practitioner.Bundle>builder()
          .type(Practitioner.class)
          .bundleType(Practitioner.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.empty())
          .build();
}
