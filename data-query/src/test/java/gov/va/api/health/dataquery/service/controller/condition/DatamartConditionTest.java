package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartConditionTest {

  public void assertReadable(String json) throws java.io.IOException {
    DatamartCondition dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartCondition.class);
    assertThat(dm).isEqualTo(sample());
  }

  private DatamartCondition sample() {
    return DatamartCondition.builder()
        .cdwId("800274570575:D")
        .patient(
            DatamartReference.of()
                .type("Patient")
                .reference("666V666")
                .display("VETERAN,FIRNM MINAM")
                .build())
        .asserter(
            Optional.of(
                DatamartReference.of()
                    .type("Practitioner")
                    .reference("1294265")
                    .display("DOCLANAM,DOCFIRNAM E")
                    .build()))
        .dateRecorded(Optional.of(LocalDate.parse("2011-06-27")))
        .snomed(
            Optional.of(
                DatamartCondition.SnomedCode.builder()
                    .code("70650003")
                    .display("Urinary bladder stone")
                    .build()))
        .icd(
            Optional.of(
                DatamartCondition.IcdCode.builder()
                    .code("N210")
                    .display("Calculus in bladder")
                    .version("10")
                    .build()))
        .category(DatamartCondition.Category.diagnosis)
        .clinicalStatus(DatamartCondition.ClinicalStatus.active)
        .onsetDateTime(Optional.of(Instant.parse("2011-06-27T05:40:00Z")))
        .abatementDateTime(Optional.empty())
        .build();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadable("datamart-condition.json");
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV0() {
    assertReadable("datamart-condition-v0.json");
  }
}
