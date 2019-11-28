package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitionerSamples.Datamart.Fhir.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitionerSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitionerSamples.Datamart.Fhir;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
public class DatamartPractitionerControllerTest {

  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private PractitionerRepository repository;

  @Autowired private TestEntityManager entityManager;

  @SneakyThrows
  private PractitionerEntity asEntity(DatamartPractitioner dm) {
    return PractitionerEntity.builder()
        .cdwId(dm.cdwId())
        .familyName("Joe")
        .givenName("Johnson")
        .npi("1234567")
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  PractitionerController controller() {
    return new PractitionerController(
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

  public void mockPractitionerIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("PRACTITIONER").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }
  
  @Test
  public void read() {
    DatamartPractitioner dm = Datamart.create().practitioner();
    repository.save(asEntity(dm));
    mockPractitionerIdentity("1234", dm.cdwId());
    Practitioner actual = controller().read("true", "1234");
    assertThat(actual).isEqualTo(Fhir.create().practitioner("1234"));
  }

  @Test
  public void readRaw() {
    DatamartPractitioner dm = Datamart.create().practitioner();
    PractitionerEntity entity = asEntity(dm);
    repository.save(entity);
    mockPractitionerIdentity("1234567", dm.cdwId());
    String json = controller().readRaw("1234567", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.npi());
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockPractitionerIdentity("x", "x");
    controller().readRaw("x", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("x", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockPractitionerIdentity("x", "x");
    controller().read("true", "x");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    controller().read("true", "x");
  }

  @Test
  public void searchById() {
    DatamartPractitioner dm = Datamart.create().practitioner();
    repository.save(asEntity(dm));
    mockPractitionerIdentity("1234", dm.cdwId());
    Practitioner.Bundle actual = controller().searchById("true", "1234", 1, 1);
    Practitioner practitioner = Fhir.create().practitioner("1234");
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(practitioner),
                    link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Practitioner?identifier=1234",
                        1,
                        1),
                    link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Practitioner?identifier=1234",
                        1,
                        1),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Practitioner?identifier=1234",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartPractitioner dm = Datamart.create().practitioner();
    repository.save(asEntity(dm));
    mockPractitionerIdentity("1234", dm.cdwId());
    Practitioner.Bundle actual = controller().searchByIdentifier("true", "1234", 1, 1);
    Practitioner practitioner = Fhir.create().practitioner("1234");
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(practitioner),
                    link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Practitioner?identifier=1234",
                        1,
                        1),
                    link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Practitioner?identifier=1234",
                        1,
                        1),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Practitioner?identifier=1234",
                        1,
                        1))));
  }

  @SneakyThrows
  private DatamartPractitioner toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartPractitioner.class);
  }
}
