package gov.va.api.health.dataquery.service.controller.appointment;

import static gov.va.api.health.dataquery.service.controller.Transformers.asReferenceId;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Appointment;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type Appointment.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class R4AppointmentIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<Appointment, Appointment.Bundle>builder()
          .type(Appointment.class)
          .bundleType(Appointment.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(
              body -> {
                var participant =
                    body.participant().stream()
                        .filter(p -> p.actor().reference().contains("Patient"))
                        .findFirst();
                return participant
                    .map(value -> Stream.ofNullable(asReferenceId(value.actor())))
                    .orElseGet(Stream::empty);
              })
          .build();
}
