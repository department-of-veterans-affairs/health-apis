package gov.va.api.health.dataquery.patientregistration;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;

public interface PatientRegistrar {

  @Async
  CompletableFuture<PatientRegistration> register(String icn);
}
