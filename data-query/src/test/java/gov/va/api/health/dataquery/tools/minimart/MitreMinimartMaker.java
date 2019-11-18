package gov.va.api.health.dataquery.tools.minimart;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceEntity;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartEntity;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReports;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportCrossEntity;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportsEntity;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationEntity;
import gov.va.api.health.dataquery.service.controller.location.DatamartLocation;
import gov.va.api.health.dataquery.service.controller.location.LocationEntity;
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
import gov.va.api.health.dataquery.tools.ExternalDb;
import gov.va.api.health.dataquery.tools.LocalH2;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
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
          AllergyIntoleranceEntity.class,
          ConditionEntity.class,
          DiagnosticReportsEntity.class,
          DiagnosticReportCrossEntity.class,
          ImmunizationEntity.class,
          LocationEntity.class,
          MedicationOrderEntity.class,
          MedicationEntity.class,
          MedicationStatementEntity.class,
          ObservationEntity.class,
          PatientEntity.class,
          PatientSearchEntity.class,
          ProcedureEntity.class);

  private String resourceToSync;

  private EntityManager entityManager;

  private MitreMinimartMaker(String resourceToSync, String configFile) {
    this.resourceToSync = resourceToSync;
    if (configFile == null || configFile.isBlank()) {
      log.info("No config file was specified... Defaulting to local h2 database...");
      this.entityManager = new LocalH2("./target/minimart", MANAGED_CLASSES).get();
    } else {
      this.entityManager = new ExternalDb(configFile, MANAGED_CLASSES).get();
    }
  }

  /** Main. */
  public static void main(String[] args) {
    if (args.length > 3 || args.length < 2) {
      throw new RuntimeException(
          "Missing command line arguments. Expected <resource-type> <input-directory> <external-db-config>");
    }
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

  @SneakyThrows
  private void insertByAllergyIntolerance(File file) {
    DatamartAllergyIntolerance dm =
        JacksonConfig.createMapper().readValue(file, DatamartAllergyIntolerance.class);
    AllergyIntoleranceEntity entity =
        AllergyIntoleranceEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .payload(fileToString(file))
            .build();
    save(entity);
  }

  @SneakyThrows
  private void insertByCondition(File file) {
    DatamartCondition dm = JacksonConfig.createMapper().readValue(file, DatamartCondition.class);
    ConditionEntity entity =
        ConditionEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .category(dm.category().toString())
            .clinicalStatus(dm.clinicalStatus().toString())
            .payload(fileToString(file))
            .build();
    save(entity);
  }

  @SneakyThrows
  // For the sake of updates, we'll rebuild it each time, this follows the other resources
  private void insertByDiagnosticReport(List<File> files) {
    // Set the icn and other values using the first file, then reset the payload before loading all
    // the files.
    DatamartDiagnosticReports dm =
        files.size() > 0
            ? JacksonConfig.createMapper().readValue(files.get(0), DatamartDiagnosticReports.class)
            : null;
    if (dm == null) {
      throw new RuntimeException("Couldn't find any Diagnostic Reports to push to database.");
    }
    dm.reports(new ArrayList<>());
    // Crosswalk Entities are dealt with below
    files.forEach(
        file -> {
          try {
            DatamartDiagnosticReports tmpDr =
                JacksonConfig.createMapper().readValue(file, DatamartDiagnosticReports.class);
            for (DatamartDiagnosticReports.DiagnosticReport report : tmpDr.reports()) {
              saveDrCrosswalkEntity(dm.fullIcn(), report.identifier());
              dm.reports().add(report);
            }
          } catch (IOException e) {
            log.error("Couldnt process file {}", file.getName());
            throw new RuntimeException("Couldnt process file as Diagnostic Report... Quitting...");
          }
        });
    // DR Entity
    save(
        DiagnosticReportsEntity.builder()
            .icn(dm.fullIcn())
            .payload(JacksonConfig.createMapper().writeValueAsString(dm))
            .build());
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
    save(entity);
  }

  @SneakyThrows
  private void insertByLocation(File file) {
    DatamartLocation dm = JacksonConfig.createMapper().readValue(file, DatamartLocation.class);
    LocationEntity entity =
        LocationEntity.builder()
            .cdwId(dm.cdwId())
            .name(dm.name())
            .street(dm.address().line1())
            .city(dm.address().city())
            .state(dm.address().state())
            .postalCode(dm.address().postalCode())
            .payload(fileToString(file))
            .build();
    save(entity);
  }

  @SneakyThrows
  private void insertByMedication(File file) {
    DatamartMedication dm = JacksonConfig.createMapper().readValue(file, DatamartMedication.class);
    MedicationEntity entity =
        MedicationEntity.builder().cdwId(dm.cdwId()).payload(fileToString(file)).build();
    save(entity);
  }

  @SneakyThrows
  private void insertByMedicationOrder(File file) {
    DatamartMedicationOrder dm =
        JacksonConfig.createMapper().readValue(file, DatamartMedicationOrder.class);
    MedicationOrderEntity entity =
        MedicationOrderEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .payload(fileToString(file))
            .build();
    save(entity);
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
    save(entity);
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
    save(entity);
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
    save(patientSearchEntity);
    PatientEntity patEntity =
        PatientEntity.builder()
            .icn(dm.fullIcn())
            .search(patientSearchEntity)
            .payload(fileToString(file))
            .build();
    save(patEntity);
  }

  @SneakyThrows
  private void insertByProcedure(File file) {
    DatamartProcedure dm = JacksonConfig.createMapper().readValue(file, DatamartProcedure.class);
    Long performedOnEpoch =
        dm.performedDateTime().isPresent() ? dm.performedDateTime().get().toEpochMilli() : null;
    ProcedureEntity entity =
        ProcedureEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient()))
            .performedOnEpochTime(performedOnEpoch)
            .payload(fileToString(file))
            .build();
    save(entity);
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
        insertByDiagnosticReport(listByPattern(dmDirectory, "^dmDiaRep.*json$"));
        break;
      case "Immunization":
        listByPattern(dmDirectory, "^dmImm.*json$").forEach(file -> insertByImmunization(file));
        break;
      case "Location":
        listByPattern(dmDirectory, "^dmLoc.*json$").forEach(file -> insertByLocation(file));
        break;
      case "Medication":
        listByPattern(dmDirectory, "^dmMed(?!Sta|Ord).*json$")
            .forEach(file -> insertByMedication(file));
        break;
      case "MedicationOrder":
        listByPattern(dmDirectory, "^dmMedOrd.*json$")
            .forEach(file -> insertByMedicationOrder(file));
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

  private <T extends DatamartEntity> void save(T entity) {
    DatamartEntity existing = entityManager.find(entity.getClass(), entity.cdwId());
    if (existing == null) {
      log.info("Adding {} {}", entity.getClass().getSimpleName(), entity.cdwId());
      entityManager.persist(entity);
    } else {
      log.info("Updating {} {}", entity.getClass().getSimpleName(), entity.cdwId());
      entityManager.merge(entity);
    }
    entityManager.flush();
    entityManager.clear();
  }

  private void saveDrCrosswalkEntity(String icn, String reportIdentifier) {
    save(DiagnosticReportCrossEntity.builder().icn(icn).reportId(reportIdentifier).build());
  }
}
