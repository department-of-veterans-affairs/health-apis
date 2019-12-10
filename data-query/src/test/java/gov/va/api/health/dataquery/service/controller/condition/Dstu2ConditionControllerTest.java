package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.Dstu2.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.Condition;
import gov.va.api.health.argonaut.api.resources.Condition.Bundle;
import gov.va.api.health.argonaut.api.resources.Condition.ClinicalStatusCode;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.Dstu2;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.Category;
import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition.ClinicalStatus;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
public class Dstu2ConditionControllerTest {

  HttpServletResponse response = mock(HttpServletResponse.class);

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

  Dstu2ConditionController controller() {
    return new Dstu2ConditionController(
        new Dstu2Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockConditionIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("CONDITION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  private Multimap<String, Condition> populateData() {
    var fhir = Dstu2.create();
    var datamart = Datamart.create();
    var conditionsByPatient = LinkedHashMultimap.<String, Condition>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String dateRecorded = "2005-01-1" + i;
      String patientId = "p" + i % 2;
      String cdwId = "cdw-" + i;
      String publicId = "public" + i;
      DatamartCondition dm = datamart.condition(cdwId, patientId, dateRecorded);
      dm.category(Category.values()[i % 2]);
      dm.clinicalStatus(ClinicalStatus.values()[i % 2]);
      repository.save(asEntity(dm));
      Condition condition = fhir.condition(publicId, patientId, dateRecorded);
      condition.clinicalStatus(
          dm.clinicalStatus() == ClinicalStatus.active
              ? ClinicalStatusCode.active
              : ClinicalStatusCode.resolved);
      condition.category(
          dm.category() == Category.problem ? fhir.problemCategory() : fhir.diagnosisCategory());
      conditionsByPatient.put(patientId, condition);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder().system("CDW").resource("CONDITION").identifier(cdwId).build();
      Registration registration =
          Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return conditionsByPatient;
  }

  @Test
  public void read() {
    DatamartCondition dm = Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("x", dm.cdwId());
    Condition actual = controller().read("x");
    assertThat(actual).isEqualTo(ConditionSamples.Dstu2.create().condition("x"));
  }

  @Test
  public void readRaw() {
    DatamartCondition dm = Datamart.create().condition();
    ConditionEntity entity = asEntity(dm);
    repository.save(entity);
    mockConditionIdentity("x", dm.cdwId());
    String json = controller().readRaw("x", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockConditionIdentity("x", "x");
    controller().readRaw("x", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("x", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockConditionIdentity("x", "x");
    controller().read("x");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    controller().read("x");
  }

  @Test
  public void searchById() {
    DatamartCondition dm = Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("x", dm.cdwId());
    Bundle actual = controller().searchById("x", 1, 1);
    Condition condition = Dstu2.create().condition("x");
    assertThat(json(actual))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(condition),
                    1,
                    link(LinkRelation.first, "http://fonzy.com/cool/Condition?identifier=x", 1, 1),
                    link(LinkRelation.self, "http://fonzy.com/cool/Condition?identifier=x", 1, 1),
                    link(
                        LinkRelation.last, "http://fonzy.com/cool/Condition?identifier=x", 1, 1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartCondition dm = Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("1", dm.cdwId());
    Bundle actual = controller().searchByIdentifier("1", 1, 1);
    validateSearchByIdResult(dm, actual);
  }

  @Test
  public void searchByIdentifierWithCount0() {
    DatamartCondition dm = Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("1", dm.cdwId());
    assertThat(json(controller().searchByIdentifier("1", 1, 0)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    1,
                    Dstu2.link(
                        LinkRelation.self, "http://fonzy.com/cool/Condition?identifier=1", 1, 0))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 10)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    conditionsByPatient.get("p0"),
                    conditionsByPatient.get("p0").size(),
                    link(LinkRelation.first, "http://fonzy.com/cool/Condition?patient=p0", 1, 10),
                    link(LinkRelation.self, "http://fonzy.com/cool/Condition?patient=p0", 1, 10),
                    link(LinkRelation.last, "http://fonzy.com/cool/Condition?patient=p0", 1, 10))));
  }

  @Test
  public void searchByPatientAndCategory() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    assertThat(json(controller().searchByPatientAndCategory("p0", "diagnosis", 1, 10)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    conditionsByPatient
                        .get("p0")
                        .stream()
                        .filter(c -> "Diagnosis".equalsIgnoreCase(c.category().text()))
                        .collect(Collectors.toList()),
                    (int)
                        conditionsByPatient
                            .get("p0")
                            .stream()
                            .filter(c -> "Diagnosis".equalsIgnoreCase(c.category().text()))
                            .count(),
                    link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Condition?category=diagnosis&patient=p0",
                        1,
                        10),
                    link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Condition?category=diagnosis&patient=p0",
                        1,
                        10),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Condition?category=diagnosis&patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndClinicalStatus() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    assertThat(json(controller().searchByPatientAndClinicalStatus("p0", "active", 1, 10)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    conditionsByPatient
                        .get("p0")
                        .stream()
                        .filter(c -> Condition.ClinicalStatusCode.active == c.clinicalStatus())
                        .collect(Collectors.toList()),
                    (int)
                        conditionsByPatient
                            .get("p0")
                            .stream()
                            .filter(c -> Condition.ClinicalStatusCode.active == c.clinicalStatus())
                            .count(),
                    link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinicalstatus=active&patient=p0",
                        1,
                        10),
                    link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinicalstatus=active&patient=p0",
                        1,
                        10),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinicalstatus=active&patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndMultipleClinicalStatus() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    assertThat(json(controller().searchByPatientAndClinicalStatus("p0", "active,resolved", 1, 10)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    conditionsByPatient.get("p0"),
                    conditionsByPatient.get("p0").size(),
                    link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinicalstatus=active,resolved&patient=p0",
                        1,
                        10),
                    link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinicalstatus=active,resolved&patient=p0",
                        1,
                        10),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinicalstatus=active,resolved&patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientWithCount0() {
    Multimap<String, Condition> conditionByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 0)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    conditionByPatient.get("p0").size(),
                    Dstu2.link(
                        LinkRelation.self, "http://fonzy.com/cool/Condition?patient=p0", 1, 0))));
  }

  @SneakyThrows
  private DatamartCondition toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartCondition.class);
  }

  private void validateSearchByIdResult(DatamartCondition dm, Bundle actual) {
    Condition condition =
        Dstu2.create().condition("1", dm.patient().reference().get(), "2011-06-27");
    assertThat(json(actual))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(condition),
                    1,
                    Dstu2.link(
                        LinkRelation.first, "http://fonzy.com/cool/Condition?identifier=1", 1, 1),
                    Dstu2.link(
                        LinkRelation.self, "http://fonzy.com/cool/Condition?identifier=1", 1, 1),
                    Dstu2.link(
                        LinkRelation.last, "http://fonzy.com/cool/Condition?identifier=1", 1, 1))));
  }
}
