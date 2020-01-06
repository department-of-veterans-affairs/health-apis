package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Appointment;
import java.util.List;
import org.junit.Test;

public class Dstsu2AppointmentIncludesIcnMajigTest {

  @Test
  public void extractIcns() {
    ExtractIcnValidator.<Dstsu2AppointmentIncludesIcnMajig, Appointment>builder()
        .majig(new Dstsu2AppointmentIncludesIcnMajig())
        .body(
            Appointment.builder()
                .id("123")
                .participant(
                    List.of(
                        Appointment.Participant.builder()
                            .actor(Reference.builder().reference("Patient/666V666").build())
                            .build(),
                        Appointment.Participant.builder()
                            .actor(Reference.builder().reference("Patient/777V777").build())
                            .build(),
                        Appointment.Participant.builder()
                            .actor(Reference.builder().reference("Practitioner/n074n1cn").build())
                            .build()))
                .build())
        .expectedIcns(List.of("666V666", "777V777"))
        .build()
        .assertIcn();
  }

  @Test
  public void noReferences() {
    ExtractIcnValidator.<Dstsu2AppointmentIncludesIcnMajig, Appointment>builder()
        .majig(new Dstsu2AppointmentIncludesIcnMajig())
        .body(Appointment.builder().id("123").build())
        .expectedIcns(List.of("NONE"))
        .build()
        .assertIcn();
  }
}
