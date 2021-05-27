package gov.va.api.health.dataquery.patientregistration;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;

/** Registers usages of a consumer for later metrics collection. */
public interface PatientRegistrar {

  @Async
  CompletableFuture<PatientRegistration> register(String icn);
}
