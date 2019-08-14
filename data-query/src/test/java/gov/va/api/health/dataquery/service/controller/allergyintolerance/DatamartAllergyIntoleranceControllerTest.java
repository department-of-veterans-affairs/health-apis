package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntoleranceSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntoleranceSamples.Fhir;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.ArrayList;
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
public class DatamartAllergyIntoleranceControllerTest {

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private TestEntityManager entityManager;

  @Autowired private AllergyIntoleranceRepository repository;

  @SneakyThrows
  private AllergyIntoleranceEntity asEntity(DatamartAllergyIntolerance dm) {
    return AllergyIntoleranceEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().get().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  AllergyIntoleranceController controller() {
    return new AllergyIntoleranceController(
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

  public void mockAllergyIntoleranceIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("ALLERGY_INTOLERANCE")
            .identifier(cdwId)
            .build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  private Multimap<String, AllergyIntolerance> populateData() {
    var fhir = Fhir.create();
    var datamart = Datamart.create();
    var allergyIntoleranceByPatient = LinkedHashMultimap.<String, AllergyIntolerance>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String patientId = "p" + i % 2;
      String cdwId = "" + i;
      String publicId = "90" + i;
      var dm = datamart.allergyIntolerance(cdwId, patientId);
      repository.save(asEntity(dm));
      var medicationStatement = fhir.allergyIntolerance(publicId, patientId);
      allergyIntoleranceByPatient.put(patientId, medicationStatement);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .system("CDW")
              .resource("ALLERGY_INTOLERANCE")
              .identifier(cdwId)
              .build();
      Registration registration =
          Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return allergyIntoleranceByPatient;
  }

  @Test
  public void read() {
    DatamartAllergyIntolerance dm =
        DatamartAllergyIntoleranceSamples.Datamart.create().allergyIntolerance();
    repository.save(asEntity(dm));
    mockAllergyIntoleranceIdentity("1", dm.cdwId());
    AllergyIntolerance actual = controller().read("true", "1");
    assertThat(json(actual)).isEqualTo(json(Fhir.create().allergyIntolerance("1")));
  }

  @Test
  public void readRaw() {
    DatamartAllergyIntolerance dm =
        DatamartAllergyIntoleranceSamples.Datamart.create().allergyIntolerance();
    repository.save(asEntity(dm));
    mockAllergyIntoleranceIdentity("1", dm.cdwId());
    String json = controller().readRaw("1");
    assertThat(toObject(json)).isEqualTo(dm);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockAllergyIntoleranceIdentity("1", "1");
    controller().readRaw("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockAllergyIntoleranceIdentity("1", "1");
    controller().read("true", "1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    controller().read("true", "1");
  }

  @Test
  public void searchById() {
    DatamartAllergyIntolerance dm = Datamart.create().allergyIntolerance();
    repository.save(asEntity(dm));
    mockAllergyIntoleranceIdentity("1", dm.cdwId());
    Bundle actual = controller().searchById("true", "1", 1, 1);
    AllergyIntolerance allergyIntolerance =
        Fhir.create().allergyIntolerance("1", dm.patient().get().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(allergyIntolerance),
                    Fhir.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    Fhir.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    Fhir.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, AllergyIntolerance> allergyIntoleranceByPatient = populateData();
    assertThat(json(controller().searchByPatient("true", "p0", 1, 10)))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    allergyIntoleranceByPatient.get("p0"),
                    Fhir.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        10),
                    Fhir.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        10),
                    Fhir.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        10))));
  }

  @SneakyThrows
  private DatamartAllergyIntolerance toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartAllergyIntolerance.class);
  }
}
