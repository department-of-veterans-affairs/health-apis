package gov.va.api.health.dataquery.service.controller.organization;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.resources.Organization;
import gov.va.api.health.dstu2.api.resources.Organization.Bundle;
import gov.va.api.health.dstu2.api.resources.Organization.Entry;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Organization.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class OrganizationIncludesIcnMajig
    extends IncludesIcnMajig<Organization, Entry, Bundle> {

  /** Returns empty to send the value "NONE" back to Kong. */
  public OrganizationIncludesIcnMajig() {
    super(Organization.class, Bundle.class, (body) -> Stream.empty());
  }
}
