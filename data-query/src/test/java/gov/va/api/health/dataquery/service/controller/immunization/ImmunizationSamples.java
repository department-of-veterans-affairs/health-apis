package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.Immunization.Bundle;
import gov.va.api.health.argonaut.api.resources.Immunization.Entry;
import gov.va.api.health.argonaut.api.resources.Immunization.Reaction;
import gov.va.api.health.argonaut.api.resources.Immunization.Status;
import gov.va.api.health.dataquery.service.controller.R4Transformers;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.VaccineCode;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.DataAbsentReason.Reason;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ImmunizationSamples {
  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    public DatamartImmunization immunization() {
      return immunization("1000000030337", "1011549983V753765");
    }

    DatamartImmunization immunization(String cdwId, String patientId) {
      return DatamartImmunization.builder()
          .cdwId(cdwId)
          .status(DatamartImmunization.Status.completed)
          .date(Instant.parse("1997-05-09T14:21:18Z"))
          .vaccineCode(vaccineCode())
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference(patientId)
                  .display("ZZTESTPATIENT,THOMAS THE")
                  .build())
          .wasNotGiven(false)
          .performer(performer())
          .requester(
              Optional.of(
                  DatamartReference.of()
                      .type("Practitioner")
                      .reference("1702436")
                      .display("SHINE,DOC RAINER")
                      .build()))
          .location(
              Optional.of(
                  DatamartReference.of()
                      .type("Location")
                      .reference("358359")
                      .display("ZZGOLD PRIMARY CARE")
                      .build()))
          .note(Optional.of("PATIENT CALM AFTER VACCINATION"))
          .reaction(Optional.of(reaction()))
          .vaccinationProtocols(
              Optional.of(
                  DatamartImmunization.VaccinationProtocols.builder()
                      .series("Booster")
                      .seriesDoses(1)
                      .build()))
          .build();
    }

    Optional<DatamartReference> performer() {
      return Optional.of(
          DatamartReference.of()
              .type("Practitioner")
              .reference("3868169")
              .display("ZHIVAGO,YURI ANDREYEVICH")
              .build());
    }

    DatamartReference reaction() {
      return DatamartReference.of().type("Observation").reference(null).display("Other").build();
    }

    VaccineCode vaccineCode() {
      return VaccineCode.builder()
          .code("112")
          .text("TETANUS TOXOID, UNSPECIFIED FORMULATION")
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class Dstu2 {
    static Bundle asBundle(
        String baseUrl,
        Collection<Immunization> immunizations,
        int totalRecords,
        BundleLink... links) {
      return Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              immunizations.stream()
                  .map(
                      c ->
                          Entry.builder()
                              .fullUrl(baseUrl + "/Immunization/" + c.id())
                              .resource(c)
                              .search(Search.builder().mode(SearchMode.match).build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static BundleLink link(LinkRelation rel, String base, int page, int count) {
      return BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    Immunization immunization() {
      return immunization("1000000030337", "1011549983V753765");
    }

    Immunization immunization(String id, String patientId) {
      return Immunization.builder()
          .resourceType(Immunization.class.getSimpleName())
          .id(id)
          .date("1997-05-09T14:21:18Z")
          .status(Status.completed)
          ._status(null)
          .vaccineCode(vaccineCode())
          .patient(reference("ZZTESTPATIENT,THOMAS THE", "Patient/" + patientId))
          .wasNotGiven(false)
          .reported(null)
          ._reported(DataAbsentReason.of(Reason.unsupported))
          .performer(reference("ZHIVAGO,YURI ANDREYEVICH", "Practitioner/3868169"))
          .requester(reference("SHINE,DOC RAINER", "Practitioner/1702436"))
          .location(reference("ZZGOLD PRIMARY CARE", "Location/358359"))
          .note(note("PATIENT CALM AFTER VACCINATION"))
          .reaction(reactions())
          .build();
    }

    List<Annotation> note(String text) {
      return List.of(Annotation.builder().text(text).build());
    }

    Reaction reaction(String display) {
      return Reaction.builder().detail(Reference.builder().display(display).build()).build();
    }

    List<Reaction> reactions() {
      return List.of(reaction("Other"));
    }

    Reference reference(String display, String ref) {
      return Reference.builder().display(display).reference(ref).build();
    }

    CodeableConcept vaccineCode() {
      return CodeableConcept.builder()
          .text("TETANUS TOXOID, UNSPECIFIED FORMULATION")
          .coding(
              List.of(Coding.builder().code("112").system("http://hl7.org/fhir/sid/cvx").build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    gov.va.api.health.uscorer4.api.resources.Immunization immunization() {
      return immunization("1000000030337", "1011549983V753765");
    }

    gov.va.api.health.uscorer4.api.resources.Immunization immunization(
        String id, String patientId) {
      return gov.va.api.health.uscorer4.api.resources.Immunization.builder()
          .resourceType(Immunization.class.getSimpleName())
          .id(id)
          .occurrenceDateTime("1997-05-09T14:21:18Z")
          .status(gov.va.api.health.uscorer4.api.resources.Immunization.Status.completed)
          .vaccineCode(vaccineCode())
          .patient(reference("ZZTESTPATIENT,THOMAS THE", "Patient/" + patientId))
          .performer(performer())
          .location(reference("ZZGOLD PRIMARY CARE", "Location/358359"))
          .note(note("PATIENT CALM AFTER VACCINATION"))
          .reaction(reactions())
          .build();
    }

    List<gov.va.api.health.r4.api.datatypes.Annotation> note(String text) {
      return List.of(gov.va.api.health.r4.api.datatypes.Annotation.builder().text(text).build());
    }

    List<gov.va.api.health.uscorer4.api.resources.Immunization.Performer> performer() {
      return List.of(
          gov.va.api.health.uscorer4.api.resources.Immunization.Performer.builder()
              .actor(
                  R4Transformers.asReference(
                      Optional.of(
                          DatamartReference.of()
                              .display("ZHIVAGO,YURI ANDREYEVICH")
                              .type("Practitioner")
                              .reference("3868169")
                              .build())))
              .build());
    }

    gov.va.api.health.uscorer4.api.resources.Immunization.Reaction reaction(String display) {
      return gov.va.api.health.uscorer4.api.resources.Immunization.Reaction.builder()
          .detail(gov.va.api.health.r4.api.elements.Reference.builder().display(display).build())
          .build();
    }

    List<gov.va.api.health.uscorer4.api.resources.Immunization.Reaction> reactions() {
      return List.of(reaction("Other"));
    }

    gov.va.api.health.r4.api.elements.Reference reference(String display, String ref) {
      return gov.va.api.health.r4.api.elements.Reference.builder()
          .display(display)
          .reference(ref)
          .build();
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept vaccineCode() {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text("TETANUS TOXOID, UNSPECIFIED FORMULATION")
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .code("112")
                      .system("http://hl7.org/fhir/sid/cvx")
                      .build()))
          .build();
    }
  }
}
