package gov.va.api.health.dataquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.condition.DatamartConditionSamples.Datamart;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartConditionControllerTest {

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private ConditionRepository repository;
  @Autowired private TestEntityManager entityManager;

  @SneakyThrows
  private ConditionEntity asEntity(DatamartCondition dm) {
    return ConditionEntity.builder()
        .cdwId(dm.cdwId())
        .category(dm.category().toString())
        .clinicalStatus(dm.clinicalStatus().toString())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  ConditionController controller() {
    return new ConditionController(
        true,
        null,
        null,
        new Bundler(new ConfigurableBaseUrlPageLinks("", "")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  public void mockConditionIdentity(String publicId, String cdwId) {
    when(ids.lookup(publicId))
        .thenReturn(
            List.of(
                ResourceIdentity.builder()
                    .system("CDW")
                    .resource("CONDITION")
                    .identifier(cdwId)
                    .build()));
  }

  @Test
  public void read() {
    DatamartCondition dm = Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("x", dm.cdwId());
    Condition actual = controller().read("true", "x");
    assertThat(actual).isEqualTo(DatamartConditionSamples.Fhir.create().condition());
  }

  @Test
  public void readRaw() {
    DatamartCondition dm = Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("x", dm.cdwId());
    String json = controller().readRaw("x");
    assertThat(toObject(json)).isEqualTo(dm);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockConditionIdentity("x", "x");
    controller().readRaw("x");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("x");
  }

  @SneakyThrows
  private DatamartCondition toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartCondition.class);
  }
}
