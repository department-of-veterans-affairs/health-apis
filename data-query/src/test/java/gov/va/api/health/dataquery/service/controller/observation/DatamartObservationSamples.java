package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.Quantity;
import gov.va.api.health.dstu2.api.datatypes.SimpleQuantity;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartObservationSamples {
  @AllArgsConstructor(staticName = "create")
  static class Datamart {
    public DatamartObservation observation() {
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
          .encounter(
              Optional.of(
                  DatamartReference.builder()
                      .type(Optional.of("Encounter"))
                      .reference(Optional.of("123454321"))
                      .display(Optional.of("Ambulatory"))
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
                      .value(111.82)
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
                                              .system(Optional.of("http://loinc.org"))
                                              .code(Optional.of("76-0"))
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
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {
    static final String ID = "2b45ed16-3d77-45b0-b540-928605528ef0";

    static final String SUBJECT_ID = "a54f0884-e781-47e0-b1b3-59ab9764c019";

    static final String ENCOUNTER_ID = "68dc7dd9-1f4e-43fa-b07f-234766aa3d5d";

    static final String PERFORMER_ID_1 = "f07c0bbc-780f-4dd2-b1f3-4e2158e97273";

    static final String PERFORMER_ID_2 = "a695b2b2-49bc-42c8-99fc-2b405be77f36";

    public Observation observation() {
      return Observation.builder()
          .resourceType("Observation")
          .id(ID)
          .status(Observation.Status._final)
          .category(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://hl7.org/fhir/observation-category")
                              .code("laboratory")
                              .display("Laboratory")
                              .build()))
                  .build())
          .code(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://loinc.org")
                              .code("1989-3")
                              .display("VITAMIN D,25-OH,TOTAL")
                              .build()))
                  .text("VITAMIN D,25-OH,TOTAL")
                  .build())
          .subject(
              Reference.builder()
                  .reference("Patient/" + SUBJECT_ID)
                  .display("VETERAN,AUDIE OBS")
                  .build())
          .encounter(
              Reference.builder()
                  .reference("Encounter/" + ENCOUNTER_ID)
                  .display("Ambulatory")
                  .build())
          .effectiveDateTime("2012-12-24T14:12:00Z")
          .issued("2012-12-26T19:42:00Z")
          .performer(
              asList(
                  Reference.builder()
                      .reference("Practitioner/" + PERFORMER_ID_1)
                      .display("WELBY,MARCUS MCCOY")
                      .build(),
                  Reference.builder()
                      .reference("Organization/" + PERFORMER_ID_2)
                      .display("WHITE RIVER JCT VAMROC")
                      .build()))
          .valueQuantity(
              Quantity.builder()
                  .value(111.82)
                  .unit("ng/mL")
                  .system("http://unitsofmeasure.org")
                  .code("ng/mL")
                  .build())
          .valueCodeableConcept(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://snomed.info/sct")
                              .code("112283007")
                              .display("ESCHERICHIA COLI")
                              .build()))
                  .text("ESCHERICHIA COLI")
                  .build())
          .interpretation(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://hl7.org/fhir/v2/0078")
                              .code("H")
                              .display("High")
                              .build()))
                  .text("H")
                  .build())
          .comments(
              "CYTOSPIN:NO ACID-FAST BACILLI SEEN. 01/02/2015 BACILLI ISOLATED AFTER 6 WEEKS BY LABCORP")
          .referenceRange(
              asList(
                  Observation.ObservationReferenceRange.builder()
                      .low(
                          SimpleQuantity.builder()
                              .value(30.0)
                              .unit("ng/mL")
                              .system("http://unitsofmeasure.org")
                              .code("ng/mL")
                              .build())
                      .high(
                          SimpleQuantity.builder()
                              .value(100.0)
                              .unit("ng/mL")
                              .system("http://unitsofmeasure.org")
                              .code("ng/mL")
                              .build())
                      .build()))
          .component(
              asList(
                  Observation.ObservationComponent.builder()
                      .code(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://loinc.org")
                                          .code("8480-6")
                                          .display("Systolic blood pressure")
                                          .build()))
                              .build())
                      .valueQuantity(
                          Quantity.builder()
                              .value(114.0)
                              .unit("mm[Hg]")
                              .system("http://unitsofmeasure.org")
                              .code("mm[Hg]")
                              .build())
                      .build(),
                  Observation.ObservationComponent.builder()
                      .code(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://loinc.org")
                                          .code("8462-4")
                                          .display("Diastolic blood pressure")
                                          .build()))
                              .build())
                      .valueQuantity(
                          Quantity.builder()
                              .value(62.0)
                              .unit("mm[Hg]")
                              .system("http://unitsofmeasure.org")
                              .code("mm[Hg]")
                              .build())
                      .build(),
                  Observation.ObservationComponent.builder()
                      .code(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://loinc.org")
                                          .code("76-0")
                                          .display("CEFAZOLIN")
                                          .build()))
                              .text("CEFAZOLIN-1")
                              .build())
                      .valueCodeableConcept(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://snomed.info/sct")
                                          .code("S")
                                          .display("Sensitive")
                                          .build()))
                              .build())
                      .build(),
                  Observation.ObservationComponent.builder()
                      .code(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://loinc.org")
                                          .code("279-0")
                                          .display("IMIPENEM")
                                          .build()))
                              .text("IMIPENEM")
                              .build())
                      .valueCodeableConcept(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://snomed.info/sct")
                                          .code("S")
                                          .display("Sensitive")
                                          .build()))
                              .build())
                      .build(),
                  Observation.ObservationComponent.builder()
                      .code(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://loinc.org")
                                          .code("185-9")
                                          .display("CIPROFLOXACIN")
                                          .build()))
                              .text("CIPROFLOXACIN")
                              .build())
                      .valueCodeableConcept(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://snomed.info/sct")
                                          .code("S")
                                          .display("Sensitive")
                                          .build()))
                              .build())
                      .build(),
                  Observation.ObservationComponent.builder()
                      .code(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://loinc.org")
                                          .code("141-2")
                                          .display("CEFTRIAXONE")
                                          .build()))
                              .text("CEFTRIAXONE-3")
                              .build())
                      .valueCodeableConcept(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://snomed.info/sct")
                                          .code("S")
                                          .display("Sensitive")
                                          .build()))
                              .build())
                      .build(),
                  Observation.ObservationComponent.builder()
                      .code(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://loinc.org")
                                          .code("516-5")
                                          .display("TRIMETHOPRIM+SULFAMETHOXAZOLE")
                                          .build()))
                              .text("SXT (BACTRIM)")
                              .build())
                      .valueCodeableConcept(
                          CodeableConcept.builder()
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://snomed.info/sct")
                                          .code("R")
                                          .display("Resistant")
                                          .build()))
                              .build())
                      .build(),
                  Observation.ObservationComponent.builder()
                      .code(CodeableConcept.builder().text("Acid Fast Stain").build())
                      .valueString("Concentrate Negative")
                      .build(),
                  Observation.ObservationComponent.builder()
                      .code(CodeableConcept.builder().text("Sputum Screen").build())
                      .valueString("GOOD QUALITY SPECIMEN BY GRAM STAIN EVALUATION")
                      .build()))
          .build();
    }
  }
}
