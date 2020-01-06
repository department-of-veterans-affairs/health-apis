package gov.va.api.health.dataquery.service.controller.organization;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.resources.Organization;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Organization.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class OrganizationIncludesIcnMajig {
  @Delegate
  private final IncludesIcnMajig<Organization, Organization.Bundle> delegate =
      IncludesIcnMajig.<Organization, Organization.Bundle>builder()
          .type(Organization.class)
          .bundleType(Organization.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.empty())
          .build();
}
