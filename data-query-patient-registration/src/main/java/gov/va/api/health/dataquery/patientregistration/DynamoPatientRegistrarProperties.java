package gov.va.api.health.dataquery.patientregistration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration options for the Dynamo DB used for patient registration. */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("dynamo-patient-registrar")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynamoPatientRegistrarProperties {
  /**
   * The AWS endpoint, e.g. "http://localhost:8000" for local development or
   * https://dynamodb.us-gov-west-1.amazonaws.com:443 for real AWS.
   */
  private String endpoint;

  /** The AWS region, e.g. us-gov-west-1. */
  private String region;

  /** The table name, e.g. patient-registration-production. */
  private String table;

  /** The name of the application to use when registering, e.g. data-query */
  private String applicationName;

  /** If true, registration will be applied. */
  private boolean enabled;
}
