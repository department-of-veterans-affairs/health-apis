package gov.va.api.health.dataquery.patientregistration;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class DynamoPatientRegistrarTest {

  private static LocalDynamoDb db;

  @Test
  @SneakyThrows
  void deleteMeAndDoSomethingMeaningful() {
    try (var db = LocalDynamoDb.startDefault()) {
      assertThat(
              new DynamoPatientRegistrar(
                      DynamoPatientRegistrarOptions.builder()
                          .enabled(true)
                          .endpoint("http://localhost:" + db.port())
                          .region(db.signingRegion())
                          .table(db.tableName())
                          .applicationName("fugazi")
                          .build())
                  .register("whatever"))
          .isNotNull();
    }
  }
}
