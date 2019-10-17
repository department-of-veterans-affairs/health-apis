package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Transformers;
import gov.va.api.health.dstu2.api.resources.Appointment;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Intercept all RequestMapping payloads of Type Appointment.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class AppointmentIncludesIcnMajig
    extends AbstractIncludesIcnMajig<Appointment, Appointment.Entry, Appointment.Bundle> {
  /** Converts the reference to a Datamart Reference to pull out the patient id. */
  public AppointmentIncludesIcnMajig() {
    super(
        Appointment.class,
        Appointment.Bundle.class,
        body ->
            Stream.ofNullable(
                body.participant() == null || body.participant().isEmpty()
                    ? null
                    : body.participant()
                        .stream()
                        .map(p -> p.actor())
                        .filter(r -> r.reference().contains("Patient"))
                        .map(i -> Transformers.asReferenceId(i))
                        .collect(Collectors.joining(","))));
  }
}
