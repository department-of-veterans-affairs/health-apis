package gov.va.api.health.dataquery.patientregistration;

import java.util.concurrent.Future;
import org.springframework.scheduling.annotation.Async;

public interface PatientRegistrar {

  @Async
  Future<PatientRegistration> register(String icn);
}
