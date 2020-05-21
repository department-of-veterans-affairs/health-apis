package gov.va.api.health.dataquery.service.controller.medication;

import static gov.va.api.health.dataquery.service.controller.medication.MedicationSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.uscorer4.api.resources.Medication;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class R4MedicationControllerTest {
  private static final String PUBLIC_TEST_ID = "1000";

  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private MedicationRepository repository;

  @SneakyThrows
  private MedicationEntity asEntity(DatamartMedication dm) {
    return MedicationEntity.builder()
        .cdwId(dm.cdwId())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  R4MedicationController controller() {
    return new R4MedicationController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://abed.com", "cool", "cool", "cool")),
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
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  @Test
  public void read() {
    DatamartMedication dm = MedicationSamples.Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity(PUBLIC_TEST_ID, dm.cdwId());
    Medication actual = controller().read(PUBLIC_TEST_ID);
    assertThat(json(actual))
        .isEqualTo(json(MedicationSamples.R4.create().medication(PUBLIC_TEST_ID)));
  }

  @Test
  public void readRaw() {
    DatamartMedication dm = MedicationSamples.Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1", dm.cdwId());
    String json = controller().readRaw("1", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockMedicationIdentity("1", "1");
    controller().readRaw("1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockMedicationIdentity("1", "1");
    controller().read("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    controller().read("1");
  }

  @Test
  public void searchById() {
    DatamartMedication dm = MedicationSamples.Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1", dm.cdwId());
    Medication.Bundle actual = controller().searchById("1", 1, 1);
    Medication medication = MedicationSamples.R4.create().medication("1");
    assertThat(json(actual))
        .isEqualTo(
            json(
                MedicationSamples.R4.asBundle(
                    "http://abed.com/cool",
                    List.of(medication),
                    1,
                    link(LinkRelation.first, "http://abed.com/cool/Medication?identifier=1", 1, 1),
                    link(LinkRelation.self, "http://abed.com/cool/Medication?identifier=1", 1, 1),
                    link(
                        LinkRelation.last, "http://abed.com/cool/Medication?identifier=1", 1, 1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartMedication dm = MedicationSamples.Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1", dm.cdwId());
    Medication.Bundle actual = controller().searchByIdentifier("1", 1, 1);
    validationSearchByIdResult(actual);
  }

  @SneakyThrows
  private DatamartMedication toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedication.class);
  }

  private void validationSearchByIdResult(Medication.Bundle actual) {
    Medication medication = MedicationSamples.R4.create().medication("1");
    assertThat(json(actual))
        .isEqualTo(
            json(
                MedicationSamples.R4.asBundle(
                    "http://abed.com/cool",
                    List.of(medication),
                    1,
                    link(LinkRelation.first, "http://abed.com/cool/Medication?identifier=1", 1, 1),
                    link(LinkRelation.self, "http://abed.com/cool/Medication?identifier=1", 1, 1),
                    link(
                        LinkRelation.last, "http://abed.com/cool/Medication?identifier=1", 1, 1))));
  }
}
