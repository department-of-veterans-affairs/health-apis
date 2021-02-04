package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppointmentSamples {

  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    public DatamartAppointment appointment() {
      return DatamartAppointment.builder()
          .cdwId("1600021515962")
          .status(Optional.of("NT-CO"))
          .cancelationReason(Optional.of("OTHER"))
          .serviceCategory(Optional.of("SURGERY"))
          .serviceType("OTOLARYNGOLOGY/ENT")
          .specialty(Optional.of("SURGERY"))
          .appointmentType(Optional.of("WALKIN"))
          .description(Optional.of("Walk-In Visit"))
          .start(Optional.of(Instant.parse("2020-11-25T08:00:00Z")))
          .end(Optional.of(Instant.parse("2020-11-25T08:20:00Z")))
          .minutesDuration(Optional.of(20))
          .created("2020-11-24")
          .comment(
              Optional.of(
                  "LS 11/20/2020 PID 11/25/2020 5-DAY RTC PER DR STEELE "
                      + "F2F FOR 40-MIN OK TO SCHEDULE @0800 FOR 40-MIN PER DR STEELE "
                      + "TO SEE RESIDENT"))
          .basedOn(Optional.of("OTHER"))
          .participant(
              List.of(
                  DatamartReference.builder()
                      .type(Optional.of("Location"))
                      .reference(Optional.of("800157972"))
                      .display(Optional.of("SAC ENT RESIDENT 2"))
                      .build(),
                  DatamartReference.builder()
                      .type(Optional.of("Patient"))
                      .reference(Optional.of("802095909"))
                      .display(Optional.of("PATIENT,FHIRAPPTT JR"))
                      .build()))
          .build();
    }
  }
}
