package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.Condition.Bundle;
import gov.va.api.health.argonaut.api.resources.Condition.Entry;
import gov.va.api.health.argonaut.api.resources.Condition.VerificationStatusCode;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.IcdCode;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.SnomedCode;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConditionSamples {
  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    public DatamartCondition condition() {
      return condition("800274570575:D", "666V666", "2011-06-27");
    }

    public DatamartCondition condition(String cdwId, String patientId, String dateRecorded) {
      return DatamartCondition.builder()
          .cdwId(cdwId)
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference(patientId)
                  .display("VETERAN,FIRNM MINAM")
                  .build())
          .asserter(
              Optional.of(
                  DatamartReference.of()
                      .type("Practitioner")
                      .reference("1294265")
                      .display("DOCLANAM,DOCFIRNAM E")
                      .build()))
          .dateRecorded(Optional.of(LocalDate.parse(dateRecorded)))
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
  public static class Dstu2 {
    static Condition.Bundle asBundle(
        String baseUrl, Collection<Condition> conditions, int totalRecords, BundleLink... links) {
      return Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              conditions.stream()
                  .map(
                      c ->
                          Entry.builder()
                              .fullUrl(baseUrl + "/Condition/" + c.id())
                              .resource(c)
                              .search(Search.builder().mode(SearchMode.match).build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static BundleLink link(LinkRelation rel, String base, int page, int count) {
      return BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public Condition condition() {
      return condition("800274570575:D");
    }

    public Condition condition(String id) {
      return condition(id, "666V666", "2011-06-27");
    }

    public Condition condition(String id, String patientId, String dateRecorded) {
      return Condition.builder()
          .resourceType("Condition")
          .abatementDateTime("2011-06-27T01:11:00Z")
          .asserter(reference("DOCLANAM,DOCFIRNAM E", "Practitioner/1294265"))
          .category(diagnosisCategory())
          .id(id)
          .clinicalStatus(Condition.ClinicalStatusCode.active)
          .code(snomedCode())
          .dateRecorded(dateRecorded)
          .onsetDateTime("2011-06-27T05:40:00Z")
          .patient(reference("VETERAN,FIRNM MINAM", "Patient/" + patientId))
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
                      .system("http://snomed.info/sct")
                      .code("70650003")
                      .display("Urinary bladder stone")
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    static gov.va.api.health.uscorer4.api.resources.Condition.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.uscorer4.api.resources.Condition> conditions,
        int totalRecords,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return gov.va.api.health.uscorer4.api.resources.Condition.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              conditions.stream()
                  .map(
                      c ->
                          gov.va.api.health.uscorer4.api.resources.Condition.Entry.builder()
                              .fullUrl(baseUrl + "/Condition/" + c.id())
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

    public static gov.va.api.health.r4.api.bundle.BundleLink link(
        gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.r4.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    List<gov.va.api.health.r4.api.datatypes.CodeableConcept> categoryDiagnosis() {
      return List.of(
          gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
              .text("Encounter Diagnosis")
              .coding(
                  List.of(
                      gov.va.api.health.r4.api.datatypes.Coding.builder()
                          .display("Encounter Diagnosis")
                          .code("encounter-diagnosis")
                          .system("http://terminology.hl7.org/CodeSystem/condition-category")
                          .build()))
              .build());
    }

    List<gov.va.api.health.r4.api.datatypes.CodeableConcept> categoryProblem() {
      return List.of(
          gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
              .text("Problem List Item")
              .coding(
                  List.of(
                      gov.va.api.health.r4.api.datatypes.Coding.builder()
                          .display("Problem List Item")
                          .code("problem-list-item")
                          .system("http://terminology.hl7.org/CodeSystem/condition-category")
                          .build()))
              .build());
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept clinicalStatusActive() {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text("Active")
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .code("active")
                      .display("Active")
                      .system("http://hl7.org/fhir/R4/codesystem-condition-clinical.html")
                      .build()))
          .build();
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept clinicalStatusResolved() {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text("Resolved")
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .code("resolved")
                      .display("Resolved")
                      .system("http://hl7.org/fhir/R4/codesystem-condition-clinical.html")
                      .build()))
          .build();
    }

    public gov.va.api.health.uscorer4.api.resources.Condition condition(
        String id, String patientId, String recordedDate) {
      return gov.va.api.health.uscorer4.api.resources.Condition.builder()
          .resourceType("Condition")
          .id(id)
          .subject(reference("VETERAN,FIRNM MINAM", "Patient/" + patientId))
          .asserter(reference("DOCLANAM,DOCFIRNAM E", "Practitioner/1294265"))
          .recordedDate(recordedDate)
          .code(snomedCode())
          .category(categoryDiagnosis())
          .clinicalStatus(clinicalStatusActive())
          .onsetDateTime("2011-06-27T05:40:00Z")
          .abatementDateTime("2011-06-27T01:11:00Z")
          .verificationStatus(verificationStatus())
          .build();
    }

    public gov.va.api.health.uscorer4.api.resources.Condition condition() {
      return condition("800274570575:D");
    }

    public gov.va.api.health.uscorer4.api.resources.Condition condition(String id) {
      return condition(id, "666V666", "2011-06-27");
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept icd10Code() {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text("Calculus in bladder")
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .system("http://hl7.org/fhir/sid/icd-10")
                      .code("N210")
                      .display("Calculus in bladder")
                      .build()))
          .build();
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept icd9Code() {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text("UNSPECIFIED PROTEIN-CALORIE MALNUTRITION")
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .system("http://hl7.org/fhir/sid/icd-9-cm")
                      .code("263.9")
                      .display("UNSPECIFIED PROTEIN-CALORIE MALNUTRITION")
                      .build()))
          .build();
    }

    private gov.va.api.health.r4.api.elements.Reference reference(String display, String ref) {
      return gov.va.api.health.r4.api.elements.Reference.builder()
          .display(display)
          .reference(ref)
          .build();
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept snomedCode() {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text("Urinary bladder stone")
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .system("http://snomed.info/sct")
                      .code("70650003")
                      .display("Urinary bladder stone")
                      .build()))
          .build();
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept verificationStatus() {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text("Unconfirmed")
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .code("unconfirmed")
                      .system("http://hl7.org/fhir/R4/codesystem-condition-ver-status.html")
                      .display("Unconfirmed")
                      .build()))
          .build();
    }
  }
}
