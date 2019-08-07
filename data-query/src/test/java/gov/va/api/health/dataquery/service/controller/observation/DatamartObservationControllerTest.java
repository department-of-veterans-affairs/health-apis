package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.ResourceIdentity;
import lombok.SneakyThrows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartObservationControllerTest {
  @Autowired private ObservationRepository repository;

  private static void setUpIds(IdentityService ids, DatamartObservation dm) {
    when(ids.lookup(DatamartObservationSamples.Fhir.ID))
        .thenReturn(
            asList(
                ResourceIdentity.builder()
                    .system("CDW")
                    .resource("OBSERVATION")
                    .identifier(dm.cdwId())
                    .build()));
  }

  @SneakyThrows
  private static ObservationEntity asEntity(DatamartObservation dm) {
    return ObservationEntity.builder()
        .id(dm.cdwId())
        .icn(dm.subject().get().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @SneakyThrows
  private static DatamartObservation toObject(String payload) {
    return JacksonConfig.createMapper().readValue(payload, DatamartObservation.class);
  }

  @Test
  public void readRaw() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            false,
            null,
            null,
            null,
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    // entityManager.persistAndFlush(asEntity(dm));
    setUpIds(ids, dm);
    assertThat(toObject(controller.readRaw(DatamartObservationSamples.Fhir.ID))).isEqualTo(dm);
  }
}
