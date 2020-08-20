package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.patientregistration.PatientRegistrationFilter;
import org.junit.jupiter.api.Test;

public class PatientRegistrationIT {

  private String patientReadUrlDstu2() {
    return TestClients.dstu2DataQuery().service().apiPath()
        + "Patient/"
        + SystemDefinitions.systemDefinition().cdwIds().patient();
  }

  private String patientReadUrlR4() {
    return TestClients.r4DataQuery().service().apiPath()
        + "Patient/"
        + SystemDefinitions.systemDefinition().cdwIds().patient();
  }

  @Test
  void patientRegistrationIsAppliedToPatientUrls() {
    /*
     * If the filter is applied, a header will be set regardless of whether registration is acutally
     * applied. This test isn't verifying whether we registered or not, just whether the filter is
     * correctly configured.
     */
    assertThat(
            TestClients.dstu2DataQuery()
                .get(patientReadUrlDstu2())
                .response()
                .getHeader(PatientRegistrationFilter.REGISTRATION_HEADER))
        .isNotNull();
    assertThat(
            TestClients.r4DataQuery()
                .get(patientReadUrlR4())
                .response()
                .getHeader(PatientRegistrationFilter.REGISTRATION_HEADER))
        .isNotNull();
  }
}
