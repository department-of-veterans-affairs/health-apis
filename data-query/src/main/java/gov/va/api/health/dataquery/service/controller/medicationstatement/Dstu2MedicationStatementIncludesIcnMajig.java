package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static gov.va.api.health.dataquery.service.controller.Transformers.asReferenceId;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type MedicationStatement.class or Bundle.class. Extract
 * ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class Dstu2MedicationStatementIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<MedicationStatement, MedicationStatement.Bundle>builder()
          .type(MedicationStatement.class)
          .bundleType(MedicationStatement.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.ofNullable(asReferenceId(body.patient())))
          .build();
}
