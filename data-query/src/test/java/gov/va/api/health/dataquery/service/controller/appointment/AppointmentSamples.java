package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppointmentSamples {

  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    public DatamartAppointment appointment() {
      return DatamartAppointment.builder()
          .cdwId("1600021515962:A")
          //.status(Optional.of("NT-CO"))
          .cancelationReason(Optional.of("SCHEDULING CONFLICT/ERROR"))
          .serviceCategory(Optional.of("SURGERY"))
          .serviceType("OTOLARYNGOLOGY/ENT")
          .specialty(Optional.of("Surgery-Cardiac surgery"))
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
                      .reference(Optional.of("43841:L"))
                      .display(Optional.of("MENTAL HEALTH SERVICES"))
                      .build(),
                  DatamartReference.builder()
                      .type(Optional.of("Patient"))
                      .reference(Optional.of("1017283180V801730"))
                      .display(Optional.of("Frankenpatient, Victor"))
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {

    static Appointment.Bundle asBundle(
        String baseUrl,
        Collection<Appointment> appointments,
        int totalRecords,
        BundleLink... links) {
      return Appointment.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              appointments.stream()
                  .map(
                      a ->
                          Appointment.Entry.builder()
                              .fullUrl(baseUrl + "/Appointment/" + a.id())
                              .resource(a)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static gov.va.api.health.r4.api.bundle.BundleLink link(
            gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation rel,
            String base,
            int page,
            int count) {
      return gov.va.api.health.r4.api.bundle.BundleLink.builder()
              .relation(rel)
              .url(base + "&page=" + page + "&_count=" + count)
              .build();
    }

    public Appointment appointment(String id) {
      return Appointment.builder()
              .resourceType("Appointment")
              .id(id)
              //.status()
              .cancelationReason(cancelationReason())
              .serviceCategory(serviceCategory())
              .serviceType(serviceType())
              .specialty(specialty())
              .appointmentType(appointmentType())
              .description("Walk-In Visit")
              .start("2020-11-25T08:00:00Z")
              .end("2020-11-25T08:20:00Z")
              .minutesDuration(20)
              .created("2020-11-24")
              .comment("LS 11/20/2020 PID 11/25/2020 5-DAY RTC PER DR STEELE "
                      + "F2F FOR 40-MIN OK TO SCHEDULE @0800 FOR 40-MIN PER DR STEELE "
                      + "TO SEE RESIDENT")
              .participant(participants())
              .build();
    }

    private gov.va.api.health.r4.api.elements.Reference reference(String display, String ref) {
      return gov.va.api.health.r4.api.elements.Reference.builder()
              .display(display)
              .reference(ref)
              .build();
    }

    private List<Appointment.Participant> participants() {
      return List.of(
              Appointment.Participant.builder()
                      .actor(reference("MENTAL HEALTH SERVICES", "Location/43841:L"))
                      .status(Appointment.ParticipationStatus.accepted)
                      .build(),
              Appointment.Participant.builder()
                      .actor(reference("Frankenpatient, Victor", "Patient/1017283180V801730"))
                      .status(Appointment.ParticipationStatus.accepted)
                      .build()
      );
    }

    private CodeableConcept appointmentType() {
      return CodeableConcept.builder()
              .coding(
                      List.of(
                              Coding.builder()
                                      .system("http://terminology.hl7.org/CodeSystem/v2-0276")
                                      .code("WALKIN")
                                      .display("WALKIN")
                                      .build()))
              .text("WALKIN")
              .build();
    }

    private List<CodeableConcept> specialty() {
      return List.of(
              CodeableConcept.builder()
                      .coding(
                              List.of(
                                      Coding.builder()
                                              .system("http://hl7.org/fhir/ValueSet/c80-practice-codes")
                                              .code("Surgery-Cardiac surgery")
                                              .display("Surgery-Cardiac surgery")
                                              .build()))
                      .text("Surgery-Cardiac surgery")
                      .build());    }


    private List<CodeableConcept> serviceType() {
      return List.of(
              CodeableConcept.builder()
                      .coding(
                              List.of(
                                      Coding.builder()
                                              .system("http://terminology.hl7.org/CodeSystem/service-type")
                                              .code("OTOLARYNGOLOGY/ENT")
                                              .display("OTOLARYNGOLOGY/ENT")
                                              .build()))
                      .text("OTOLARYNGOLOGY/ENT")
                      .build());
    }

    private List<CodeableConcept> serviceCategory() {
      return List.of(
              CodeableConcept.builder()
                      .coding(
                              List.of(
                                      Coding.builder()
                                              .system("http://terminology.hl7.org/CodeSystem/service-category")
                                              .code("SURGERY")
                                              .display("SURGERY")
                                              .build()))
                      .text("SURGERY")
                      .build());
    }

    private CodeableConcept cancelationReason() {
      return CodeableConcept.builder()
              .coding(
                      List.of(
                              Coding.builder()
                                      .system("http://terminology.hl7.org/CodeSystem/appointment-cancellation-reason")
                                      .code("SCHEDULING CONFLICT/ERROR")
                                      .display("SCHEDULING CONFLICT/ERROR")
                                      .build()))
              .text("SCHEDULING CONFLICT/ERROR")
              .build();    }

    public Appointment appointment() {
      return appointment("1600021515962:A");
    }
  }
}
