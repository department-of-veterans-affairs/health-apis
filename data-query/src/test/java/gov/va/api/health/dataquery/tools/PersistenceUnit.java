package gov.va.api.health.dataquery.tools;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hibernate.jpa.HibernatePersistenceProvider;

@Value
@Accessors(fluent = false)
@Builder
public class PersistenceUnit implements PersistenceUnitInfo {
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
