package gov.va.api.health.dataquery.patientregistration;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncResult;

/** A patient registrar that does nothing and returns a precomputed registration. */
@Configuration
public class NoopPatientRegistrarConfig {

  @Bean
  @ConditionalOnMissingBean
  public PatientRegistrar noopPatientRegistrar() {
    return new NoopPatientRegistrar();
  }

  /** A patient registrar that does nothing. */
  public static class NoopPatientRegistrar implements PatientRegistrar {
    @Override
    public CompletableFuture<PatientRegistration> register(String icn) {
      return new AsyncResult<>(
              PatientRegistration.builder()
                  .icn(icn)
                  .application("noop")
                  .firstAccessTime(Instant.MIN)
                  .lastAccessTime(Instant.now())
                  .build())
          .completable();
    }
  }
}
