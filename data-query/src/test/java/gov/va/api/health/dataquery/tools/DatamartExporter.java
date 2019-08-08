package gov.va.api.health.dataquery.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceEntity;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportCrossEntity;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportsEntity;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientSearchEntity;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureEntity;
import java.io.FileInputStream;
import java.net.URL;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
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
          PatientEntity.class,
          PatientSearchEntity.class,
          ProcedureEntity.class
          //
          );

  EntityManager h2;
  EntityManager mitre;

  public DatamartExporter(String configFile, String outputFile) {
    mitre = new Mitre(configFile).get();
    h2 = new LocalH2(outputFile).get();
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

  @AllArgsConstructor
  private static class LocalH2 implements Supplier<EntityManager> {

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
      log.info("Exporting to {}", outputFile);
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
      properties.put("hibernate.show_sql", "true");
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
