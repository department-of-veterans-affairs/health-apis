package gov.va.api.health.dataquery.tools.minimart;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceEntity;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportCrossEntity;
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
import gov.va.api.health.dataquery.tools.SqlServerDb;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MitreMinimartMaker {

  private final List<Class<?>> MANAGED_CLASSES =
      Arrays.asList(
          AllergyIntoleranceEntity.class
          //
          );

  private String resourceToSync;

  private EntityManager entityManager;

  public MitreMinimartMaker(String resourceToSync, String configFile) {
    this.resourceToSync = resourceToSync;
    this.entityManager = new SqlServerDb(configFile, MANAGED_CLASSES).get();
  }

  /** Main. */
  public static void main(String[] args) {
    String directory = args[1];
    MitreMinimartMaker mmm = new MitreMinimartMaker(args[0], args[2]);
    log.info("Syncing {} files in {} to db", mmm.resourceToSync, directory);
    mmm.pushToDatabaseByResourceType(directory);
    log.info("{} sync complete", mmm.resourceToSync);
    System.exit(0);
  }

  @SneakyThrows
  private String fileToString(File file) {
    return new String(Files.readAllBytes(Paths.get(file.getPath())));
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @SneakyThrows
  private void insertByAllergyIntolerance(File file) {
    DatamartAllergyIntolerance dm =
        JacksonConfig.createMapper().readValue(file, DatamartAllergyIntolerance.class);
    log.info("Processing AllergyIntolerance cdwId: {}", dm.cdwId());
    AllergyIntoleranceEntity entity =
        AllergyIntoleranceEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .payload(fileToString(file))
            .build();
    entityManager.persist(entity);
    flushAndClear();
  }

  @SneakyThrows
  private void insertByCondition(File file) {
    DatamartCondition dm = JacksonConfig.createMapper().readValue(file, DatamartCondition.class);
    log.info("Processing Condition cdwId: {}", dm.cdwId());
    ConditionEntity entity =
        ConditionEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .category(dm.category().toString())
            .clinicalStatus(dm.clinicalStatus().toString())
            .payload(fileToString(file))
            .build();
    entityManager.persist(entity);
    flushAndClear();
  }

  @SneakyThrows
  private void insertByDiagnosticReport(File file) {
    DatamartDiagnosticReports dm =
        JacksonConfig.createMapper().readValue(file, DatamartDiagnosticReports.class);
    // DR Entity
    entityManager.persist(
        DiagnosticReportsEntity.builder()
            .icn(dm.fullIcn())
            .payload(
                JacksonConfig.createMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(dm.reports()))
            .build());
    // DR Crosswalk Entities
    dm.reports()
        .forEach(
            report -> {
              log.info("Processing id {} for patient {}", report.identifier(), dm.fullIcn());
              entityManager.persist(
                  DiagnosticReportCrossEntity.builder()
                      .icn(dm.fullIcn())
                      .reportId(report.identifier())
                      .build());
            });
    flushAndClear();
  }

  @SneakyThrows
  private void insertByImmunization(File file) {
    DatamartImmunization dm =
        JacksonConfig.createMapper().readValue(file, DatamartImmunization.class);
    ImmunizationEntity entity =
        ImmunizationEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .payload(fileToString(file))
            .build();
    entityManager.persist(entity);
    flushAndClear();
  }

  @SneakyThrows
  private void insertByMedication(File file) {
    DatamartMedication dm = JacksonConfig.createMapper().readValue(file, DatamartMedication.class);
    MedicationEntity entity =
        MedicationEntity.builder().cdwId(dm.cdwId()).payload(fileToString(file)).build();
    entityManager.persist(entity);
    flushAndClear();
  }

  @SneakyThrows
  private void insertByMedicationStatement(File file) {
    DatamartMedicationStatement dm =
        JacksonConfig.createMapper().readValue(file, DatamartMedicationStatement.class);
    MedicationStatementEntity entity =
        MedicationStatementEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .payload(fileToString(file))
            .build();
    entityManager.persist(entity);
    flushAndClear();
  }

  @SneakyThrows
  private void insertByMedictionOrder(File file) {
    DatamartMedicationOrder dm =
        JacksonConfig.createMapper().readValue(file, DatamartMedicationOrder.class);
    MedicationOrderEntity entity =
        MedicationOrderEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .payload(fileToString(file))
            .build();
    entityManager.persist(entity);
    flushAndClear();
  }

  @SneakyThrows
  private void insertByObservation(File file) {
    DatamartObservation dm =
        JacksonConfig.createMapper().readValue(file, DatamartObservation.class);
    ObservationEntity entity =
        ObservationEntity.builder()
            .cdwId(dm.cdwId())
            .icn(dm.subject().isPresent() ? patientIcn(dm.subject().get()) : null)
            .payload(fileToString(file))
            .build();
    entityManager.persist(entity);
    flushAndClear();
  }

  @SneakyThrows
  private void insertByPatient(File file) {
    DatamartPatient dm = JacksonConfig.createMapper().readValue(file, DatamartPatient.class);
    PatientSearchEntity patientSearchEntity =
        PatientSearchEntity.builder()
            .icn(dm.fullIcn())
            .firstName(dm.firstName())
            .lastName(dm.lastName())
            .name(dm.name())
            .birthDateTime(Instant.parse(dm.birthDateTime()))
            .gender(dm.gender())
            .build();
    entityManager.persist(patientSearchEntity);
    PatientEntity patEntity =
        PatientEntity.builder()
            .icn(dm.fullIcn())
            .search(patientSearchEntity)
            .payload(fileToString(file))
            .build();
    entityManager.persist(patEntity);
    flushAndClear();
  }

  @SneakyThrows
  private void insertByProcedure(File file) {
    DatamartProcedure dm = JacksonConfig.createMapper().readValue(file, DatamartProcedure.class);
    ProcedureEntity entity =
        ProcedureEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .payload(fileToString(file))
            .build();
    entityManager.persist(entity);
    flushAndClear();
  }

  private List<File> listByPattern(File dmDirectory, String filePattern) {
    return Arrays.stream(dmDirectory.listFiles())
        .filter(File::isFile)
        .filter(f -> f.getName().matches(filePattern))
        .collect(Collectors.toList());
  }

  private String patientIcn(DatamartReference dm) {
    if (dm != null && dm.reference().isPresent()) {
      return dm.reference().get().replaceAll("http.*/fhir/v0/dstu2/Patient/", "");
    }
    return null;
  }

  private void pushToDatabaseByResourceType(String directory) {
    File dmDirectory = new File(directory);
    if (dmDirectory.listFiles() == null) {
      log.error("No files in directory {}", directory);
      throw new RuntimeException("No files found in directory: " + directory);
    }
    entityManager.getTransaction().begin();
    switch (resourceToSync) {
      case "AllergyIntolerance":
        listByPattern(dmDirectory, "^dmAllInt.*json$")
            .forEach(file -> insertByAllergyIntolerance(file));
        break;
      case "Condition":
        listByPattern(dmDirectory, "^dmCon.*json$").forEach(file -> insertByCondition(file));
        break;
      case "DiagnosticReport":
        listByPattern(dmDirectory, "^dmDiaRep.*json$")
            .forEach(file -> insertByDiagnosticReport(file));
        break;
      case "Immunization":
        listByPattern(dmDirectory, "^dmImm.*json$").forEach(file -> insertByImmunization(file));
        break;
      case "Medication":
        listByPattern(dmDirectory, "^dmMed(?!Sta|Ord).*json$")
            .forEach(file -> insertByMedication(file));
        break;
      case "MedicationOrder":
        listByPattern(dmDirectory, "^dmMedOrd.*json$")
            .forEach(file -> insertByMedictionOrder(file));
        break;
      case "MedicationStatement":
        listByPattern(dmDirectory, "^dmMedSta.*json$")
            .forEach(file -> insertByMedicationStatement(file));
        break;
      case "Observation":
        listByPattern(dmDirectory, "^dmObs.*json$").forEach(file -> insertByObservation(file));
        break;
      case "Patient":
        listByPattern(dmDirectory, "^dmPat.*json$").forEach(file -> insertByPatient(file));
        break;
      case "Procedure":
        listByPattern(dmDirectory, "^dmPro.*json$").forEach(file -> insertByProcedure(file));
        break;
      default:
        throw new RuntimeException("Couldnt determine resource type for file: " + resourceToSync);
    }
    // Commit changes to db
    entityManager.getTransaction().commit();
    entityManager.close();
  }
}
