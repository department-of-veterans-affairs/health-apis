package gov.va.api.health.dataquery.patientregistration;

import java.util.concurrent.Future;

public class NoopPatientRegistrar implements PatientRegistrar {

  @Override
  public Future<PatientRegistration> register(String icn) {
    return null;
  }
}
