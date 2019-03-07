package gov.va.api.health.dataquery.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.Appointment;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerAppointment {
  static final Appointment APPOINTMENT =
      Appointment.builder()
          .resourceType("Appointment")
          .id("0be173b4-721c-554e-ba7d-966d04633b68")
          .status(Appointment.Status.cancelled)
          .description("Scheduled Visit")
          .start("2006-06-30T17:00:00Z")
          .participant(
              asList(
                  Appointment.Participant.builder()
                      .type(
                          asList(
                              CodeableConcept.builder()
                                  .coding(
                                      asList(
                                          Coding.builder()
                                              .system("http://hl7.org/fhir/v3/ParticipationType")
                                              .code("PART")
                                              .display("Participation")
                                              .build()))
                                  .build()))
                      .actor(
                          Reference.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Patient/185601V825290")
                              .display("VETERAN,JOHN Q")
                              .build())
                      .required(Appointment.Participant.RequiredCode.required)
                      .status(Appointment.Participant.ParticipantStatus.accepted)
                      .build(),
                  Appointment.Participant.builder()
                      .type(
                          asList(
                              CodeableConcept.builder()
                                  .coding(
                                      asList(
                                          Coding.builder()
                                              .system("http://hl7.org/fhir/v3/ParticipationType")
                                              .code("PART")
                                              .display("Participation")
                                              .build()))
                                  .build()))
                      .actor(
                          Reference.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Location/71fed7fc-d060-5a35-bba9-e89fd7cdb4e6")
                              .display("ZZZTG SURG POD SCHEULLER COBI")
                              .build())
                      .required(Appointment.Participant.RequiredCode.required)
                      .status(Appointment.Participant.ParticipantStatus.accepted)
                      .build()))
          .build();

  static final Appointment.Bundle APPOINTMENT_BUNDLE =
      Appointment.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Appointment?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Appointment?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Appointment?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  Appointment.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Appointment/0be173b4-721c-554e-ba7d-966d04633b68")
                      .resource(
                          Appointment.builder()
                              .resourceType("Appointment")
                              .id("0be173b4-721c-554e-ba7d-966d04633b68")
                              .status(Appointment.Status.cancelled)
                              .description("Scheduled Visit")
                              .start("2006-06-30T17:00:00Z")
                              .participant(
                                  asList(
                                      Appointment.Participant.builder()
                                          .type(
                                              asList(
                                                  CodeableConcept.builder()
                                                      .coding(
                                                          asList(
                                                              Coding.builder()
                                                                  .system(
                                                                      "http://hl7.org/fhir/v3/ParticipationType")
                                                                  .code("PART")
                                                                  .display("Participation")
                                                                  .build()))
                                                      .build()))
                                          .actor(
                                              Reference.builder()
                                                  .reference(
                                                      "https://www.freedomstream.io/Argonaut/api/Patient/185601V825290")
                                                  .display("VETERAN,JOHN Q")
                                                  .build())
                                          .required(Appointment.Participant.RequiredCode.required)
                                          .status(
                                              Appointment.Participant.ParticipantStatus.accepted)
                                          .build(),
                                      Appointment.Participant.builder()
                                          .type(
                                              asList(
                                                  CodeableConcept.builder()
                                                      .coding(
                                                          asList(
                                                              Coding.builder()
                                                                  .system(
                                                                      "http://hl7.org/fhir/v3/ParticipationType")
                                                                  .code("PART")
                                                                  .display("Participation")
                                                                  .build()))
                                                      .build()))
                                          .actor(
                                              Reference.builder()
                                                  .reference(
                                                      "https://www.freedomstream.io/Argonaut/api/Location/71fed7fc-d060-5a35-bba9-e89fd7cdb4e6")
                                                  .display("ZZZTG SURG POD SCHEULLER COBI")
                                                  .build())
                                          .required(Appointment.Participant.RequiredCode.required)
                                          .status(
                                              Appointment.Participant.ParticipantStatus.accepted)
                                          .build()))
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
