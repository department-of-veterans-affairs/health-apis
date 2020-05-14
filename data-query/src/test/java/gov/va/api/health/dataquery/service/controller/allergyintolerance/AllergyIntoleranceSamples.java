package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AllergyIntoleranceSamples {
  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    public DatamartAllergyIntolerance allergyIntolerance() {
      return allergyIntolerance("800001608621", "666V666", "1234", "12345");
    }

    public DatamartAllergyIntolerance allergyIntolerance(
        String cdwId, String patientCdwId, String recorderCdwId, String authorCdwId) {
      return DatamartAllergyIntolerance.builder()
          .cdwId(cdwId)
          .patient(
              DatamartReference.builder()
                  .type(Optional.of("Patient"))
                  .reference(Optional.of(patientCdwId))
                  .display(Optional.of("VETERAN,HERNAM MINAM"))
                  .build())
          .recordedDate(Optional.of(Instant.parse("2017-07-23T04:27:43Z")))
          .recorder(
              Optional.of(
                  DatamartReference.builder()
                      .type(Optional.of("Practitioner"))
                      .reference(Optional.of(recorderCdwId))
                      .display(Optional.of("MONTAGNE,JO BONES"))
                      .build()))
          .substance(fullyPopulatedSubstance())
          .status(DatamartAllergyIntolerance.Status.confirmed)
          .type(DatamartAllergyIntolerance.Type.allergy)
          .category(DatamartAllergyIntolerance.Category.medication)
          .notes(notes(authorCdwId))
          .reactions(reactions())
          .build();
    }

    public List<DatamartAllergyIntolerance.Note> emptyNotes() {
      return asList(DatamartAllergyIntolerance.Note.builder().build());
    }

    public Optional<DatamartAllergyIntolerance.Reaction> emptyReactions() {
      return Optional.of(DatamartAllergyIntolerance.Reaction.builder().build());
    }

    public Optional<DatamartAllergyIntolerance.Substance> emptySubstance() {
      return Optional.of(DatamartAllergyIntolerance.Substance.builder().build());
    }

    public Optional<DatamartAllergyIntolerance.Substance> fullyPopulatedSubstance() {
      return Optional.of(
          DatamartAllergyIntolerance.Substance.builder()
              .coding(
                  Optional.of(
                      DatamartCoding.of()
                          .system("http://www.nlm.nih.gov/research/umls/rxnorm")
                          .code("70618")
                          .display("Penicillin")
                          .build()))
              .text("PENICILLIN")
              .build());
    }

    public List<DatamartCoding> manifestations() {
      return asList(
          DatamartCoding.of()
              .system("urn:oid:2.16.840.1.113883.6.233")
              .code("4637183")
              .display("RESPIRATORY DISTRESS")
              .build(),
          DatamartCoding.of()
              .system("urn:oid:2.16.840.1.113883.6.233")
              .code("4538635")
              .display("RASH")
              .build());
    }

    public List<DatamartAllergyIntolerance.Note> notes(String authorCdwId) {
      return asList(
          DatamartAllergyIntolerance.Note.builder()
              .text("ADR PER PT.")
              .time(Optional.of(Instant.parse("2012-03-29T01:55:03Z")))
              .practitioner(
                  Optional.of(
                      DatamartReference.builder()
                          .type(Optional.of("Practitioner"))
                          .reference(Optional.of(authorCdwId))
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
                          .reference(Optional.of(authorCdwId))
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
                          .reference(Optional.of(authorCdwId))
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
                          .reference(Optional.of(authorCdwId))
                          .display(Optional.of("PROVID,ALLIN DOC"))
                          .build()))
              .build());
    }

    public Optional<DatamartAllergyIntolerance.Reaction> reactions() {
      return Optional.of(
          DatamartAllergyIntolerance.Reaction.builder()
              .certainty(DatamartAllergyIntolerance.Certainty.likely)
              .manifestations(manifestations())
              .build());
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class Dstu2 {

    static gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.argonaut.api.resources.AllergyIntolerance> resources,
        int totalRecords,
        gov.va.api.health.dstu2.api.bundle.BundleLink... links) {
      return gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources
                  .stream()
                  .map(
                      a ->
                          gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Entry
                              .builder()
                              .fullUrl(baseUrl + "/AllergyIntolerance/" + a.id())
                              .resource(a)
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

    public gov.va.api.health.argonaut.api.resources.AllergyIntolerance allergyIntolerance(
        String cdwId) {
      return allergyIntolerance(cdwId, "666V666");
    }

    public gov.va.api.health.argonaut.api.resources.AllergyIntolerance allergyIntolerance() {
      return allergyIntolerance("800001608621", "666V666");
    }

    public gov.va.api.health.argonaut.api.resources.AllergyIntolerance allergyIntolerance(
        String cdwId, String patientId) {
      return gov.va.api.health.argonaut.api.resources.AllergyIntolerance.builder()
          .resourceType("AllergyIntolerance")
          .id(cdwId)
          .recordedDate("2017-07-23T04:27:43Z")
          .recorder(
              gov.va.api.health.dstu2.api.elements.Reference.builder()
                  .reference("Practitioner/1234")
                  .display("MONTAGNE,JO BONES")
                  .build())
          .patient(
              gov.va.api.health.dstu2.api.elements.Reference.builder()
                  .reference("Patient/" + patientId)
                  .display("VETERAN,HERNAM MINAM")
                  .build())
          .substance(fullyPopulatedSubstance())
          .status(gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Status.active)
          .type(gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Type.allergy)
          .category(gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Category.medication)
          .note(note())
          .reaction(reactions())
          .build();
    }

    public gov.va.api.health.dstu2.api.datatypes.CodeableConcept fullyPopulatedSubstance() {
      return gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
          .coding(
              asList(
                  gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                      .system("http://www.nlm.nih.gov/research/umls/rxnorm")
                      .code("70618")
                      .display("Penicillin")
                      .build()))
          .text("PENICILLIN")
          .build();
    }

    public List<gov.va.api.health.dstu2.api.datatypes.CodeableConcept> manifestations() {
      return asList(
          gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
              .coding(
                  asList(
                      gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                          .system("urn:oid:2.16.840.1.113883.6.233")
                          .code("4637183")
                          .display("RESPIRATORY DISTRESS")
                          .build()))
              .build(),
          gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
              .coding(
                  asList(
                      gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                          .system("urn:oid:2.16.840.1.113883.6.233")
                          .code("4538635")
                          .display("RASH")
                          .build()))
              .build());
    }

    public gov.va.api.health.dstu2.api.datatypes.Annotation note() {
      return gov.va.api.health.dstu2.api.datatypes.Annotation.builder()
          .authorReference(
              gov.va.api.health.dstu2.api.elements.Reference.builder()
                  .reference("Practitioner/12345")
                  .display("PROVID,ALLIN DOC")
                  .build())
          .time("2012-03-29T01:55:03Z")
          .text("ADR PER PT.")
          .build();
    }

    public List<gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Reaction> reactions() {
      return asList(
          gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Reaction.builder()
              .certainty(
                  gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Certainty.likely)
              .manifestation(manifestations())
              .build());
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    public gov.va.api.health.uscorer4.api.resources.AllergyIntolerance allergyIntolerance() {
      return allergyIntolerance("800001608621", "666V666", "1234", "12345");
    }

    public gov.va.api.health.uscorer4.api.resources.AllergyIntolerance allergyIntolerance(
        String publicId, String patientPublicId, String recorderPublicId, String authorPublicId) {
      return gov.va.api.health.uscorer4.api.resources.AllergyIntolerance.builder()
          .resourceType("AllergyIntolerance")
          .id(publicId)
          .clinicalStatus(
              gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                  .coding(
                      List.of(
                          gov.va.api.health.r4.api.datatypes.Coding.builder()
                              .system("http://hl7.org/fhir/ValueSet/allergyintolerance-clinical")
                              .code("active")
                              .build()))
                  .build())
          .type(gov.va.api.health.uscorer4.api.resources.AllergyIntolerance.Type.allergy)
          .category(
              List.of(
                  gov.va.api.health.uscorer4.api.resources.AllergyIntolerance.Category.medication))
          .patient(
              gov.va.api.health.r4.api.elements.Reference.builder()
                  .reference("Patient/" + patientPublicId)
                  .display("VETERAN,HERNAM MINAM")
                  .build())
          .recordedDate("2017-07-23T04:27:43Z")
          .recorder(
              gov.va.api.health.r4.api.elements.Reference.builder()
                  .reference("Practitioner/" + recorderPublicId)
                  .display("MONTAGNE,JO BONES")
                  .build())
          .note(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Annotation.builder()
                      .authorReference(
                          gov.va.api.health.r4.api.elements.Reference.builder()
                              .reference("Practitioner/" + authorPublicId)
                              .display("PROVID,ALLIN DOC")
                              .build())
                      .time("2012-03-29T01:55:03Z")
                      .text("ADR PER PT.")
                      .build(),
                  gov.va.api.health.r4.api.datatypes.Annotation.builder()
                      .authorReference(
                          gov.va.api.health.r4.api.elements.Reference.builder()
                              .reference("Practitioner/" + authorPublicId)
                              .display("PROVID,ALLIN DOC")
                              .build())
                      .time("2012-03-29T01:56:59Z")
                      .text("ADR PER PT.")
                      .build(),
                  gov.va.api.health.r4.api.datatypes.Annotation.builder()
                      .authorReference(
                          gov.va.api.health.r4.api.elements.Reference.builder()
                              .reference("Practitioner/" + authorPublicId)
                              .display("PROVID,ALLIN DOC")
                              .build())
                      .time("2012-03-29T01:57:40Z")
                      .text("ADR PER PT.")
                      .build(),
                  gov.va.api.health.r4.api.datatypes.Annotation.builder()
                      .authorReference(
                          gov.va.api.health.r4.api.elements.Reference.builder()
                              .reference("Practitioner/" + authorPublicId)
                              .display("PROVID,ALLIN DOC")
                              .build())
                      .time("2012-03-29T01:58:21Z")
                      .text("REDO")
                      .build()))
          .reaction(
              List.of(
                  gov.va.api.health.uscorer4.api.resources.AllergyIntolerance.Reaction.builder()
                      .substance(
                          gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                              .coding(
                                  List.of(
                                      gov.va.api.health.r4.api.datatypes.Coding.builder()
                                          .system("http://www.nlm.nih.gov/research/umls/rxnorm")
                                          .code("70618")
                                          .display("Penicillin")
                                          .build()))
                              .text("PENICILLIN")
                              .build())
                      .manifestation(
                          List.of(
                              gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                                  .coding(
                                      List.of(
                                          gov.va.api.health.r4.api.datatypes.Coding.builder()
                                              .system("urn:oid:2.16.840.1.113883.6.233")
                                              .code("4637183")
                                              .display("RESPIRATORY DISTRESS")
                                              .build()))
                                  .build(),
                              gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                                  .coding(
                                      List.of(
                                          gov.va.api.health.r4.api.datatypes.Coding.builder()
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
