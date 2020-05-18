package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.dataquery.service.controller.R4Transformers;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.VaccineCode;
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
          .note(note())
          .reaction(Optional.of(reaction()))
          .vaccinationProtocols(
              Optional.of(
                  DatamartImmunization.VaccinationProtocols.builder()
                      .series("Booster")
                      .seriesDoses(1)
                      .build()))
          .build();
    }

    Optional<String> note() {
      return Optional.of("PATIENT CALM AFTER VACCINATION");
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
    static gov.va.api.health.argonaut.api.resources.Immunization.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.argonaut.api.resources.Immunization> immunizations,
        int totalRecords,
        gov.va.api.health.dstu2.api.bundle.BundleLink... links) {
      return gov.va.api.health.argonaut.api.resources.Immunization.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              immunizations.stream()
                  .map(
                      c ->
                          gov.va.api.health.argonaut.api.resources.Immunization.Entry.builder()
                              .fullUrl(baseUrl + "/Immunization/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.dstu2.api.bundle.AbstractEntry
                                              .SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static gov.va.api.health.dstu2.api.bundle.BundleLink link(
        gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.dstu2.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    gov.va.api.health.argonaut.api.resources.Immunization immunization() {
      return immunization("1000000030337", "1011549983V753765");
    }

    gov.va.api.health.argonaut.api.resources.Immunization immunization(
        String id, String patientId) {
      return gov.va.api.health.argonaut.api.resources.Immunization.builder()
          .resourceType(gov.va.api.health.argonaut.api.resources.Immunization.class.getSimpleName())
          .id(id)
          .date("1997-05-09T14:21:18Z")
          .status(gov.va.api.health.argonaut.api.resources.Immunization.Status.completed)
          ._status(null)
          .vaccineCode(vaccineCode())
          .patient(reference("ZZTESTPATIENT,THOMAS THE", "Patient/" + patientId))
          .wasNotGiven(false)
          .reported(null)
          ._reported(
              gov.va.api.health.dstu2.api.DataAbsentReason.of(
                  gov.va.api.health.dstu2.api.DataAbsentReason.Reason.unsupported))
          .performer(reference("ZHIVAGO,YURI ANDREYEVICH", "Practitioner/3868169"))
          .requester(reference("SHINE,DOC RAINER", "Practitioner/1702436"))
          .location(reference("ZZGOLD PRIMARY CARE", "Location/358359"))
          .note(note("PATIENT CALM AFTER VACCINATION"))
          .reaction(reactions())
          .build();
    }

    List<gov.va.api.health.dstu2.api.datatypes.Annotation> note(String text) {
      return List.of(gov.va.api.health.dstu2.api.datatypes.Annotation.builder().text(text).build());
    }

    gov.va.api.health.argonaut.api.resources.Immunization.Reaction reaction(String display) {
      return gov.va.api.health.argonaut.api.resources.Immunization.Reaction.builder()
          .detail(gov.va.api.health.dstu2.api.elements.Reference.builder().display(display).build())
          .build();
    }

    List<gov.va.api.health.argonaut.api.resources.Immunization.Reaction> reactions() {
      return List.of(reaction("Other"));
    }

    gov.va.api.health.dstu2.api.elements.Reference reference(String display, String ref) {
      return gov.va.api.health.dstu2.api.elements.Reference.builder()
          .display(display)
          .reference(ref)
          .build();
    }

    gov.va.api.health.dstu2.api.datatypes.CodeableConcept vaccineCode() {
      return gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
          .text("TETANUS TOXOID, UNSPECIFIED FORMULATION")
          .coding(
              List.of(
                  gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                      .code("112")
                      .system("http://hl7.org/fhir/sid/cvx")
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {

    static gov.va.api.health.uscorer4.api.resources.Immunization.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.uscorer4.api.resources.Immunization> immunizations,
        int totalRecords,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return gov.va.api.health.uscorer4.api.resources.Immunization.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              immunizations.stream()
                  .map(
                      c ->
                          gov.va.api.health.uscorer4.api.resources.Immunization.Entry.builder()
                              .fullUrl(baseUrl + "/Immunization/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.r4.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode
                                              .match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static gov.va.api.health.r4.api.bundle.BundleLink link(
        gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.r4.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    gov.va.api.health.uscorer4.api.resources.Immunization immunization() {
      return immunization("1000000030337", "1011549983V753765");
    }

    gov.va.api.health.uscorer4.api.resources.Immunization immunization(
        String id, String patientId) {
      return gov.va.api.health.uscorer4.api.resources.Immunization.builder()
          .resourceType(gov.va.api.health.uscorer4.api.resources.Immunization.class.getSimpleName())
          .id(id)
          .occurrenceDateTime("1997-05-09T14:21:18Z")
          .status(gov.va.api.health.uscorer4.api.resources.Immunization.Status.completed)
          .vaccineCode(vaccineCode())
          .patient(reference("ZZTESTPATIENT,THOMAS THE", "Patient/" + patientId))
          .performer(performer())
          .location(reference("ZZGOLD PRIMARY CARE", "Location/358359"))
          .note(note())
          .reaction(reactions())
          .build();
    }

    List<gov.va.api.health.r4.api.datatypes.Annotation> note() {
      return List.of(
          gov.va.api.health.r4.api.datatypes.Annotation.builder()
              .text("PATIENT CALM AFTER VACCINATION")
              .build());
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
