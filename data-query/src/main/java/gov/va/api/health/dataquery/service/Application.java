package gov.va.api.health.dataquery.service;

import gov.va.api.health.dataquery.service.config.DatabaseCredentialManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SuppressWarnings("WeakerAccess")
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(DatabaseCredentialManager.class)
@EnableAsync
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
