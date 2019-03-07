package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.resources.Appointment;
import gov.va.api.health.dataquery.api.resources.Appointment.Participant;
import gov.va.api.health.dataquery.api.resources.Appointment.Participant.ParticipantStatus;
import gov.va.api.health.dataquery.api.resources.Appointment.Participant.RequiredCode;
import gov.va.api.health.dataquery.api.resources.Appointment.Status;
import java.util.Arrays;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@NoArgsConstructor(staticName = "get")
public class SampleAppointments {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public Appointment appointment() {
    return Appointment.builder()
        .resourceType("Appointment")
        .id("2222")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello Language")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .identifier(singletonList(identifier()))
        .status(Status.proposed)
        .type(dataTypes.codeableConcept())
        .reason(dataTypes.codeableConcept())
        .priority(1)
        .description("Description")
        .start("1970-01-01T00:00:00Z")
        .end("1970-01-01T00:00:00Z")
        .minutesDuration(5)
        .slot(singletonList(dataTypes.reference()))
        .comment("Comment")
        .participant(
            singletonList(
                Participant.builder()
                    .type(Arrays.asList(dataTypes.codeableConcept(), dataTypes.codeableConcept()))
                    .actor(dataTypes.reference())
                    .required(RequiredCode.information_only)
                    .status(ParticipantStatus.needs_action)
                    .build()))
        .build();
  }
}
