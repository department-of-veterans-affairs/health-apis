package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import lombok.SneakyThrows;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartAllergyIntoleranceControllerTest {
  @Autowired private TestEntityManager entityManager;

  @SneakyThrows
  private static DatamartAllergyIntolerance toObject(String payload) {
    return JacksonConfig.createMapper().readValue(payload, DatamartAllergyIntolerance.class);
  }

  @SneakyThrows
  private static AllergyIntoleranceEntity asEntity(DatamartAllergyIntolerance dm) {
    return AllergyIntoleranceEntity.builder()
        .id(dm.cdwId())
        .icn(dm.patient().get().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @Test
  public void readRaw() {
    String publicId = "865e1010-99b6-4b8d-a5c9-4ad259db0857";
    IdentityService ids = mock(IdentityService.class);

    AllergyIntoleranceController controller =
        new AllergyIntoleranceController(
            true,
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
            entityManager.getEntityManager(),
            WitnessProtection.builder().identityService(ids).build());

    DatamartAllergyIntolerance dm =
        DatamartAllergyIntoleranceSamples.Datamart.create().allergyIntolerance();
    entityManager.persistAndFlush(asEntity(dm));

    when(ids.lookup(publicId))
        .thenReturn(
            asList(
                ResourceIdentity.builder()
                    .system("CDW")
                    .resource("CONDITION")
                    .identifier(dm.cdwId())
                    .build()));

    assertThat(toObject(controller.readRaw(publicId))).isEqualTo(dm);
  }

  // identifier id patient
  @Test
  public void read() {
    String publicId = "865e1010-99b6-4b8d-a5c9-4ad259db0857";
    IdentityService ids = mock(IdentityService.class);

    AllergyIntoleranceController controller =
        new AllergyIntoleranceController(
            true,
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
            entityManager.getEntityManager(),
            WitnessProtection.builder().identityService(ids).build());

    DatamartAllergyIntolerance dm =
        DatamartAllergyIntoleranceSamples.Datamart.create().allergyIntolerance();
    entityManager.persistAndFlush(asEntity(dm));

    when(ids.lookup(publicId))
        .thenReturn(
            asList(
                ResourceIdentity.builder()
                    .system("CDW")
                    .resource("CONDITION")
                    .identifier(dm.cdwId())
                    .build()));
    //    when(ids.register(Mockito.any()))
    //    .thenReturn(
    //        List.of(
    //
    // Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));

    assertThat(controller.read("true", publicId))
        .isEqualTo(DatamartAllergyIntoleranceSamples.Fhir.create().allergyIntolerance());
  }
}
