package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.uscorer4.api.resources.Patient;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type Patient.class or Bundle.class. Extract ICN(s) from
 * these payloads with the provided function. This will lead to populating the X-VA-INCLUDES-ICN
 * header.
 */
@ControllerAdvice
public class R4PatientIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<Patient, Patient.Bundle>builder()
          .type(Patient.class)
          .bundleType(Patient.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.of(body.id()))
          .build();
}
