package gov.va.api.health.dataquery.tests.minimart;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceEntity;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportsEntity;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationEntity;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.service.controller.medication.MedicationEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementEntity;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dataquery.service.controller.observation.ObservationEntity;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dataquery.service.controller.patient.PatientEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientSearchEntity;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureEntity;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class MitreMinimartMaker {

  private String directory;

  private String resourceToSync;

  private EntityManager entityManager;

  public String determineFilePrefix(String resourceFullName) {
    String abbreviation = "";
    switch (resourceFullName) {
      case "AllergyIntolerance":
        abbreviation = "AllInt";
      case "Condition":
        abbreviation = "Con";
      case "DiagnosticReport":
        abbreviation = "DiaRep";
      case "Immunization":
        abbreviation = "Imm";
      case "MedicationStatement":
        abbreviation = "MedSta";
      case "MedicationOrder":
        abbreviation = "MedOrd";
      case "Medication":
        abbreviation = "Med";
      case "Observation":
        abbreviation = "Obs";
      case "Patient":
        abbreviation = "Pat";
      case "Procedure":
        abbreviation = "Pro";
      default:
        abbreviation = "--FAILURE--";
    }
    return "dm" + abbreviation;
  }

  @SneakyThrows
  public void insertByResourceType(String resourceName, File file) {
    switch (resourceName) {
      case "AllergyIntolerance":
        DatamartAllergyIntolerance ai =
            JacksonConfig.createMapper().readValue(file, DatamartAllergyIntolerance.class);
        AllergyIntoleranceEntity aiEntity =
            AllergyIntoleranceEntity.builder()
                .cdwId(ai.cdwId())
                .icn(patientIcn(ai.patient().isPresent() ? ai.patient().get() : null))
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(aiEntity);
      case "Condition":
        DatamartCondition cond =
            JacksonConfig.createMapper().readValue(file, DatamartCondition.class);
        ConditionEntity condEntity =
            ConditionEntity.builder()
                .cdwId(cond.cdwId())
                .icn(patientIcn(cond.patient()))
                .category(cond.category().toString())
                .clinicalStatus(cond.clinicalStatus().toString())
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(condEntity);
      case "DiagnosticReport":
        DatamartDiagnosticReports dr =
            JacksonConfig.createMapper().readValue(file, DatamartDiagnosticReports.class);
        DiagnosticReportsEntity drEntity =
            DiagnosticReportsEntity.builder()
                .icn(dr.fullIcn())
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(drEntity);
      case "Immunization":
        DatamartImmunization imm =
            JacksonConfig.createMapper().readValue(file, DatamartImmunization.class);
        ImmunizationEntity immEntity =
            ImmunizationEntity.builder()
                .cdwId(imm.cdwId())
                .icn(patientIcn(imm.patient()))
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(immEntity);
      case "Medication":
        DatamartMedication med =
            JacksonConfig.createMapper().readValue(file, DatamartMedication.class);
        MedicationEntity medEntity =
            MedicationEntity.builder()
                .cdwId(med.cdwId())
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(medEntity);
      case "MedicationOrder":
        DatamartMedicationOrder medOrd =
            JacksonConfig.createMapper().readValue(file, DatamartMedicationOrder.class);
        MedicationOrderEntity moEntity =
            MedicationOrderEntity.builder()
                .cdwId(medOrd.cdwId())
                .icn(patientIcn(medOrd.patient()))
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(moEntity);
      case "MedicationStatement":
        DatamartMedicationStatement medSta =
            JacksonConfig.createMapper().readValue(file, DatamartMedicationStatement.class);
        MedicationStatementEntity msEntity =
            MedicationStatementEntity.builder()
                .cdwId(medSta.cdwId())
                .icn(patientIcn(medSta.patient()))
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(msEntity);
      case "Observation":
        DatamartObservation obs =
            JacksonConfig.createMapper().readValue(file, DatamartObservation.class);
        ObservationEntity obsEntity =
            ObservationEntity.builder()
                .cdwId(obs.cdwId())
                .icn(obs.subject().isPresent() ? patientIcn(obs.subject().get()) : null)
                // .code(obs.code().isPresent() ? obs.code().get() : null)
                // .category(obs.category())
                // .epochTime(obs.effectiveDateTime())
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(obsEntity);
      case "Patient":
        DatamartPatient pat = JacksonConfig.createMapper().readValue(file, DatamartPatient.class);
        PatientSearchEntity patientSearchEntity =
            PatientSearchEntity.builder()
                .icn(pat.fullIcn())
                .firstName(pat.firstName())
                .lastName(pat.lastName())
                .name(pat.name())
                .birthDateTime(Instant.parse(pat.birthDateTime()))
                .gender(pat.gender())
                .build();
        PatientEntity patEntity =
            PatientEntity.builder()
                .icn(pat.fullIcn())
                .search(patientSearchEntity)
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(patEntity);
      case "Procedure":
        DatamartProcedure proc =
            JacksonConfig.createMapper().readValue(file, DatamartProcedure.class);
        ProcedureEntity procEntity =
            ProcedureEntity.builder()
                .cdwId(proc.cdwId())
                .icn(patientIcn(proc.patient()))
                // .performedOnEpochTime(proc.performedDateTime().isPresent() ?
                // proc.performedDateTime().get() : null)
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(procEntity);
      default:
        throw new RuntimeException("Couldnt determine resource type for file: " + resourceToSync);
    }
  }

  public String patientIcn(DatamartReference dm) {
    return dm != null && dm.reference().isPresent() ? dm.reference().get() : null;
  }

  public void prepareMitreResource() {
    File dmDirectory = new File(directory);
    List<File> dmFiles = Arrays.stream(dmDirectory.listFiles()).collect(Collectors.toList());
    dmFiles =
        dmFiles
            .stream()
            .filter(
                f ->
                    StringUtils.containsIgnoreCase(
                        f.getName(), determineFilePrefix(resourceToSync)))
            .collect(Collectors.toList());
    entityManager.getTransaction().begin();
    for (File file : dmFiles) {
      if (file.isFile()) {
        insertByResourceType(resourceToSync, file);
        entityManager.flush();
        entityManager.clear();
      }
    }
    entityManager.getTransaction().commit();
  }
}
