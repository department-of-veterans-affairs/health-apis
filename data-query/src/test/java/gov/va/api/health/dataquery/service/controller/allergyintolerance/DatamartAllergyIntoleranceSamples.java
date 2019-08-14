package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartAllergyIntoleranceSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {

    public DatamartAllergyIntolerance allergyIntolerance() {
      return allergyIntolerance("800001608621", "666V666");
    }

    public DatamartAllergyIntolerance allergyIntolerance(String cdwId, String patientId) {
      return DatamartAllergyIntolerance.builder()
          .cdwId(cdwId)
          .patient(
              Optional.of(
                  DatamartReference.builder()
                      .type(Optional.of("Patient"))
                      .reference(Optional.of(patientId))
                      .display(Optional.of("VETERAN,HERNAM MINAM"))
                      .build()))
          .recordedDate(Optional.of(Instant.parse("2017-07-23T04:27:43Z")))
          .recorder(
              Optional.of(
                  DatamartReference.builder()
                      .type(Optional.of("Practitioner"))
                      .reference(Optional.of("1234"))
                      .display(Optional.of("MONTAGNE,JO BONES"))
                      .build()))
          .substance(
              Optional.of(
                  DatamartAllergyIntolerance.Substance.builder()
                      .coding(
                          Optional.of(
                              DatamartCoding.of()
                                  .system("http://www.nlm.nih.gov/research/umls/rxnorm")
                                  .code("70618")
                                  .display("Penicillin")
                                  .build()))
                      .text("PENICILLIN")
                      .build()))
          .status(DatamartAllergyIntolerance.Status.confirmed)
          .type(DatamartAllergyIntolerance.Type.allergy)
          .category(DatamartAllergyIntolerance.Category.medication)
          .notes(
              asList(
                  DatamartAllergyIntolerance.Note.builder()
                      .text("ADR PER PT.")
                      .time(Optional.of(Instant.parse("2012-03-29T01:55:03Z")))
                      .practitioner(
                          Optional.of(
                              DatamartReference.builder()
                                  .type(Optional.of("Practitioner"))
                                  .reference(Optional.of("12345"))
                                  .display(Optional.of("PROVID,ALLIN DOC"))
                                  .build()))
                      .build(),
                  DatamartAllergyIntolerance.Note.builder()
                      .text("ADR PER PT.")
                      .time(Optional.of(Instant.parse("2012-03-29T01:56:59Z")))
                      .practitioner(
                          Optional.of(
                              DatamartReference.builder()
                                  .type(Optional.of("Practitioner"))
                                  .reference(Optional.of("12345"))
                                  .display(Optional.of("PROVID,ALLIN DOC"))
                                  .build()))
                      .build(),
                  DatamartAllergyIntolerance.Note.builder()
                      .text("ADR PER PT.")
                      .time(Optional.of(Instant.parse("2012-03-29T01:57:40Z")))
                      .practitioner(
                          Optional.of(
                              DatamartReference.builder()
                                  .type(Optional.of("Practitioner"))
                                  .reference(Optional.of("12345"))
                                  .display(Optional.of("PROVID,ALLIN DOC"))
                                  .build()))
                      .build(),
                  DatamartAllergyIntolerance.Note.builder()
                      .text("REDO")
                      .time(Optional.of(Instant.parse("2012-03-29T01:58:21Z")))
                      .practitioner(
                          Optional.of(
                              DatamartReference.builder()
                                  .type(Optional.of("Practitioner"))
                                  .reference(Optional.of("12345"))
                                  .display(Optional.of("PROVID,ALLIN DOC"))
                                  .build()))
                      .build()))
          .reactions(
              Optional.of(
                  DatamartAllergyIntolerance.Reaction.builder()
                      .certainty(DatamartAllergyIntolerance.Certainty.likely)
                      .manifestations(
                          asList(
                              DatamartCoding.of()
                                  .system("urn:oid:2.16.840.1.113883.6.233")
                                  .code("4637183")
                                  .display("RESPIRATORY DISTRESS")
                                  .build(),
                              DatamartCoding.of()
                                  .system("urn:oid:2.16.840.1.113883.6.233")
                                  .code("4538635")
                                  .display("RASH")
                                  .build()))
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {

    static AllergyIntolerance.Bundle asBundle(
        String baseUrl, Collection<AllergyIntolerance> resources, BundleLink... links) {
      return AllergyIntolerance.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(resources.size())
          .link(Arrays.asList(links))
          .entry(
              resources
                  .stream()
                  .map(
                      a ->
                          AllergyIntolerance.Entry.builder()
                              .fullUrl(baseUrl + "/AllergyIntolerance/" + a.id())
                              .resource(a)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static BundleLink link(BundleLink.LinkRelation rel, String base, int page, int count) {
      return BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public AllergyIntolerance allergyIntolerance(String cdwId) {
      return allergyIntolerance(cdwId, "666V666");
    }

    public AllergyIntolerance allergyIntolerance(String cdwId, String patientId) {
      return AllergyIntolerance.builder()
          .resourceType("AllergyIntolerance")
          .id(cdwId)
          .recordedDate("2017-07-23T04:27:43Z")
          .recorder(
              Reference.builder()
                  .reference("Practitioner/1234")
                  .display("MONTAGNE,JO BONES")
                  .build())
          .patient(
              Reference.builder()
                  .reference("Patient/" + patientId)
                  .display("VETERAN,HERNAM MINAM")
                  .build())
          .substance(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://www.nlm.nih.gov/research/umls/rxnorm")
                              .code("70618")
                              .display("Penicillin")
                              .build()))
                  .text("PENICILLIN")
                  .build())
          .status(AllergyIntolerance.Status.confirmed)
          .type(AllergyIntolerance.Type.allergy)
          .category(AllergyIntolerance.Category.medication)
          .note(
              Annotation.builder()
                  .authorReference(
                      Reference.builder()
                          .reference("Practitioner/12345")
                          .display("PROVID,ALLIN DOC")
                          .build())
                  .time("2012-03-29T01:55:03Z")
                  .text("ADR PER PT.")
                  .build())
          .reaction(
              asList(
                  AllergyIntolerance.Reaction.builder()
                      .certainty(AllergyIntolerance.Certainty.likely)
                      .manifestation(
                          asList(
                              CodeableConcept.builder()
                                  .coding(
                                      asList(
                                          Coding.builder()
                                              .system("urn:oid:2.16.840.1.113883.6.233")
                                              .code("4637183")
                                              .display("RESPIRATORY DISTRESS")
                                              .build()))
                                  .build(),
                              CodeableConcept.builder()
                                  .coding(
                                      asList(
                                          Coding.builder()
                                              .system("urn:oid:2.16.840.1.113883.6.233")
                                              .code("4538635")
                                              .display("RASH")
                                              .build()))
                                  .build()))
                      .build()))
          .build();
    }
  }
}
