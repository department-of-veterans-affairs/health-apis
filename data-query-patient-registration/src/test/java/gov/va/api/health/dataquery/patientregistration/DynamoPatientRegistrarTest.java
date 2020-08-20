package gov.va.api.health.dataquery.patientregistration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DynamoPatientRegistrarTest {

  @Test
  void deleteMeAndDoSomethingMeaningful() {
    assertThat(new DynamoPatientRegistrar().register("whatever")).isNotNull();
  }
}
