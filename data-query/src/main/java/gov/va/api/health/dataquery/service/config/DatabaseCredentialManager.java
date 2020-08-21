package gov.va.api.health.dataquery.service.config;

import static gov.va.api.health.dataquery.service.controller.Transformers.allPresent;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException;
import com.amazonaws.services.simplesystemsmanagement.model.InvalidKeyIdException;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterVersionNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Configuration
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseCredentialManager {
  private String awsParameterName;

  String buildConnectionUrl(ConnectionInfo connectionInfo) {
    StringBuilder dbUrl = new StringBuilder();
    dbUrl.append("jdbc:sqlserver://");
    dbUrl.append(connectionInfo.host().get());
    dbUrl.append(":" + connectionInfo.port().get());
    dbUrl.append(";");
    dbUrl.append("DatabaseName=" + connectionInfo.db().get());
    if (!connectionInfo.configurations().isEmpty()) {
      dbUrl.append(";");
      dbUrl.append(String.join(";", connectionInfo.configurations()));
    }
    return dbUrl.toString();
  }

  private void configureDataSource(DataSourceBuilder<?> dataSourceBuilder, String parameterValue) {
    try {
      ConnectionInfo connectionInfo =
          JacksonConfig.createMapper().readValue(parameterValue, ConnectionInfo.class);
      log.info("Configuring database using AWS Parameter: {}", getAwsParameterName());
      if (allPresent(connectionInfo.db(), connectionInfo.host(), connectionInfo.port())) {
        dataSourceBuilder.url(buildConnectionUrl(connectionInfo));
      }
      connectionInfo.user().ifPresent(dataSourceBuilder::username);
      connectionInfo.password().ifPresent(dataSourceBuilder::password);
    } catch (JsonProcessingException e) {
      log.info(
          "Value for AWS Parameter ({}) is not readable, defaulting to application.properties.",
          getAwsParameterName());
    }
  }

  public String getAwsParameterName() {
    // To avoid setting this in every spring test, we'll make a default
    return awsParameterName == null ? "unset" : awsParameterName;
  }

  public void setAwsParameterName(String awsParameterName) {
    this.awsParameterName = awsParameterName;
  }

  /**
   * Conditionally overrides the database connection information provided in application.properties
   * when an aws parameter value is provided and valid.
   */
  @Bean
  public DataSource getDataSource() {
    AWSSimpleSystemsManagement ssm = AWSSimpleSystemsManagementClientBuilder.defaultClient();
    DataSourceBuilder<?> dataSourceBuilder =
        getDataSourceProperties().initializeDataSourceBuilder();
    getParameterValue(ssm, getAwsParameterName())
        .ifPresent(param -> configureDataSource(dataSourceBuilder, param));
    return dataSourceBuilder.build();
  }

  @Bean
  @Primary
  public DataSourceProperties getDataSourceProperties() {
    return new DataSourceProperties();
  }

  Optional<String> getParameterValue(AWSSimpleSystemsManagement ssm, String parameterName) {
    GetParameterRequest req = new GetParameterRequest();
    req.setName(parameterName);
    req.setWithDecryption(true);
    Parameter awsParameter;
    try {
      awsParameter = ssm.getParameter(req).getParameter();
    } catch (InternalServerErrorException
        | InvalidKeyIdException
        | ParameterNotFoundException
        | ParameterVersionNotFoundException e) {
      log.info(
          "Couldn't find credential {} ({}), defaulting to application.properties.",
          parameterName,
          e.getClass().getSimpleName());
      return Optional.empty();
    }
    return Optional.ofNullable(awsParameter.getValue());
  }

  @Value
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static class ConnectionInfo {
    Optional<String> host;

    Optional<Integer> port;

    Optional<String> db;

    Optional<String> user;

    Optional<String> password;

    List<String> configurations;

    /** Lazy Getter. */
    public List<String> configurations() {
      return configurations == null ? new ArrayList<>() : configurations;
    }

    /** Lazy Getter. */
    public Optional<String> db() {
      return db == null ? Optional.empty() : db;
    }

    /** Lazy Getter. */
    public Optional<String> host() {
      return host == null ? Optional.empty() : host;
    }

    /** Lazy Getter. */
    public Optional<String> password() {
      return password == null ? Optional.empty() : password;
    }

    /** Lazy Getter. */
    public Optional<Integer> port() {
      return port == null ? Optional.empty() : port;
    }

    /** Lazy Getter. */
    public Optional<String> user() {
      return user == null ? Optional.empty() : user;
    }
  }
}
