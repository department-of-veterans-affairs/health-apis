package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static java.util.Collections.singletonList;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement.Dosage;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement.Status;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept.CodeableConceptBuilder;
import gov.va.api.health.dstu2.api.datatypes.Timing;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartMedicationStatementSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {
    public DatamartMedicationStatement medicationStatement() {
      return medicationStatement("800008482786", "666V666", "2019-07-01");
    }

    public DatamartMedicationStatement medicationStatement(String cdwId, String patientId, String dateRecorded) {
      return DatamartMedicationStatement.builder()
          .cdwId(cdwId)
          .etlDate("2014-12-06T05:53:02Z")
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference("1004810366V403573")
                  .display("BARKER,BOBBIE LEE")
                  .build())
          .dateAsserted(Instant.parse("2017-11-03T01:39:21Z"))
          .status(DatamartMedicationStatement.Status.completed)
          .effectiveDateTime(Optional.of(Instant.parse("2017-11-03T01:39:21Z")))
          .note(Optional.of("NOTES NOTES NOTES"))
          .medication(
              DatamartReference.of()
                  .type("Medication")
                  .reference("123456789")
                  .display("SAW PALMETTO")
                  .build())
          .dosage(
              DatamartMedicationStatement.Dosage.builder()
                  .text(Optional.of("1"))
                  .timingCodeText(Optional.of("EVERYDAY"))
                  .routeText(Optional.of("MOUTH"))
                  .build())
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {

    public MedicationStatement medicationStatement() {
      return medicationStatement("800008482786");
    }

    public MedicationStatement medicationStatement(String id) {
      return medicationStatement(id, "666V666", "2019-07-01");
    }

    public MedicationStatement medicationStatement(String id, String patiendId, String dateRecorded) {
      return MedicationStatement.builder()
          .resourceType("MedicationStatement")
          .id(id)
          .patient(reference("Patient/1004810366V403573", "BARKER,BOBBIE LEE"))
          .dateAsserted("2017-11-03T01:39:21Z")
          .note("NOTES NOTES NOTES")
          .status(MedicationStatement.Status.completed)
          .effectiveDateTime("2017-11-03T01:39:21Z")
          .medicationReference(reference("Medication/123456789", "SAW PALMETTO"))
          .dosage(Dosage())
          .build();
    }

    private List<MedicationStatement.Dosage> Dosage() {
      return singletonList(
          MedicationStatement.Dosage.builder().route(route()).text("1").timing(timing()).build());
    }

    private CodeableConcept route() {
      return CodeableConcept.builder().text("MOUTH").build();
    }

    private Timing timing() {
      return Timing.builder().code(timingCode()).build();
    }

    private CodeableConcept timingCode() {
      return CodeableConcept.builder().text("EVERYDAY").build();
    }

    private Reference reference(String ref, String display) {
      return Reference.builder().reference(ref).display(display).build();
    }

  }
}
