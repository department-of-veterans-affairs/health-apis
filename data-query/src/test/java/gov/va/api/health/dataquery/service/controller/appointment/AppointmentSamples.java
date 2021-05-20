package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppointmentSamples {
  public static ResourceIdentity id(String cdwId) {
    return ResourceIdentity.builder()
        .system("CDW")
        .resource("APPOINTMENT")
        .identifier(cdwId)
        .build();
  }

  @SneakyThrows
  static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public static Registration registration(String cdwId, String publicId) {
    return Registration.builder().uuid(publicId).resourceIdentities(List.of(id(cdwId))).build();
  }

  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    public DatamartAppointment appointment(
        String cdwIdNumber, String cdwIdResourceCode, String patientIcn) {
      return DatamartAppointment.builder()
          .cdwId(cdwIdNumber + ":" + cdwIdResourceCode)
          .cancelationReason(Optional.of("OTHER"))
          .serviceCategory(Optional.of("SURGERY"))
          .serviceType("OTOLARYNGOLOGY/ENT")
          .status(Optional.of("INPATIENT APPOINTMENT"))
          .specialty(Optional.of("SURGERY"))
          .appointmentType(Optional.of("WALKIN"))
          .description(Optional.of("Walk-In Visit"))
          .start(Optional.of(Instant.parse("2020-11-25T08:00:00Z")))
          .end(Optional.of(Instant.parse("2020-11-25T08:20:00Z")))
          .minutesDuration(Optional.of(20))
          .visitSid(Optional.of(1L))
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
                      .reference(Optional.of(patientIcn))
                      .display(Optional.of("PATIENT,FHIRAPPTT JR"))
                      .build()))
          .lastUpdated(Instant.parse("2020-12-25T00:00:00Z"))
          .build();
    }

    public DatamartAppointment appointment() {
      return appointment("1600021515962", "A", "802095909");
    }

    public AppointmentEntity entity(
        String cdwIdNumber, String cdwIdResourceCode, String patientIcn) {
      return entity(
          appointment(cdwIdNumber, cdwIdResourceCode, patientIcn),
          cdwIdNumber,
          cdwIdResourceCode,
          patientIcn);
    }

    public AppointmentEntity entity(
        DatamartAppointment dm, String cdwIdNumber, String cdwIdResourceCode, String patientIcn) {
      return AppointmentEntity.builder()
          .cdwIdNumber(new BigInteger(cdwIdNumber))
          .cdwIdResourceCode(cdwIdResourceCode.charAt(0))
          .icn(patientIcn)
          .payload(json(dm))
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

    public Appointment appointment(String cdwId, String patientId) {
      return Appointment.builder()
          .id(cdwId)
          .meta(Meta.builder().lastUpdated("2020-12-25T00:00:00Z").build())
          .status(Appointment.AppointmentStatus.fulfilled)
          .cancelationReason(cancelationReason())
          .serviceCategory(serviceCategory())
          .specialty(specialty())
          .appointmentType(appointmentType())
          .description("Walk-In Visit")
          .start("2020-11-25T08:00:00Z")
          .end("2020-11-25T08:20:00Z")
          .minutesDuration(20)
          .created("2020-11-24")
          .comment(
              "LS 11/20/2020 PID 11/25/2020 5-DAY RTC PER DR STEELE "
                  + "F2F FOR 40-MIN OK TO SCHEDULE @0800 FOR 40-MIN PER DR STEELE "
                  + "TO SEE RESIDENT")
          .participant(participants(patientId))
          .build();
    }

    public Appointment appointment() {
      return appointment("1600021515962:A", "802095909");
    }

    private CodeableConcept appointmentType() {
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .system("http://terminology.hl7.org/CodeSystem/v2-0276")
                      .display("WALKIN")
                      .build()))
          .text("WALKIN")
          .build();
    }

    private CodeableConcept cancelationReason() {
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .system("http://www.va.gov/Terminology/VistADefinedTerms/2.98-16")
                      .display("OTHER")
                      .build()))
          .text("OTHER")
          .build();
    }

    private List<Appointment.Participant> participants(String patientId) {
      return List.of(
          Appointment.Participant.builder()
              .actor(reference("SAC ENT RESIDENT 2", "Location/800157972"))
              .status(Appointment.ParticipationStatus.accepted)
              .build(),
          Appointment.Participant.builder()
              .actor(reference("PATIENT,FHIRAPPTT JR", "Patient/" + patientId))
              .status(Appointment.ParticipationStatus.accepted)
              .build());
    }

    private gov.va.api.health.r4.api.elements.Reference reference(String display, String ref) {
      return gov.va.api.health.r4.api.elements.Reference.builder()
          .display(display)
          .reference(ref)
          .build();
    }

    private List<CodeableConcept> serviceCategory() {
      return List.of(
          CodeableConcept.builder()
              .coding(
                  List.of(
                      Coding.builder()
                          .system("http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                          .code("S")
                          .display("SURGERY")
                          .build()))
              .text("SURGERY")
              .build());
    }

    private List<CodeableConcept> specialty() {
      return List.of(
          CodeableConcept.builder()
              .coding(
                  List.of(
                      Coding.builder()
                          .system("http://hl7.org/fhir/ValueSet/c80-practice-codes")
                          .display("SURGERY")
                          .build()))
              .text("SURGERY")
              .build());
    }
  }
}
