package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.AntibioticComponent;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.BacteriologyComponent;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.CodeableConcept;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.ReferenceRange;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.VitalsComponent;
import java.time.Instant;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatamartObservationTest {
  @SneakyThrows
  public void assertReadable(String json) {
    assertThat(
            createMapper()
                .readValue(getClass().getResourceAsStream(json), DatamartObservation.class))
        .isEqualTo(sample());
  }

  @Test
  @SneakyThrows
  public void lazy() {
    DatamartObservation dm = DatamartObservation.builder().build();
    assertThat(dm.antibioticComponents()).isNotNull();
    assertThat(dm.bacteriologyComponents()).isNotNull();
    assertThat(dm.code()).isNotNull();
    assertThat(dm.effectiveDateTime()).isNotNull();
    assertThat(dm.issued()).isNotNull();
    assertThat(dm.mycobacteriologyComponents()).isNotNull();
    assertThat(dm.performer()).isNotNull();
    assertThat(dm.referenceRange()).isNotNull();
    assertThat(dm.specimen()).isNotNull();
    assertThat(dm.subject()).isNotNull();
    assertThat(dm.valueCodeableConcept()).isNotNull();
    assertThat(dm.valueQuantity()).isNotNull();
    assertThat(dm.vitalsComponents()).isNotNull();

    AntibioticComponent abc = DatamartObservation.AntibioticComponent.builder().build();
    assertThat(abc.code()).isNotNull();
    assertThat(abc.valueCodeableConcept()).isNotNull();

    BacteriologyComponent bc = DatamartObservation.BacteriologyComponent.builder().build();
    assertThat(bc.code()).isNotNull();
    assertThat(bc.valueText()).isNotNull();

    assertThat(DatamartObservation.CodeableConcept.builder().build().coding()).isNotNull();

    ReferenceRange rr = DatamartObservation.ReferenceRange.builder().build();
    assertThat(rr.high()).isNotNull();
    assertThat(rr.low()).isNotNull();

    VitalsComponent vc = DatamartObservation.VitalsComponent.builder().build();
    assertThat(vc.code()).isNotNull();
    assertThat(vc.valueQuantity()).isNotNull();

    CodeableConcept cc = CodeableConcept.builder().build();
    assertThat(cc.coding().isPresent()).isFalse();
    cc.setDisplay("HALLO");
    assertThat(cc.coding().get().display().get()).isEqualTo("HALLO");
  }

  public DatamartObservation sample() {
    return DatamartObservation.builder()
        .objectType("Observation")
        .objectVersion(1)
        .cdwId("800001973863:A")
        .status(DatamartObservation.Status._final)
        .category(DatamartObservation.Category.laboratory)
        .code(
            Optional.of(
                DatamartObservation.CodeableConcept.builder()
                    .coding(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("http://loinc.org"))
                                .code(Optional.of("1989-3"))
                                .display(Optional.of("VITAMIN D,25-OH,TOTAL"))
                                .build()))
                    .text("VITAMIN D,25-OH,TOTAL")
                    .build()))
        .subject(
            Optional.of(
                DatamartReference.builder()
                    .type(Optional.of("Patient"))
                    .reference(Optional.of("1002003004V666666"))
                    .display(Optional.of("VETERAN,AUDIE OBS"))
                    .build()))
        .effectiveDateTime(Optional.of(Instant.parse("2012-12-24T14:12:00Z")))
        .issued(Optional.of(Instant.parse("2012-12-26T19:42:00Z")))
        .performer(
            asList(
                DatamartReference.builder()
                    .type(Optional.of("Practitioner"))
                    .reference(Optional.of("666000"))
                    .display(Optional.of("WELBY,MARCUS MCCOY"))
                    .build(),
                DatamartReference.builder()
                    .type(Optional.of("Organization"))
                    .reference(Optional.of("325832"))
                    .display(Optional.of("WHITE RIVER JCT VAMROC"))
                    .build()))
        .valueQuantity(
            Optional.of(
                DatamartObservation.Quantity.builder()
                    .value(111.82D)
                    .unit("ng/mL")
                    .system("http://unitsofmeasure.org")
                    .code("ng/mL")
                    .build()))
        .valueCodeableConcept(
            Optional.of(
                DatamartObservation.CodeableConcept.builder()
                    .coding(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("http://snomed.info/sct"))
                                .code(Optional.of("112283007"))
                                .display(Optional.of("ESCHERICHIA COLI"))
                                .build()))
                    .text("ESCHERICHIA COLI")
                    .build()))
        .interpretation("H")
        .comment(
            "CYTOSPIN:NO ACID-FAST BACILLI SEEN. 01/02/2015 BACILLI ISOLATED AFTER 6 WEEKS BY LABCORP")
        .specimen(
            Optional.of(
                DatamartReference.builder()
                    .type(Optional.of("Specimen"))
                    .reference(Optional.of("800005563"))
                    .display(Optional.of("URINE (CLEAN CATCH)"))
                    .build()))
        .referenceRange(
            Optional.of(
                DatamartObservation.ReferenceRange.builder()
                    .high(
                        Optional.of(
                            DatamartObservation.Quantity.builder()
                                .value(100D)
                                .unit("ng/mL")
                                .system("http://unitsofmeasure.org")
                                .code("ng/mL")
                                .build()))
                    .low(
                        Optional.of(
                            DatamartObservation.Quantity.builder()
                                .value(30D)
                                .unit("ng/mL")
                                .system("http://unitsofmeasure.org")
                                .code("ng/mL")
                                .build()))
                    .build()))
        .vitalsComponents(
            asList(
                DatamartObservation.VitalsComponent.builder()
                    .code(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("http://loinc.org"))
                                .code(Optional.of("8480-6"))
                                .display(Optional.of("Systolic blood pressure"))
                                .build()))
                    .valueQuantity(
                        Optional.of(
                            DatamartObservation.Quantity.builder()
                                .value(114D)
                                .unit("mm[Hg]")
                                .system("http://unitsofmeasure.org")
                                .code("mm[Hg]")
                                .build()))
                    .build(),
                DatamartObservation.VitalsComponent.builder()
                    .code(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("http://loinc.org"))
                                .code(Optional.of("8462-4"))
                                .display(Optional.of("Diastolic blood pressure"))
                                .build()))
                    .valueQuantity(
                        Optional.of(
                            DatamartObservation.Quantity.builder()
                                .value(62D)
                                .unit("mm[Hg]")
                                .system("http://unitsofmeasure.org")
                                .code("mm[Hg]")
                                .build()))
                    .build()))
        .antibioticComponents(
            asList(
                DatamartObservation.AntibioticComponent.builder()
                    .id("800011708199")
                    .codeText("CEFAZOLIN-1")
                    .code(
                        Optional.of(
                            DatamartObservation.CodeableConcept.builder()
                                .text("CEFAZOLIN-1")
                                .coding(
                                    Optional.of(
                                        DatamartCoding.builder()
                                            .display(Optional.of("CEFAZOLIN"))
                                            .build()))
                                .build()))
                    .valueCodeableConcept(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("http://snomed.info/sct"))
                                .code(Optional.of("S"))
                                .display(Optional.of("Sensitive"))
                                .build()))
                    .build(),
                DatamartObservation.AntibioticComponent.builder()
                    .id("800011708205")
                    .codeText("IMIPENEM")
                    .code(
                        Optional.of(
                            DatamartObservation.CodeableConcept.builder()
                                .text("IMIPENEM")
                                .coding(
                                    Optional.of(
                                        DatamartCoding.builder()
                                            .system(Optional.of("http://loinc.org"))
                                            .code(Optional.of("279-0"))
                                            .display(Optional.of("IMIPENEM"))
                                            .build()))
                                .build()))
                    .valueCodeableConcept(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("http://snomed.info/sct"))
                                .code(Optional.of("S"))
                                .display(Optional.of("Sensitive"))
                                .build()))
                    .build(),
                DatamartObservation.AntibioticComponent.builder()
                    .id("800011708207")
                    .codeText("CIPROFLOXACIN")
                    .code(
                        Optional.of(
                            DatamartObservation.CodeableConcept.builder()
                                .text("CIPROFLOXACIN")
                                .coding(
                                    Optional.of(
                                        DatamartCoding.builder()
                                            .system(Optional.of("http://loinc.org"))
                                            .code(Optional.of("185-9"))
                                            .display(Optional.of("CIPROFLOXACIN"))
                                            .build()))
                                .build()))
                    .valueCodeableConcept(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("http://snomed.info/sct"))
                                .code(Optional.of("S"))
                                .display(Optional.of("Sensitive"))
                                .build()))
                    .build(),
                DatamartObservation.AntibioticComponent.builder()
                    .id("800011708208")
                    .codeText("CEFTRIAXONE-3")
                    .code(
                        Optional.of(
                            DatamartObservation.CodeableConcept.builder()
                                .text("CEFTRIAXONE-3")
                                .coding(
                                    Optional.of(
                                        DatamartCoding.builder()
                                            .system(Optional.of("http://loinc.org"))
                                            .code(Optional.of("141-2"))
                                            .display(Optional.of("CEFTRIAXONE"))
                                            .build()))
                                .build()))
                    .valueCodeableConcept(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("http://snomed.info/sct"))
                                .code(Optional.of("S"))
                                .display(Optional.of("Sensitive"))
                                .build()))
                    .build(),
                DatamartObservation.AntibioticComponent.builder()
                    .id("800011708202")
                    .codeText("SXT (BACTRIM)")
                    .code(
                        Optional.of(
                            DatamartObservation.CodeableConcept.builder()
                                .text("SXT (BACTRIM)")
                                .coding(
                                    Optional.of(
                                        DatamartCoding.builder()
                                            .system(Optional.of("http://loinc.org"))
                                            .code(Optional.of("516-5"))
                                            .display(Optional.of("TRIMETHOPRIM+SULFAMETHOXAZOLE"))
                                            .build()))
                                .build()))
                    .valueCodeableConcept(
                        Optional.of(
                            DatamartCoding.builder()
                                .system(Optional.of("http://snomed.info/sct"))
                                .code(Optional.of("R"))
                                .display(Optional.of("Resistant"))
                                .build()))
                    .build()))
        .mycobacteriologyComponents(
            Optional.of(
                DatamartObservation.BacteriologyComponent.builder()
                    .code(
                        Optional.of(
                            DatamartObservation.Text.builder().text("Acid Fast Stain").build()))
                    .valueText(
                        Optional.of(
                            DatamartObservation.Text.builder()
                                .text("Concentrate Negative")
                                .build()))
                    .build()))
        .bacteriologyComponents(
            Optional.of(
                DatamartObservation.BacteriologyComponent.builder()
                    .code(
                        Optional.of(
                            DatamartObservation.Text.builder().text("Sputum Screen").build()))
                    .valueText(
                        Optional.of(
                            DatamartObservation.Text.builder()
                                .text("GOOD QUALITY SPECIMEN BY GRAM STAIN EVALUATION")
                                .build()))
                    .build()))
        .build();
  }

  @Test
  public void unmarshalSample() {
    assertReadable("datamart-observation.json");
  }
}
