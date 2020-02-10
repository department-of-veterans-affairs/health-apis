package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Dosage;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Timing;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.junit.Test;

public class Dstu2MedicationStatementTransformerTest {
  @Test
  public void codeableConcept() {
    Dstu2MedicationStatementTransformer tx =
        Dstu2MedicationStatementTransformer.builder()
            .datamart(Datamart.create().medicationStatement())
            .build();
    assertThat(tx.codeableConcept(Optional.empty())).isNull();
    assertThat(tx.codeableConcept(Optional.of("x")))
        .isEqualTo(CodeableConcept.builder().text("x").build());
  }

  @Test
  public void dosage() {
    Dstu2MedicationStatementTransformer tx =
        Dstu2MedicationStatementTransformer.builder()
            .datamart(Datamart.create().medicationStatement())
            .build();
    assertThat(tx.dosage(null)).isNull();
    assertThat(tx.dosage(Datamart.create().dosage())).isEqualTo(List.of(Fhir.create().dosage()));
  }

  @Test
  public void empty() {
    assertThat(tx(DatamartMedicationStatement.builder().build()).toFhir())
        .isEqualTo(MedicationStatement.builder().resourceType("MedicationStatement").build());
  }

  @Test
  public void medicationStatement() {
    assertThat(tx(Datamart.create().medicationStatement()).toFhir())
        .isEqualTo(Fhir.create().medicationStatement());
  }

  @Test
  public void status() {
    Dstu2MedicationStatementTransformer tx =
        Dstu2MedicationStatementTransformer.builder()
            .datamart(Datamart.create().medicationStatement())
            .build();
    assertThat(tx.status(null)).isNull();
    assertThat(tx.status(DatamartMedicationStatement.Status.active))
        .isEqualTo(MedicationStatement.Status.active);
  }

  @Test
  public void timing() {
    Dstu2MedicationStatementTransformer tx =
        Dstu2MedicationStatementTransformer.builder()
            .datamart(Datamart.create().medicationStatement())
            .build();
    assertThat(tx.timing(Optional.empty())).isNull();
    assertThat(tx.timing(Optional.of("x")))
        .isEqualTo(Timing.builder().code(CodeableConcept.builder().text("x").build()).build());
  }

  private Dstu2MedicationStatementTransformer tx(DatamartMedicationStatement dm) {
    return Dstu2MedicationStatementTransformer.builder().datamart(dm).build();
  }

  @AllArgsConstructor(staticName = "create")
  static class Datamart {
    DatamartMedicationStatement.Dosage dosage() {
      return DatamartMedicationStatement.Dosage.builder()
          .text(Optional.of("1"))
          .timingCodeText(Optional.of("EVERYDAY"))
          .routeText(Optional.of("MOUTH"))
          .build();
    }

    DatamartMedicationStatement medicationStatement() {
      return DatamartMedicationStatement.builder()
          .cdwId("800008482786")
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference("1004810366V403573")
                  .display("BARKER,BOBBIE LEE")
                  .build())
          .dateAsserted(Instant.parse("2017-11-03T01:39:21Z"))
          .status(DatamartMedicationStatement.Status.completed)
          .effectiveDateTime(Optional.of(Instant.parse("2017-11-03T01:39:21Z")))
          .note(Optional.of("notes notes notes"))
          .medication(
              DatamartReference.of()
                  .type("Medication")
                  .reference(null)
                  .display("SAW PALMETTO")
                  .build())
          .dosage(dosage())
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {
    CodeableConcept codeableConcept(String text) {
      return CodeableConcept.builder().text(text).build();
    }

    MedicationStatement.Dosage dosage() {
      return Dosage.builder()
          .route(codeableConcept("MOUTH"))
          .text("1")
          .timing(timing("EVERYDAY"))
          .build();
    }

    MedicationStatement medicationStatement() {
      return MedicationStatement.builder()
          .resourceType("MedicationStatement")
          .id("800008482786")
          .dateAsserted("2017-11-03T01:39:21Z")
          .dosage(List.of(dosage()))
          .effectiveDateTime("2017-11-03T01:39:21Z")
          .medicationReference(reference("SAW PALMETTO"))
          .note("notes notes notes")
          .patient(reference("BARKER,BOBBIE LEE", "Patient/1004810366V403573"))
          .status(MedicationStatement.Status.completed)
          .build();
    }

    Reference reference(String display, String ref) {
      return Reference.builder().display(display).reference(ref).build();
    }

    Reference reference(String display) {
      return Reference.builder().display(display).build();
    }

    Timing timing(String code) {
      return Timing.builder().code(codeableConcept(code)).build();
    }
  }
}
