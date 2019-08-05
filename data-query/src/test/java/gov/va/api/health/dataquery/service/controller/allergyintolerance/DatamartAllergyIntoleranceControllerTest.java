package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartAllergyIntoleranceControllerTest {
  @Autowired private TestEntityManager entityManager;

  @SneakyThrows
  private static AllergyIntoleranceEntity asEntity(DatamartAllergyIntolerance dm) {
    return AllergyIntoleranceEntity.builder()
        .id(dm.cdwId())
        .icn(dm.patient().get().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  private static void setUpIds(IdentityService ids, DatamartAllergyIntolerance dm) {
    when(ids.lookup(DatamartAllergyIntoleranceSamples.Fhir.ID))
        .thenReturn(
            asList(
                ResourceIdentity.builder()
                    .system("CDW")
                    .resource("ALLERGY_INTOLERANCE")
                    .identifier(dm.cdwId())
                    .build()));
    when(ids.register(Mockito.any()))
        .thenReturn(
            asList(
                Registration.builder()
                    .uuid(DatamartAllergyIntoleranceSamples.Fhir.ID)
                    .resourceIdentity(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("ALLERGY_INTOLERANCE")
                            .identifier(dm.cdwId())
                            .build())
                    .build(),
                Registration.builder()
                    .uuid(DatamartAllergyIntoleranceSamples.Fhir.RECORDER_ID)
                    .resourceIdentity(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("PRACTITIONER")
                            .identifier(dm.recorder().get().reference().get())
                            .build())
                    .build(),
                Registration.builder()
                    .uuid(DatamartAllergyIntoleranceSamples.Fhir.PATIENT_ID)
                    .resourceIdentity(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("PATIENT")
                            .identifier(dm.patient().get().reference().get())
                            .build())
                    .build(),
                Registration.builder()
                    .uuid(DatamartAllergyIntoleranceSamples.Fhir.NOTE_AUTHOR_ID)
                    .resourceIdentity(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("PRACTITIONER")
                            .identifier(dm.notes().get(0).practitioner().get().reference().get())
                            .build())
                    .build()));
  }

  @SneakyThrows
  private static DatamartAllergyIntolerance toObject(String payload) {
    return JacksonConfig.createMapper().readValue(payload, DatamartAllergyIntolerance.class);
  }

  @Test
  public void id() {
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
    setUpIds(ids, dm);
    AllergyIntolerance.Bundle result =
        controller.searchById("true", DatamartAllergyIntoleranceSamples.Fhir.ID, 1, 15);
    assertThat(Iterables.getOnlyElement(result.entry()).resource())
        .isEqualTo(DatamartAllergyIntoleranceSamples.Fhir.create().allergyIntolerance());
  }

  @Test
  public void identifier() {
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
    setUpIds(ids, dm);
    AllergyIntolerance.Bundle result =
        controller.searchByIdentifier("true", DatamartAllergyIntoleranceSamples.Fhir.ID, 1, 15);
    assertThat(Iterables.getOnlyElement(result.entry()).resource())
        .isEqualTo(DatamartAllergyIntoleranceSamples.Fhir.create().allergyIntolerance());
  }

  @Test
  public void patient() {
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
    setUpIds(ids, dm);
    AllergyIntolerance.Bundle result =
        controller.searchByPatient("true", dm.patient().get().reference().get(), 1, 15);
    assertThat(Iterables.getOnlyElement(result.entry()).resource())
        .isEqualTo(DatamartAllergyIntoleranceSamples.Fhir.create().allergyIntolerance());
  }

  @Test
  public void read() {
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
    setUpIds(ids, dm);
    AllergyIntolerance result = controller.read("true", DatamartAllergyIntoleranceSamples.Fhir.ID);
    assertThat(result)
        .isEqualTo(DatamartAllergyIntoleranceSamples.Fhir.create().allergyIntolerance());
  }

  @Test
  public void readRaw() {
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
    setUpIds(ids, dm);
    assertThat(toObject(controller.readRaw(DatamartAllergyIntoleranceSamples.Fhir.ID)))
        .isEqualTo(dm);
  }
}
