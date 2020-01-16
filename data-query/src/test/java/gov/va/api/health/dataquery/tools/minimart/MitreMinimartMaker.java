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
import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization;
import gov.va.api.health.dataquery.service.controller.organization.OrganizationEntity;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.dataquery.service.controller.patient.PatientEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientSearchEntity;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureEntity;
import gov.va.api.health.dataquery.tools.ExternalDb;
import gov.va.api.health.dataquery.tools.LocalH2;
import gov.va.api.health.fallrisk.service.controller.DatamartSurvey;
import gov.va.api.health.fallrisk.service.controller.SurveyEntity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class MitreMinimartMaker {

  private static final ThreadLocal<EntityManager> LOCAL_ENTITY_MANAGER = new ThreadLocal<>();

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
          OrganizationEntity.class,
          PatientEntity.class,
          PatientSearchEntity.class,
          PractitionerEntity.class,
          ProcedureEntity.class,
          SurveyEntity.class);

  private String resourceToSync;

  private EntityManagerFactory entityManagerFactory;

  private List<EntityManager> entityManagers;

  private AtomicInteger addedCount = new AtomicInteger(0);

  private MitreMinimartMaker(String resourceToSync, String configFile) {
    this.resourceToSync = resourceToSync;
    if (configFile == null || configFile.isBlank()) {
      log.info("No config file was specified... Defaulting to local h2 database...");
      this.entityManagerFactory = new LocalH2("./target/minimart", MANAGED_CLASSES).get();
    } else {
      this.entityManagerFactory = new ExternalDb(configFile, MANAGED_CLASSES).get();
    }
    this.entityManagers = Collections.synchronizedList(new ArrayList<>());
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
  private Stream<File> findUniqueFiles(File dmDirectory, String filePattern) {
    List<File> files =
        Files.walk(dmDirectory.toPath())
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(f -> f.getName().matches(filePattern))
            .collect(Collectors.toList());
    Set<String> fileNames = new HashSet<>();
    List<File> uniqueFiles = new ArrayList<>();
    for (File file : files) {
      if (fileNames.add(file.getName())) {
        uniqueFiles.add(file);
      }
    }
    log.info("{} unique files found", uniqueFiles.size());
    return uniqueFiles.stream();
  }

  private EntityManager getEntityManager() {
    if (LOCAL_ENTITY_MANAGER.get() == null) {
      LOCAL_ENTITY_MANAGER.set(entityManagerFactory.createEntityManager());
      LOCAL_ENTITY_MANAGER.get().getTransaction().begin();
      entityManagers.add(LOCAL_ENTITY_MANAGER.get());
    }
    return LOCAL_ENTITY_MANAGER.get();
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
  private void insertByDiagnosticReport(File dmDirectory) {
    /*
     * Diagnostic reports need to be done by patient, so if we were given a
     * patient directory, continue processing as normal, if not we were probably
     * given the parent datamart directory for all patients, so try to process
     * for each directory.
     */
    List<File> files = listDiagnosticReportFiles(dmDirectory);
    if (files.isEmpty()) {
      Files.walk(dmDirectory.toPath())
          .map(Path::toFile)
          .filter(File::isDirectory)
          .parallel()
          .forEach(f -> insertByDiagnosticReport(listDiagnosticReportFiles(f)));
    } else {
      insertByDiagnosticReport(files);
    }
  }

  /* For the sake of updates, we'll rebuild it each time, this follows the other resources */
  @SneakyThrows
  private void insertByDiagnosticReport(List<File> files) {
    // Set the icn and other values using the first file, then reset the payload before loading all
    // the files.
    DatamartDiagnosticReports dm =
        files.size() > 0
            ? JacksonConfig.createMapper().readValue(files.get(0), DatamartDiagnosticReports.class)
            : null;
    if (dm == null) {
      /*
       * NOTHING TO SEE HERE, MOVE ALONG SIR
       *
       * Because we are running these in parallel, just return here if no files were given
       * for this patient.
       */
      return;
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
            .category(dm.category().name())
            .code(
                dm.code().isPresent() && dm.code().get().coding().isPresent()
                    ? dm.code().get().coding().get().code().orElse(null)
                    : null)
            .build();
    save(entity);
  }

  @SneakyThrows
  private void insertByOrganization(File file) {
    DatamartOrganization dm =
        JacksonConfig.createMapper().readValue(file, DatamartOrganization.class);
    OrganizationEntity entity =
        OrganizationEntity.builder()
            .cdwId(dm.cdwId())
            .npi(dm.npi().orElse(null))
            .providerId(dm.providerId().orElse(null))
            .ediId(dm.ediId().orElse(null))
            .agencyId(dm.agencyId().orElse(null))
            .payload(fileToString(file))
            .street(
                StringUtils.trimToNull(
                    StringUtils.trimToEmpty(dm.address().line1())
                        + " "
                        + StringUtils.trimToEmpty(dm.address().line2())))
            .name(dm.name())
            .city(dm.address().city())
            .state(dm.address().state())
            .postalCode(dm.address().postalCode())
            .build();
    save(entity);
  }

  @SneakyThrows
  private void insertByPatient(File file) {
    DatamartPatient dm = JacksonConfig.createMapper().readValue(file, DatamartPatient.class);
    PatientEntity patEntity =
        PatientEntity.builder().icn(dm.fullIcn()).payload(fileToString(file)).build();
    save(patEntity);
    PatientSearchEntity patientSearchEntity =
        PatientSearchEntity.builder()
            .icn(dm.fullIcn())
            .firstName(dm.firstName())
            .lastName(dm.lastName())
            .name(dm.name())
            .birthDateTime(Instant.parse(dm.birthDateTime()))
            .gender(dm.gender())
            .patient(patEntity)
            .build();
    save(patientSearchEntity);
  }

  @SneakyThrows
  private void insertByPractitioner(File file) {
    DatamartPractitioner dm =
        JacksonConfig.createMapper().readValue(file, DatamartPractitioner.class);
    PractitionerEntity entity =
        PractitionerEntity.builder()
            .cdwId(dm.cdwId())
            .npi(dm.npi().orElse(null))
            .familyName(dm.name().family())
            .givenName(dm.name().given())
            .payload(fileToString(file))
            .build();
    save(entity);
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

  @SneakyThrows
  private void insertBySurvey(File file) {
    DatamartSurvey dm = JacksonConfig.createMapper().readValue(file, DatamartSurvey.class);
    String cdwId = dm.getCdwId();
    SurveyEntity entity =
        SurveyEntity.builder()
            .cdwId(cdwId)
            .patientFullIcn(dm.getPatientFullIcn())
            .surveyName(dm.getSurveyName())
            .sta3n(dm.getSta3n())
            .surveySavedDateTime(dm.getSurveySavedDateTime().toEpochMilli())
            .payload(fileToString(file))
            .build();
    save(entity, cdwId);
  }

  @SneakyThrows
  private void insertResourceByPattern(
      File dmDirectory, String filePattern, Consumer<File> fileWriter) {
    findUniqueFiles(dmDirectory, filePattern).parallel().forEach(fileWriter);
  }

  @SneakyThrows
  private List<File> listDiagnosticReportFiles(File dmDirectory) {
    return Arrays.stream(dmDirectory.listFiles())
        .filter(File::isFile)
        .filter(f -> f.getName().matches("^dmDiaRep.*json$"))
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
    switch (resourceToSync) {
      case "AllergyIntolerance":
        insertResourceByPattern(dmDirectory, "^dmAllInt.*json$", this::insertByAllergyIntolerance);
        break;
      case "Condition":
        insertResourceByPattern(dmDirectory, "^dmCon.*json$", this::insertByCondition);
        break;
      case "DiagnosticReport":
        insertByDiagnosticReport(dmDirectory);
        break;
      case "Immunization":
        insertResourceByPattern(dmDirectory, "^dmImm.*json$", this::insertByImmunization);
        break;
      case "Location":
        insertResourceByPattern(dmDirectory, "^dmLoc.*json$", this::insertByLocation);
        break;
      case "Medication":
        insertResourceByPattern(dmDirectory, "^dmMed(?!Sta|Ord).*json$", this::insertByMedication);
        break;
      case "MedicationOrder":
        insertResourceByPattern(dmDirectory, "^dmMedOrd.*json$", this::insertByMedicationOrder);
        break;
      case "MedicationStatement":
        insertResourceByPattern(dmDirectory, "^dmMedSta.*json$", this::insertByMedicationStatement);
        break;
      case "Observation":
        insertResourceByPattern(dmDirectory, "^dmObs.*json$", this::insertByObservation);
        break;
      case "Organization":
        insertResourceByPattern(dmDirectory, "^dmOrg.*json$", this::insertByOrganization);
        break;
      case "Patient":
        insertResourceByPattern(dmDirectory, "^dmPat.*json$", this::insertByPatient);
        break;
      case "Practitioner":
        insertResourceByPattern(dmDirectory, "^dmPra.*json$", this::insertByPractitioner);
        break;
      case "Procedure":
        insertResourceByPattern(dmDirectory, "^dmPro.*json$", this::insertByProcedure);
        break;
      case "Survey":
        insertResourceByPattern(dmDirectory, "^dmSur.*json$", this::insertBySurvey);
        break;
      default:
        throw new RuntimeException("Couldnt determine resource type for file: " + resourceToSync);
    }
    /*
     * Commit and clean up the transactions for the entity managers from
     * the various threads.
     */
    for (EntityManager entityManager : entityManagers) {
      entityManager.getTransaction().commit();
      entityManager.close();
    }
    log.info("Added {} {} entities", addedCount.get(), resourceToSync);
  }

  private <T extends DatamartEntity> void save(T entity) {
    EntityManager entityManager = getEntityManager();
    boolean exists = entityManager.find(entity.getClass(), entity.cdwId()) != null;
    updateOrAddEntity(exists, entityManager, entity);
  }

  private <T> void save(T entity, String identifier) {
    EntityManager entityManager = getEntityManager();
    boolean exists = entityManager.find(entity.getClass(), identifier) != null;
    updateOrAddEntity(exists, entityManager, entity);
  }

  private void saveDrCrosswalkEntity(String icn, String reportIdentifier) {
    save(DiagnosticReportCrossEntity.builder().icn(icn).reportId(reportIdentifier).build());
  }

  private <T> void updateOrAddEntity(boolean exists, EntityManager entityManager, T entity) {
    if (!exists) {
      entityManager.persist(entity);
    } else {
      entityManager.merge(entity);
    }
    addedCount.incrementAndGet();
    entityManager.flush();
    entityManager.clear();
  }
}
