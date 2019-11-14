package gov.va.api.health.dataquery.service.controller.immunization;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.time.Instant;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartImmunizationTest {
  public void assertReadable(String json) throws java.io.IOException {
    DatamartImmunization dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartImmunization.class);
    assertThat(dm).isEqualTo(sample());
  }

  private DatamartImmunization sample() {
    return DatamartImmunization.builder()
        .cdwId("1000000030337")
        .status(DatamartImmunization.Status.completed)
        .date(Instant.parse("1997-05-09T14:21:18Z"))
        .vaccineCode(
            DatamartImmunization.VaccineCode.builder()
                .code("112")
                .text("TETANUS TOXOID, UNSPECIFIED FORMULATION")
                .build())
        .patient(
            DatamartReference.of()
                .type("Patient")
                .reference("1011549983V753765")
                .display("ZZTESTPATIENT,THOMAS THE")
                .build())
        .wasNotGiven(false)
        .performer(
            Optional.of(
                DatamartReference.of()
                    .type("Practitioner")
                    .reference("3868169")
                    .display("ZHIVAGO,YURI ANDREYEVICH")
                    .build()))
        .requester(
            Optional.of(
                DatamartReference.of()
                    .type("Practitioner")
                    .reference("1702436")
                    .display("SHINE,DOC RAINER")
                    .build()))
        .encounter(
            Optional.of(
                DatamartReference.of()
                    .type("Encounter")
                    .reference("1000589847194")
                    .display("1000589847194")
                    .build()))
        .location(
            Optional.of(
                DatamartReference.of()
                    .type("Location")
                    .reference("358359")
                    .display("ZZGOLD PRIMARY CARE")
                    .build()))
        .note(Optional.of("PATIENT CALM AFTER VACCINATION"))
        .reaction(
            Optional.of(
                DatamartReference.of()
                    .type("Observation")
                    .reference(null)
                    .display("Other")
                    .build()))
        .vaccinationProtocols(
            Optional.of(
                DatamartImmunization.VaccinationProtocols.builder()
                    .series("Booster")
                    .seriesDoses(1)
                    .build()))
        .build();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadable("datamart-practitioner.json");
  }

  @Test
  @SneakyThrows
  public void unmarshalSampleV0() {
    assertReadable("datamart-immunization-v0.json");
  }
}
