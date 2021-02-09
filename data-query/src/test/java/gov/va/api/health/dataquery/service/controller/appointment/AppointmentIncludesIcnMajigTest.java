package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Appointment;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AppointmentIncludesIcnMajigTest {

  @Test
  public void r4() {
    ExtractIcnValidator.builder()
        .majig(new R4AppointmentIncludesIcnMajig())
        .body(
            Appointment.builder()
                .id("123")
                .participant(
                    List.of(
                        Appointment.Participant.builder()
                            .actor(
                                Reference.builder().reference("Patient/1010101010V666666").build())
                            .build()))
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
