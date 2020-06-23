package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure.Status;
import java.time.Instant;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatamartProcedureTest {
  public void assertReadable(DatamartProcedure sample, String json) throws java.io.IOException {
    DatamartProcedure dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartProcedure.class);
    assertThat(dm).isEqualTo(sample);
  }

  private DatamartProcedure notPerformedSample() {
    return DatamartProcedure.builder()
        .cdwId("1000000719261")
        .patient(
            DatamartReference.of()
                .type("Patient")
                .reference("1004476237V111282")
                .display("VETERAN,GRAY PRO")
                .build())
        .status(Status.completed)
        .coding(
            DatamartCoding.of()
                .system("http://www.ama-assn.org/go/cpt")
                .code("90870")
                .display("ELECTROCONVULSIVE THERAPY")
                .build())
        .notPerformed(true)
        .build();
  }

  private DatamartProcedure sample() {
    return DatamartProcedure.builder()
        .cdwId("1000000719261")
        .patient(
            DatamartReference.of()
                .type("Patient")
                .reference("1004476237V111282")
                .display("VETERAN,GRAY PRO")
                .build())
        .status(Status.completed)
        .coding(
            DatamartCoding.of()
                .system("http://www.ama-assn.org/go/cpt")
                .code("90870")
                .display("ELECTROCONVULSIVE THERAPY")
                .build())
        .notPerformed(false)
        .reasonNotPerformed(Optional.of("CASE MOVED TO EARLIER DATE"))
        .performedDateTime(Optional.of(Instant.parse("2008-01-02T06:00:00Z")))
        .location(
            Optional.of(
                DatamartReference.of()
                    .type("Location")
                    .reference("237281")
                    .display("ZZPSYCHIATRY")
                    .build()))
        .build();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadable(sample(), "datamart-procedure.json");
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleNotPerformed() {
    assertReadable(notPerformedSample(), "datamart-procedure-not-performed.json");
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV0() {
    assertReadable(sample(), "datamart-procedure-v0.json");
  }
}
