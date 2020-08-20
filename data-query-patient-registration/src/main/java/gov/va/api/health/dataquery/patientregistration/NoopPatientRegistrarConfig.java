package gov.va.api.health.dataquery.patientregistration;

import java.time.Instant;
import java.util.concurrent.Future;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncResult;

@Configuration
public class NoopPatientRegistrarConfig {

  @Bean
  @ConditionalOnMissingBean
  public PatientRegistrar noopPatientRegistrar() {
    return new NoopPatientRegistrar();
  }

  public static class NoopPatientRegistrar implements PatientRegistrar {
    @Override
    public Future<PatientRegistration> register(String icn) {
      return new AsyncResult<>(
          PatientRegistration.builder()
              .icn(icn)
              .application("noop")
              .firstAccessTime(Instant.MIN)
              .lastAccessTime(Instant.now())
              .build());
    }
  }
}
