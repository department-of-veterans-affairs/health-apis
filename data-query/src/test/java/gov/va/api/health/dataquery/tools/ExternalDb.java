package gov.va.api.health.dataquery.tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.io.FileInputStream;
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

@Slf4j
public class ExternalDb implements Supplier<EntityManager> {

  private final Properties config;

  private final List<Class<?>> managedClasses;

  @SneakyThrows
  public ExternalDb(String configFile, List<Class<?>> managedClasses) {
    log.info("Loading Mitre connection configuration from {}", configFile);
    config = new Properties(System.getProperties());
    try (FileInputStream inputStream = new FileInputStream(configFile)) {
      config.load(inputStream);
    }
    this.managedClasses = managedClasses;
  }

  @Override
  public EntityManager get() {

    PersistenceUnitInfo info =
        PersistenceUnit.builder()
            .persistenceUnitName("mitre")
            .jtaDataSource(sqlServerDataSource())
            .managedClasses(managedClasses)
            .properties(sqlServerProperties())
            .build();
    return new HibernatePersistenceProvider()
        .createContainerEntityManagerFactory(
            info,
            ImmutableMap.of(
                AvailableSettings.JPA_JDBC_DRIVER, valueOf("spring.datasource.driver-class-name")))
        .createEntityManager();
  }

  DataSource sqlServerDataSource() {
    SQLServerDataSource ds = new SQLServerDataSource();
    ds.setUser(valueOf("spring.datasource.username"));
    ds.setPassword(valueOf("spring.datasource.password"));
    ds.setURL(valueOf("spring.datasource.url"));
    return ds;
  }

  Properties sqlServerProperties() {
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
