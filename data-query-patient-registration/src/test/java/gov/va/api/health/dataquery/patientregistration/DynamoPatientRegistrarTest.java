package gov.va.api.health.dataquery.patientregistration;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DynamoPatientRegistrarTest {

  private static LocalDynamoDb db;

  @BeforeAll
  static void _dbStart() {
    db = LocalDynamoDb.builder().port(8000).tableName("patient-registration-local").build().start();
  }

  @AfterAll
  static void _dbStop() {
    db.stop();
  }

  @Test
  @SneakyThrows
  void deleteMeAndDoSomethingMeaningful() {
    assertThat(new DynamoPatientRegistrar().register("whatever")).isNotNull();
  }
}
