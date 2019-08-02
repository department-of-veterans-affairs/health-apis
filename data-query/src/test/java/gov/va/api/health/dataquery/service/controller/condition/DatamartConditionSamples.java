package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.Condition.VerificationStatusCode;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.IcdCode;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.SnomedCode;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartConditionSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {
    public DatamartCondition condition() {
      return DatamartCondition.builder()
          .etlDate(Instant.parse("2011-06-27T05:40:00Z"))
          .cdwId("800274570575:D")
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference("666V666")
                  .display("VETERAN,FIRNM MINAM")
                  .build())
          .encounter(
              Optional.of(
                  DatamartReference.of()
                      .type("Encounter")
                      .reference("800285390250")
                      .display("Outpatient Visit")
                      .build()))
          .asserter(
              Optional.of(
                  DatamartReference.of()
                      .type("Practitioner")
                      .reference("1294265")
                      .display("DOCLANAM,DOCFIRNAM E")
                      .build()))
          .dateRecorded(Optional.of(LocalDate.parse("2011-06-27")))
          .snomed(Optional.of(snomedCode()))
          .icd(Optional.of(icd10Code()))
          .category(DatamartCondition.Category.diagnosis)
          .clinicalStatus(DatamartCondition.ClinicalStatus.active)
          .onsetDateTime(Optional.of(Instant.parse("2011-06-27T05:40:00Z")))
          .abatementDateTime(Optional.of(Instant.parse("2011-06-27T01:11:00Z")))
          .build();
    }

    IcdCode icd10Code() {
      return IcdCode.builder().code("N210").display("Calculus in bladder").version("10").build();
    }

    IcdCode icd9Code() {
      return IcdCode.builder()
          .code("263.9")
          .display("UNSPECIFIED PROTEIN-CALORIE MALNUTRITION")
          .version("9")
          .build();
    }

    SnomedCode snomedCode() {
      return SnomedCode.builder().code("70650003").display("Urinary bladder stone").build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {
    public Condition condition() {
      return Condition.builder()
          .resourceType("Condition")
          .abatementDateTime("2011-06-27T01:11:00Z")
          .asserter(reference("DOCLANAM,DOCFIRNAM E", "Practitioner/1294265"))
          .category(diagnosisCategory())
          .id("800274570575:D")
          .clinicalStatus(Condition.ClinicalStatusCode.active)
          .code(snomedCode())
          .dateRecorded("2011-06-27")
          .encounter(reference("Outpatient Visit", "Encounter/800285390250"))
          .onsetDateTime("2011-06-27T05:40:00Z")
          .patient(reference("VETERAN,FIRNM MINAM", "Patient/666V666"))
          .verificationStatus(VerificationStatusCode.unknown)
          .build();
    }

    CodeableConcept diagnosisCategory() {
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .code("diagnosis")
                      .display("Diagnosis")
                      .system("http://hl7.org/fhir/condition-category")
                      .build()))
          .text("Diagnosis")
          .build();
    }

    CodeableConcept icd10Code() {
      return CodeableConcept.builder()
          .text("Calculus in bladder")
          .coding(
              List.of(
                  Coding.builder()
                      .system("http://hl7.org/fhir/sid/icd-10")
                      .code("N210")
                      .display("Calculus in bladder")
                      .build()))
          .build();
    }

    CodeableConcept icd9Code() {
      return CodeableConcept.builder()
          .text("UNSPECIFIED PROTEIN-CALORIE MALNUTRITION")
          .coding(
              List.of(
                  Coding.builder()
                      .system("http://hl7.org/fhir/sid/icd-9-cm")
                      .code("263.9")
                      .display("UNSPECIFIED PROTEIN-CALORIE MALNUTRITION")
                      .build()))
          .build();
    }

    CodeableConcept problemCategory() {
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .code("problem")
                      .display("Problem")
                      .system("http://argonaut.hl7.org")
                      .build()))
          .text("Problem")
          .build();
    }

    Reference reference(String display, String ref) {
      return Reference.builder().display(display).reference(ref).build();
    }

    CodeableConcept snomedCode() {
      return CodeableConcept.builder()
          .text("Urinary bladder stone")
          .coding(
              List.of(
                  Coding.builder()
                      .system("https://snomed.info/sct")
                      .code("70650003")
                      .display("Urinary bladder stone")
                      .build()))
          .build();
    }
  }
}
