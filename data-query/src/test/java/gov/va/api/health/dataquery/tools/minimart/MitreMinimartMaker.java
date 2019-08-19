package gov.va.api.health.dataquery.tools.minimart;

import com.google.common.collect.ImmutableMap;
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
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import gov.va.api.health.dataquery.tools.DatamartExporter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;

public class MitreMinimartMaker {

  private EntityManager entityManager;

  /** Uses the resource name to determine what the prefix of the file should be. */
  public String determineFilePrefix(String resourceFullName) {
    String abbreviation = "";
    switch (resourceFullName) {
      case "AllergyIntolerance":
        abbreviation = "AllInt";
        break;
      case "Condition":
        abbreviation = "Con";
        break;
      case "DiagnosticReport":
        abbreviation = "DiaRep";
        break;
      case "Immunization":
        abbreviation = "Imm";
        break;
      case "MedicationStatement":
        abbreviation = "MedSta";
        break;
      case "MedicationOrder":
        abbreviation = "MedOrd";
        break;
      case "Medication":
        abbreviation = "Med";
        break;
      case "Observation":
        abbreviation = "Obs";
        break;
      case "Patient":
        abbreviation = "Pat";
        break;
      case "Procedure":
        abbreviation = "Pro";
        break;
      default:
        abbreviation = "--FAILURE--";
    }
    return "dm" + abbreviation;
  }

  /** Uses the entity manager to insert each record into the database without committing. */
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
        break;
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
        break;
      case "DiagnosticReport":
        DatamartDiagnosticReports dr =
            JacksonConfig.createMapper().readValue(file, DatamartDiagnosticReports.class);
        DiagnosticReportsEntity drEntity =
            DiagnosticReportsEntity.builder()
                .icn(dr.fullIcn())
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(drEntity);
        break;
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
        break;
      case "Medication":
        DatamartMedication med =
            JacksonConfig.createMapper().readValue(file, DatamartMedication.class);
        MedicationEntity medEntity =
            MedicationEntity.builder()
                .cdwId(med.cdwId())
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(medEntity);
        break;
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
        break;
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
        break;
      case "Observation":
        DatamartObservation obs =
            JacksonConfig.createMapper().readValue(file, DatamartObservation.class);
        ObservationEntity obsEntity =
            ObservationEntity.builder()
                .cdwId(obs.cdwId())
                .icn(obs.subject().isPresent() ? patientIcn(obs.subject().get()) : null)
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(obsEntity);
        break;
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
        break;
      case "Procedure":
        DatamartProcedure proc =
            JacksonConfig.createMapper().readValue(file, DatamartProcedure.class);
        ProcedureEntity procEntity =
            ProcedureEntity.builder()
                .cdwId(proc.cdwId())
                .icn(patientIcn(proc.patient()))
                .payload(Files.readAllBytes(Paths.get(file.getPath())).toString())
                .build();
        entityManager.persist(procEntity);
        break;
      default:
        throw new RuntimeException("Couldnt determine resource type for file: " + resourceName);
    }
  }

  /** Main. */
  public void main(String[] args) {
    if (args.length != 3) {
      throw new RuntimeException("Arg Count Incorrect: " + args.length);
    }
    String resourceToSync = args[0];
    String directory = args[1];
    entityManager = new LocalH2(args[3]).get();
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

  public String patientIcn(DatamartReference dm) {
    return dm != null && dm.reference().isPresent() ? dm.reference().get() : null;
  }

  @AllArgsConstructor
  private static class LocalH2 implements Supplier<EntityManager> {

    private static final List<Class<?>> MANAGED_CLASSES =
        Arrays.asList(
            AllergyIntoleranceEntity.class,
            ConditionEntity.class,
            DiagnosticReportCrossEntity.class,
            DiagnosticReportsEntity.class,
            ImmunizationEntity.class,
            MedicationOrderEntity.class,
            MedicationEntity.class,
            MedicationStatementEntity.class,
            ObservationEntity.class,
            PatientEntity.class,
            PatientSearchEntity.class,
            ProcedureEntity.class
            //
            );

    private final String outputFile;

    @Override
    @SneakyThrows
    public EntityManager get() {
      PersistenceUnitInfo info =
          PersistenceUnit.builder()
              .persistenceUnitName("h2")
              .jtaDataSource(h2DataSource())
              .managedClasses(MANAGED_CLASSES)
              .properties(h2Properties())
              .build();
      info.getJtaDataSource()
          .getConnection()
          .createStatement()
          .execute("DROP SCHEMA IF EXISTS APP CASCADE; CREATE SCHEMA APP;");
      return new HibernatePersistenceProvider()
          .createContainerEntityManagerFactory(
              info, ImmutableMap.of(AvailableSettings.JPA_JDBC_DRIVER, "org.h2.Driver"))
          .createEntityManager();
    }

    DataSource h2DataSource() {
      JdbcDataSource h2 = new JdbcDataSource();
      h2.setURL("jdbc:h2:" + outputFile);
      h2.setUser("sa");
      h2.setPassword("sa");
      return h2;
    }

    Properties h2Properties() {
      Properties properties = new Properties();
      properties.put("hibernate.hbm2ddl.auto", "create-drop");
      properties.put("hibernate.connection.autocommit", "true");
      properties.put("hibernate.show_sql", "false");
      return properties;
    }
  }

  @Value
  @Accessors(fluent = false)
  @Builder
  private static class PersistenceUnit implements PersistenceUnitInfo {
    String persistenceUnitName;

    @Builder.Default
    String persistenceProviderClassName = HibernatePersistenceProvider.class.getName();

    @Builder.Default
    PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;

    DataSource jtaDataSource;
    @Builder.Default List<String> mappingFileNames = Collections.emptyList();
    @Builder.Default List<URL> jarFileUrls = Collections.emptyList();
    URL persistenceUnitRootUrl;
    @Singular List<Class<?>> managedClasses;
    @Builder.Default boolean excludeUnlistedClasses = false;
    @Builder.Default SharedCacheMode sharedCacheMode = SharedCacheMode.NONE;
    @Builder.Default ValidationMode validationMode = ValidationMode.AUTO;
    @Builder.Default Properties properties = new Properties();
    @Builder.Default String persistenceXMLSchemaVersion = "2.1";
    @Builder.Default ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Override
    public void addTransformer(ClassTransformer transformer) {}

    @Override
    public boolean excludeUnlistedClasses() {
      return excludeUnlistedClasses;
    }

    @Override
    public List<String> getManagedClassNames() {
      return managedClasses.stream().map(Class::getName).collect(Collectors.toList());
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
      return null;
    }

    @Override
    public DataSource getNonJtaDataSource() {
      return getJtaDataSource();
    }
  }
}
