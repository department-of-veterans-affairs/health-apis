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
import gov.va.api.health.dataquery.tools.DatamartExporter;
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

  private static EntityManager entityManager;

  @SneakyThrows
  private static String fileToString(File file) {
    return new String(Files.readAllBytes(Paths.get(file.getPath())));
  }

  private static void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @SneakyThrows
  private static void insertByAllergyIntolerance(File file) {
    DatamartAllergyIntolerance dm =
        JacksonConfig.createMapper().readValue(file, DatamartAllergyIntolerance.class);
    log.info(
        "Processing cdwId ({}) for icn ({})",
        dm.cdwId(),
        patientIcn(dm.patient().isPresent() ? dm.patient().get() : null));
    AllergyIntoleranceEntity entity =
        AllergyIntoleranceEntity.builder()
            .cdwId(dm.cdwId())
            .icn(patientIcn(dm.patient().isPresent() ? dm.patient().get() : null))
            .payload(fileToString(file))
            .build();
    entityManager.persist(entity);
    flushAndClear();
  }

  @SneakyThrows
  private static void insertByCondition(File file) {
    DatamartCondition dm = JacksonConfig.createMapper().readValue(file, DatamartCondition.class);
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
  private static void insertByDiagnosticReport(File file) {
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
        .stream()
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
  private static void insertByImmunization(File file) {
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
  private static void insertByMedication(File file) {
    DatamartMedication dm = JacksonConfig.createMapper().readValue(file, DatamartMedication.class);
    MedicationEntity entity =
        MedicationEntity.builder().cdwId(dm.cdwId()).payload(fileToString(file)).build();
    entityManager.persist(entity);
    flushAndClear();
  }

  @SneakyThrows
  private static void insertByMedicationStatement(File file) {
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
  private static void insertByMedictionOrder(File file) {
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
  private static void insertByObservation(File file) {
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
  private static void insertByPatient(File file) {
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
  private static void insertByProcedure(File file) {
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

  /** Main. */
  public static void main(String[] args) {
    if (args.length != 3) {
      throw new RuntimeException("Arg Count Incorrect: " + args.length);
    }
    String resourceToSync = args[0];
    String directory = args[1];
    entityManager = DatamartExporter.getH2(args[2]);
    log.info("Syncing {} file in {} to {}", resourceToSync, directory, args[2]);
    File dmDirectory = new File(directory);
    if (dmDirectory.listFiles() == null) {
      log.error("No files in directory {}", directory);
      throw new RuntimeException("No files found in directory: " + directory);
    }
    List<File> dmFiles = Arrays.stream(dmDirectory.listFiles()).collect(Collectors.toList());
    entityManager.getTransaction().begin();
    switch (resourceToSync) {
      case "AllergyIntolerance":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmAllInt.*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByAllergyIntolerance);
        break;
      case "Condition":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmCon.*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByCondition);
        break;
      case "DiagnosticReport":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmDiaRep.*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByDiagnosticReport);
        break;
      case "Immunization":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmImm.*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByImmunization);
        break;
      case "Medication":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmMed(?!Sta|Ord).*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByMedication);
        break;
      case "MedicationOrder":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmMedOrd.*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByMedictionOrder);
        break;
      case "MedicationStatement":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmMedSta.*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByMedicationStatement);
        break;
      case "Observation":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmObs.*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByObservation);
        break;
      case "Patient":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmPat.*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByPatient);
        break;
      case "Procedure":
        dmFiles
            .stream()
            .filter(f -> f.getName().matches("^dmPro.*json$"))
            .filter(File::isFile)
            .collect(Collectors.toList())
            .forEach(MitreMinimartMaker::insertByProcedure);
        break;
      default:
        throw new RuntimeException("Couldnt determine resource type for file: " + resourceToSync);
    }
    entityManager.getTransaction().commit();
    log.info("{} sync complete", resourceToSync);
    System.exit(0);
  }

  private static String patientIcn(DatamartReference dm) {
    return dm != null && dm.reference().isPresent() ? dm.reference().get() : null;
  }
}
