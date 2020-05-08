package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.Annotation;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.uscorer4.api.resources.AllergyIntolerance;
import java.util.List;
import org.junit.Test;

public class R4AllergyIntoleranceTransformerTest {
  @Test
  public void toFhir() {
    DatamartAllergyIntolerance dm =
        AllergyIntoleranceSamples.Datamart.create().allergyIntolerance();
    assertThat(R4AllergyIntoleranceTransformer.builder().datamart(dm).build().toFhir())
        .isEqualTo(
            AllergyIntolerance.builder()
                .resourceType("AllergyIntolerance")
                .id("800001608621")
                .clinicalStatus(
                    CodeableConcept.builder()
                        .coding(
                            List.of(
                                Coding.builder()
                                    .system(
                                        "http://hl7.org/fhir/ValueSet/allergyintolerance-clinical")
                                    .code("active")
                                    .build()))
                        .build())
                .type(AllergyIntolerance.Type.allergy)
                .category(List.of(AllergyIntolerance.Category.medication))
                .patient(
                    Reference.builder()
                        .reference("Patient/666V666")
                        .display("VETERAN,HERNAM MINAM")
                        .build())
                .recordedDate("2017-07-23T04:27:43Z")
                .recorder(
                    Reference.builder()
                        .reference("Practitioner/1234")
                        .display("MONTAGNE,JO BONES")
                        .build())
                .note(
                    List.of(
                        Annotation.builder()
                            .authorReference(
                                Reference.builder()
                                    .reference("Practitioner/12345")
                                    .display("PROVID,ALLIN DOC")
                                    .build())
                            .time("2012-03-29T01:55:03Z")
                            .text("ADR PER PT.")
                            .build(),
                        Annotation.builder()
                            .authorReference(
                                Reference.builder()
                                    .reference("Practitioner/12345")
                                    .display("PROVID,ALLIN DOC")
                                    .build())
                            .time("2012-03-29T01:56:59Z")
                            .text("ADR PER PT.")
                            .build(),
                        Annotation.builder()
                            .authorReference(
                                Reference.builder()
                                    .reference("Practitioner/12345")
                                    .display("PROVID,ALLIN DOC")
                                    .build())
                            .time("2012-03-29T01:57:40Z")
                            .text("ADR PER PT.")
                            .build(),
                        Annotation.builder()
                            .authorReference(
                                Reference.builder()
                                    .reference("Practitioner/12345")
                                    .display("PROVID,ALLIN DOC")
                                    .build())
                            .time("2012-03-29T01:58:21Z")
                            .text("REDO")
                            .build()))
                .reaction(
                    List.of(
                        AllergyIntolerance.Reaction.builder()
                            .substance(
                                CodeableConcept.builder()
                                    .coding(
                                        List.of(
                                            Coding.builder()
                                                .system(
                                                    "http://www.nlm.nih.gov/research/umls/rxnorm")
                                                .code("70618")
                                                .display("Penicillin")
                                                .build()))
                                    .text("PENICILLIN")
                                    .build())
                            .manifestation(
                                List.of(
                                    CodeableConcept.builder()
                                        .coding(
                                            List.of(
                                                Coding.builder()
                                                    .system("urn:oid:2.16.840.1.113883.6.233")
                                                    .code("4637183")
                                                    .display("RESPIRATORY DISTRESS")
                                                    .build()))
                                        .build(),
                                    CodeableConcept.builder()
                                        .coding(
                                            List.of(
                                                Coding.builder()
                                                    .system("urn:oid:2.16.840.1.113883.6.233")
                                                    .code("4538635")
                                                    .display("RASH")
                                                    .build()))
                                        .build()))
                            .build()))
                .build());
  }
}
