package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.time.Instant;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartAllergyIntoleranceTest {
  @Test
  @SneakyThrows
  public void unmarshalSample() {
    DatamartAllergyIntolerance dm =
        createMapper()
            .readValue(
                getClass().getResourceAsStream("datamart-allergy-intolerance.json"),
                DatamartAllergyIntolerance.class);

    assertThat(dm)
        .isEqualTo(
            DatamartAllergyIntolerance.builder()
                .objectType("AllergyIntolerance")
                .objectVersion(1)
                .cdwId("800001608621")
                .etlDate("2017-07-23T04:27:43Z")
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
                .build());
  }
}
