package gov.va.api.health.dataquery.service.controller.medication;

import static gov.va.api.health.dataquery.service.controller.medication.DatamartMedicationSamples.Fhir.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.Medication.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedicationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedicationSamples.Fhir;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
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
public class DatamartMedicationControllerTest {
  private IdentityService ids = mock(IdentityService.class);
  @Autowired private MedicationRepository repository;
  @Autowired private TestEntityManager entityManager;

  @SneakyThrows
  private MedicationEntity asEntity(DatamartMedication dm) {
    return MedicationEntity.builder()
        .cdwId(dm.cdwId())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  MedicationController controller() {
    return new MedicationController(
        true,
        null,
        null,
        new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockMedicationIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("MEDICATION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  @Test
  public void read() {
    DatamartMedication dm = Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1000", dm.cdwId());
    Medication actual = controller().read("true", "1000");
    assertThat(json(actual)).isEqualTo(json(Fhir.create().medication("1000")));
  }

  @Test
  public void readRaw() {
    DatamartMedication dm = Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1", dm.cdwId());
    String json = controller().readRaw("1");
    assertThat(toObject(json)).isEqualTo(dm);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockMedicationIdentity("1", "1");
    controller().readRaw("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1");
  }

  @Test
  public void searchById() {
    DatamartMedication dm = Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1", dm.cdwId());
    Bundle actual = controller().searchById("true", "1", 1, 1);
    Medication medication = Fhir.create().medication("1000");
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(medication),
                    link(LinkRelation.first, "http://fonzy.com/cool/Medication?identifier=1", 1, 1),
                    link(LinkRelation.self, "http://fonzy.com/cool/Medication?identifier=1", 1, 1),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Medication?identifier=1",
                        1,
                        1))));
  }

  @SneakyThrows
  private DatamartMedication toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedication.class);
  }
}
