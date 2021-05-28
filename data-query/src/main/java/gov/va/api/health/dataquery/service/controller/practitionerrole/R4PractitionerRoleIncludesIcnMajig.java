package gov.va.api.health.dataquery.service.controller.practitionerrole;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** How to retrieve patient icn from a practitioner role response (Spoiler Alert: you dont). */
@ControllerAdvice
public class R4PractitionerRoleIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<PractitionerRole, PractitionerRole.Bundle>builder()
          .type(PractitionerRole.class)
          .bundleType(PractitionerRole.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.empty())
          .build();
}
