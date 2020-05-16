package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static gov.va.api.health.dataquery.service.controller.Transformers.asReferenceId;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.uscorer4.api.resources.AllergyIntolerance;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of type AllergyIntolerance or AllergyIntoleranceBundle.
 * Extract ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class R4AllergyIntoleranceIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<AllergyIntolerance, AllergyIntolerance.Bundle>builder()
          .type(AllergyIntolerance.class)
          .bundleType(AllergyIntolerance.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.ofNullable(asReferenceId(body.patient())))
          .build();
}
