package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.argonaut.api.resources.AllergyIntolerance.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceSamples.Dstu2;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.ArrayList;
import java.util.Collections;
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
public class Dstu2AllergyIntoleranceControllerTest {

  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private TestEntityManager entityManager;

  @Autowired private AllergyIntoleranceRepository repository;

  @SneakyThrows
  private AllergyIntoleranceEntity asEntity(DatamartAllergyIntolerance dm) {
    return AllergyIntoleranceEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  Dstu2AllergyIntoleranceController controller() {
    return new Dstu2AllergyIntoleranceController(
        new Dstu2Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
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
    var fhir = Dstu2.create();
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
        AllergyIntoleranceSamples.Datamart.create().allergyIntolerance();
    repository.save(asEntity(dm));
    mockAllergyIntoleranceIdentity("1", dm.cdwId());
    AllergyIntolerance actual = controller().read("1");
    assertThat(json(actual)).isEqualTo(json(Dstu2.create().allergyIntolerance("1")));
  }

  @Test
  public void readRaw() {
    DatamartAllergyIntolerance dm =
        AllergyIntoleranceSamples.Datamart.create().allergyIntolerance();
    AllergyIntoleranceEntity entity = asEntity(dm);
    repository.save(entity);
    mockAllergyIntoleranceIdentity("1", dm.cdwId());
    String json = controller().readRaw("1", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockAllergyIntoleranceIdentity("1", "1");
    controller().readRaw("1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockAllergyIntoleranceIdentity("1", "1");
    controller().read("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    controller().read("1");
  }

  @Test
  public void searchById() {
    DatamartAllergyIntolerance dm = Datamart.create().allergyIntolerance();
    repository.save(asEntity(dm));
    mockAllergyIntoleranceIdentity("1", dm.cdwId());
    Bundle actual = controller().searchById("1", 1, 1);
    AllergyIntolerance allergyIntolerance =
        Dstu2.create().allergyIntolerance("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(allergyIntolerance),
                    1,
                    Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartAllergyIntolerance dm = Datamart.create().allergyIntolerance();
    repository.save(asEntity(dm));
    mockAllergyIntoleranceIdentity("1", dm.cdwId());
    Bundle actual = controller().searchByIdentifier("1", 1, 1);
    validateSearchByIdResult(dm, actual);
  }

  @Test
  public void searchByIdentifierWithCount0() {
    DatamartAllergyIntolerance dm = Datamart.create().allergyIntolerance();
    repository.save(asEntity(dm));
    mockAllergyIntoleranceIdentity("1", dm.cdwId());
    assertThat(json(controller().searchByIdentifier("1", 1, 0)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    1,
                    Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        0))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, AllergyIntolerance> allergyIntoleranceByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 10)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    allergyIntoleranceByPatient.get("p0"),
                    allergyIntoleranceByPatient.get("p0").size(),
                    Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        10),
                    Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        10),
                    Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientWithCount0() {
    Multimap<String, AllergyIntolerance> allergyIntoleranceByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 0)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    allergyIntoleranceByPatient.get("p0").size(),
                    Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        0))));
  }

  @SneakyThrows
  private DatamartAllergyIntolerance toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartAllergyIntolerance.class);
  }

  private void validateSearchByIdResult(DatamartAllergyIntolerance dm, Bundle actual) {
    AllergyIntolerance allergyIntolerance =
        Dstu2.create().allergyIntolerance("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(allergyIntolerance),
                    1,
                    Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1))));
  }
}
