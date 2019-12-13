package gov.va.api.health.dataquery.tools;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;

@Slf4j
@AllArgsConstructor
public class LocalH2 implements Supplier<EntityManagerFactory> {

  private final String outputFile;

  private final List<Class<?>> managedClasses;

  @Override
  @SneakyThrows
  public EntityManagerFactory get() {
    PersistenceUnitInfo info =
        PersistenceUnit.builder()
            .persistenceUnitName("h2")
            .jtaDataSource(h2DataSource())
            .managedClasses(managedClasses)
            .properties(h2Properties())
            .build();
    info.getJtaDataSource()
        .getConnection()
        .createStatement()
        .execute("DROP SCHEMA IF EXISTS APP CASCADE; CREATE SCHEMA APP;");
    return new HibernatePersistenceProvider()
        .createContainerEntityManagerFactory(
            info, ImmutableMap.of(AvailableSettings.JPA_JDBC_DRIVER, "org.h2.Driver"));
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
