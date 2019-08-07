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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartAllergyIntoleranceSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {

    public DatamartAllergyIntolerance allergyIntolerance() {
      return DatamartAllergyIntolerance.builder()
          .objectType("AllergyIntolerance")
          .objectVersion(1)
          .cdwId("800001608621")
          .patient(
              Optional.of(
                  DatamartReference.builder()
                      .type(Optional.of("Patient"))
                      .reference(Optional.of("666V666"))
                      .display(Optional.of("VETERAN,HERNAM MINAM"))
                      .build()))
          .recordedDate(Optional.of(Instant.parse("2017-07-23T04:27:43Z")))
          .recorder(
              Optional.of(
                  DatamartReference.builder()
                      .type(Optional.of("Practitioner"))
                      .reference(Optional.of("4182448"))
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
                                  .reference(Optional.of("1319143"))
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
                                  .reference(Optional.of("1319143"))
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
                                  .reference(Optional.of("1319143"))
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
                                  .reference(Optional.of("1319143"))
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

    static final String ID = "865e1010-99b6-4b8d-a5c9-4ad259db0857";

    static final String RECORDER_ID = "c8779355-6c0c-40c3-94d1-a8e2a785adfd";

    static final String PATIENT_ID = "04daf655-fcb4-480c-84bf-4319a3af93d7";

    static final String NOTE_AUTHOR_ID = "5d7a23c1-2f9d-4793-b171-b607970b56b8";

    static AllergyIntolerance.Bundle asBundle(
        String baseUrl, List<AllergyIntolerance> resources, BundleLink... links) {
      return AllergyIntolerance.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(1)
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

    public AllergyIntolerance allergyIntolerance() {
      return AllergyIntolerance.builder()
          .resourceType("AllergyIntolerance")
          .id(ID)
          .recordedDate("2017-07-23T04:27:43Z")
          .recorder(
              Reference.builder()
                  .reference("Practitioner/" + RECORDER_ID)
                  .display("MONTAGNE,JO BONES")
                  .build())
          .patient(
              Reference.builder()
                  .reference("Patient/" + PATIENT_ID)
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
                          .reference("Practitioner/" + NOTE_AUTHOR_ID)
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
                                              .build(),
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
