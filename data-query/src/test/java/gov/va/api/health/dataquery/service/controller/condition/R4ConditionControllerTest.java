package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.uscorer4.api.resources.Condition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class R4ConditionControllerTest {
  private static final String PROBLEM_AND_DIAGNOSIS_SYSTEM =
      "http://terminology.hl7.org/CodeSystem/condition-category";

  private static final String CLINICAL_STATUS_SYSTEM =
      "http://terminology.hl7.org/CodeSystem/condition-clinical";

  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private ConditionRepository repository;

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

  R4ConditionController controller() {
    return new R4ConditionController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  public void mockConditionIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("CONDITION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  private Multimap<String, Condition> populateData() {
    var fhir = ConditionSamples.R4.create();
    var datamart = ConditionSamples.Datamart.create();
    var conditionsByPatient = LinkedHashMultimap.<String, Condition>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String dateRecorded = "2005-01-1" + i;
      String patientId = "p" + i % 2;
      String cdwId = "cdw-" + i;
      String publicId = "public" + i;
      DatamartCondition dm = datamart.condition(cdwId, patientId, dateRecorded);
      dm.category(DatamartCondition.Category.values()[i % 2]);
      dm.clinicalStatus(DatamartCondition.ClinicalStatus.values()[i % 2]);
      repository.save(asEntity(dm));
      Condition condition = fhir.condition(publicId, patientId, dateRecorded);
      condition.clinicalStatus(
          dm.clinicalStatus() == DatamartCondition.ClinicalStatus.active
              ? fhir.clinicalStatusActive()
              : fhir.clinicalStatusResolved());
      condition.category(
          dm.category() == DatamartCondition.Category.problem
              ? fhir.categoryProblem()
              : fhir.categoryDiagnosis());
      conditionsByPatient.put(patientId, condition);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder().system("CDW").resource("CONDITION").identifier(cdwId).build();
      Registration registration =
          Registration.builder()
              .uuid(publicId)
              .resourceIdentities(List.of(resourceIdentity))
              .build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return conditionsByPatient;
  }

  @Test
  public void read() {
    DatamartCondition dm = ConditionSamples.Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("x", dm.cdwId());
    Condition actual = controller().read("x");
    assertThat(actual).isEqualTo(ConditionSamples.R4.create().condition("x"));
  }

  @Test
  public void readRaw() {
    DatamartCondition dm = ConditionSamples.Datamart.create().condition();
    ConditionEntity entity = asEntity(dm);
    repository.save(entity);
    mockConditionIdentity("x", dm.cdwId());
    String json = controller().readRaw("x", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockConditionIdentity("x", "x");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("x", response));
  }

  @Test
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("x", response));
  }

  @Test
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockConditionIdentity("x", "x");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  public void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  public void searchById() {
    DatamartCondition dm = ConditionSamples.Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("x", dm.cdwId());
    Condition.Bundle actual = controller().searchById("x", 1, 1);
    Condition condition = ConditionSamples.R4.create().condition("x");
    assertThat(toJson(actual))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    List.of(condition),
                    1,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?identifier=x",
                        1,
                        1),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?identifier=x",
                        1,
                        1),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?identifier=x",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartCondition dm = ConditionSamples.Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("1", dm.cdwId());
    Condition.Bundle actual = controller().searchByIdentifier("1", 1, 1);
    validateSearchByIdResult(dm, actual);
  }

  @Test
  public void searchByIdentifierWithCount0() {
    DatamartCondition dm = ConditionSamples.Datamart.create().condition();
    repository.save(asEntity(dm));
    mockConditionIdentity("1", dm.cdwId());
    assertThat(toJson(controller().searchByIdentifier("1", 1, 0)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    1,
                    ConditionSamples.R4.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?identifier=1",
                        1,
                        0))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    assertThat(toJson(controller().searchByPatient("p0", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsByPatient.get("p0"),
                    conditionsByPatient.get("p0").size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndBadCategoryReturnsEmptyBundle() {
    assertThat(toJson(controller().searchByPatientAndCategory("x", "nope", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?category=nope&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?category=nope&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?category=nope&patient=x",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndBadClinicalStatusReturnsEmptyBundle() {
    assertThat(toJson(controller().searchByPatientAndClinicalStatus("x", "nope", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinical-status=nope&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinical-status=nope&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinical-status=nope&patient=x",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndCategoryCode() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    var conditionsP0 =
        conditionsByPatient.get("p0").stream()
            .filter(c -> "Encounter Diagnosis".equalsIgnoreCase(c.category().get(0).text()))
            .collect(Collectors.toList());
    assertThat(toJson(controller().searchByPatientAndCategory("p0", "encounter-diagnosis", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsP0,
                    conditionsP0.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?category=encounter-diagnosis&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?category=encounter-diagnosis&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?category=encounter-diagnosis&patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndCategorySystem() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    assertThat(
            toJson(
                controller()
                    .searchByPatientAndCategory("p0", PROBLEM_AND_DIAGNOSIS_SYSTEM + "|", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsByPatient.get("p0"),
                    conditionsByPatient.get("p0").size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?category="
                            + PROBLEM_AND_DIAGNOSIS_SYSTEM
                            + "|&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?category="
                            + PROBLEM_AND_DIAGNOSIS_SYSTEM
                            + "|&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?category="
                            + PROBLEM_AND_DIAGNOSIS_SYSTEM
                            + "|&patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndCategorySystemAndCode() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    var conditionsP0 =
        conditionsByPatient.get("p0").stream()
            .filter(c -> "Encounter Diagnosis".equalsIgnoreCase(c.category().get(0).text()))
            .collect(Collectors.toList());
    assertThat(
            toJson(
                controller()
                    .searchByPatientAndCategory(
                        "p0", PROBLEM_AND_DIAGNOSIS_SYSTEM + "|encounter-diagnosis", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsP0,
                    conditionsP0.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?category="
                            + PROBLEM_AND_DIAGNOSIS_SYSTEM
                            + "|encounter-diagnosis&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?category="
                            + PROBLEM_AND_DIAGNOSIS_SYSTEM
                            + "|encounter-diagnosis&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?category="
                            + PROBLEM_AND_DIAGNOSIS_SYSTEM
                            + "|encounter-diagnosis&patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndClinicalStatusCode() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    var conditionsP0 =
        conditionsByPatient.get("p0").stream()
            .filter(c -> "Active".equalsIgnoreCase(c.clinicalStatus().text()))
            .collect(Collectors.toList());
    assertThat(toJson(controller().searchByPatientAndClinicalStatus("p0", "active", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsP0,
                    conditionsP0.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinical-status=active&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinical-status=active&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinical-status=active&patient=p0",
                        1,
                        10))));
    var conditionsP1 =
        conditionsByPatient.get("p1").stream()
            .filter(c -> "Resolved".equalsIgnoreCase(c.clinicalStatus().text()))
            .collect(Collectors.toList());
    assertThat(toJson(controller().searchByPatientAndClinicalStatus("p1", "resolved", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsP1,
                    conditionsP1.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinical-status=resolved&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinical-status=resolved&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinical-status=resolved&patient=p1",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndClinicalStatusOfInactiveIsTreatedAsResolved() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    var conditionsP1 =
        conditionsByPatient.get("p1").stream()
            .filter(c -> "Resolved".equalsIgnoreCase(c.clinicalStatus().text()))
            .collect(Collectors.toList());
    assertThat(
            toJson(
                controller()
                    .searchByPatientAndClinicalStatus(
                        "p1", CLINICAL_STATUS_SYSTEM + "|inactive", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsP1,
                    conditionsP1.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinical-status="
                            + CLINICAL_STATUS_SYSTEM
                            + "|inactive&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinical-status="
                            + CLINICAL_STATUS_SYSTEM
                            + "|inactive&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinical-status="
                            + CLINICAL_STATUS_SYSTEM
                            + "|inactive&patient=p1",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndClinicalStatusSystem() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    assertThat(
            toJson(
                controller()
                    .searchByPatientAndClinicalStatus("p0", CLINICAL_STATUS_SYSTEM + "|", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsByPatient.get("p0"),
                    conditionsByPatient.get("p0").size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinical-status="
                            + CLINICAL_STATUS_SYSTEM
                            + "|&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinical-status="
                            + CLINICAL_STATUS_SYSTEM
                            + "|&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinical-status="
                            + CLINICAL_STATUS_SYSTEM
                            + "|&patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndClinicalStatusUnsupportedSystemAndSupportedCode() {
    assertThat(
            toJson(controller().searchByPatientAndClinicalStatus("x", "http://nope|active", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinical-status=http://nope|active&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinical-status=http://nope|active&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinical-status=http://nope|active&patient=x",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndClinicalSystemAndCode() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    var conditionsP0 =
        conditionsByPatient.get("p0").stream()
            .filter(c -> "Active".equalsIgnoreCase(c.clinicalStatus().text()))
            .collect(Collectors.toList());
    assertThat(
            toJson(
                controller()
                    .searchByPatientAndClinicalStatus(
                        "p0", CLINICAL_STATUS_SYSTEM + "|active", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsP0,
                    conditionsP0.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinical-status="
                            + CLINICAL_STATUS_SYSTEM
                            + "|active&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinical-status="
                            + CLINICAL_STATUS_SYSTEM
                            + "|active&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinical-status="
                            + CLINICAL_STATUS_SYSTEM
                            + "|active&patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndExplicitlyNoSystemClinicalStatusReturnsEmptyBundle() {
    assertThat(toJson(controller().searchByPatientAndClinicalStatus("x", "|active", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinical-status=|active&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinical-status=|active&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinical-status=|active&patient=x",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndExplicityNoSystemCategoryReturnsEmptyBundle() {
    assertThat(toJson(controller().searchByPatientAndCategory("x", "|encounter-diagnosis", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?category=|encounter-diagnosis&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?category=|encounter-diagnosis&patient=x",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?category=|encounter-diagnosis&patient=x",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndMultipleClinicalStatus() {
    Multimap<String, Condition> conditionsByPatient = populateData();
    assertThat(
            toJson(
                controller()
                    .searchByPatientAndClinicalStatus(
                        "p0", "active," + CLINICAL_STATUS_SYSTEM + "|resolved", 1, 10)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    conditionsByPatient.get("p0"),
                    conditionsByPatient.get("p0").size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?clinical-status=active,"
                            + CLINICAL_STATUS_SYSTEM
                            + "|resolved&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?clinical-status=active,"
                            + CLINICAL_STATUS_SYSTEM
                            + "|resolved&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?clinical-status=active,"
                            + CLINICAL_STATUS_SYSTEM
                            + "|resolved&patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientWithCount0() {
    Multimap<String, Condition> conditionByPatient = populateData();
    assertThat(toJson(controller().searchByPatient("p0", 1, 0)))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    conditionByPatient.get("p0").size(),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?patient=p0",
                        1,
                        0))));
  }

  @SneakyThrows
  String toJson(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @SneakyThrows
  private DatamartCondition toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartCondition.class);
  }

  private void validateSearchByIdResult(DatamartCondition dm, Condition.Bundle actual) {
    Condition condition =
        ConditionSamples.R4.create().condition("1", dm.patient().reference().get(), "2011-06-27");
    assertThat(toJson(actual))
        .isEqualTo(
            toJson(
                ConditionSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    List.of(condition),
                    1,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Condition?identifier=1",
                        1,
                        1),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Condition?identifier=1",
                        1,
                        1),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Condition?identifier=1",
                        1,
                        1))));
  }
}
