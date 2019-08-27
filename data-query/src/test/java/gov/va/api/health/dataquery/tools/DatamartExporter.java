package gov.va.api.health.dataquery.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceEntity;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportCrossEntity;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportsEntity;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationEntity;
import gov.va.api.health.dataquery.service.controller.medication.MedicationEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementEntity;
import gov.va.api.health.dataquery.service.controller.observation.ObservationEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientSearchEntity;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureEntity;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;

/**
 * This application will copy data out of the Mitre database into a local H2 database. It expects
 * that you will have ./config/lab.properties with Mitre database credentials using standard Spring
 * properties.
 *
 * <p>This test will re-write src/test/resources/mitre, the local H2 mitre database used by
 * integration tests.
 */
@Slf4j
public class DatamartExporter {

  /** Add classes to this list to copy them from Mitre to H2 */
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

  EntityManager h2;
  EntityManager mitre;

  public DatamartExporter(String configFile, String outputFile) {
    mitre = new Mitre(configFile).get();
    h2 = new LocalH2(outputFile, MANAGED_CLASSES).get();
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      log.error("DatamartExporter <application.properties> <h2-database>");
      throw new RuntimeException("Missing arguments");
    }
    String configFile = args[0];
    String outputFile = args[1];
    new DatamartExporter(configFile, outputFile).export();
    log.info("All done");
    System.exit(0);
  }

  public void export() {
    h2.getTransaction().begin();
    MANAGED_CLASSES.stream().forEach(this::steal);
    h2.getTransaction().commit();
    mitre.close();
    h2.close();
  }

  private <T> void steal(Class<T> type) {
    log.info("Stealing {}", type);
    mitre
        .createQuery("select e from " + type.getSimpleName() + " e", type)
        .getResultStream()
        .forEach(
            e -> {
              mitre.detach(e);
              log.info("{}", e);
              h2.persist(e);
            });
  }

  private static class Mitre implements Supplier<EntityManager> {

    private final Properties config;

    @SneakyThrows
    Mitre(String configFile) {
      log.info("Loading Mitre connection configuration from {}", configFile);
      config = new Properties(System.getProperties());
      try (FileInputStream inputStream = new FileInputStream(configFile)) {
        config.load(inputStream);
      }
    }

    @Override
    public EntityManager get() {

      PersistenceUnitInfo info =
          PersistenceUnit.builder()
              .persistenceUnitName("mitre")
              .jtaDataSource(mitreDataSource())
              .managedClasses(MANAGED_CLASSES)
              .properties(mitreProperties())
              .build();
      return new HibernatePersistenceProvider()
          .createContainerEntityManagerFactory(
              info,
              ImmutableMap.of(
                  AvailableSettings.JPA_JDBC_DRIVER,
                  "com.microsoft.sqlserver.jdbc.SQLServerDriver"))
          .createEntityManager();
    }

    DataSource mitreDataSource() {
      SQLServerDataSource ds = new SQLServerDataSource();
      ds.setUser(valueOf("spring.datasource.username"));
      ds.setPassword(valueOf("spring.datasource.password"));
      ds.setURL(valueOf("spring.datasource.url"));
      return ds;
    }

    Properties mitreProperties() {
      Properties properties = new Properties();
      properties.put("hibernate.hbm2ddl.auto", "none");
      properties.put("hibernate.show_sql", "false"); // <---- CHANGE TO TRUE TO DEBUG
      properties.put("hibernate.format_sql", "true");
      properties.put("hibernate.globally_quoted_identifiers", "true");
      return properties;
    }

    private String valueOf(String name) {
      String value = config.getProperty(name, "");
      assertThat(value).withFailMessage("System property %s must be specified.", name).isNotBlank();
      return value;
    }
  }
}
