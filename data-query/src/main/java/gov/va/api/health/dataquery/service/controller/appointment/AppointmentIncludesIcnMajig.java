package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.resources.Appointment;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Appointment.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class AppointmentIncludesIcnMajig {
  @Delegate
  private final IncludesIcnMajig<Appointment, Appointment.Bundle> delegate =
      IncludesIcnMajig.<Appointment, Appointment.Bundle>builder()
          .type(Appointment.class)
          .bundleType(Appointment.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(
              body ->
                  Stream.ofNullable(
                      body.participant() == null || body.participant().isEmpty()
                          ? null
                          : body.participant()
                              .stream()
                              .map(p -> p.actor())
                              .filter(r -> r.reference().contains("Patient"))
                              .map(i -> Dstu2Transformers.asReferenceId(i))
                              .collect(Collectors.joining(","))))
          .build();
}
