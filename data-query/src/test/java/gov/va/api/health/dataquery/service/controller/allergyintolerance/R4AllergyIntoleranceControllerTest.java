package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.uscorer4.api.resources.AllergyIntolerance;
import gov.va.api.health.uscorer4.api.resources.AllergyIntolerance.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceSamples.R4;
import gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation;
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
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class R4AllergyIntoleranceControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private AllergyIntoleranceRepository repository;

  @SneakyThrows
  private AllergyIntoleranceEntity asEntity(DatamartAllergyIntolerance dm) {
    return AllergyIntoleranceEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  R4AllergyIntoleranceController controller() {
    return new R4AllergyIntoleranceController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockAllergyIntoleranceIdentity(
      String aiPublicId,
      String aiCdwId,
      String patientPublicId,
      String patientCdwId,
      String recorderPublicId,
      String recorderCdwId,
      String authorPublicId,
      String authorCdwId) {
    ResourceIdentity aiResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("ALLERGY_INTOLERANCE")
            .identifier(aiCdwId)
            .build();
    ResourceIdentity patientResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("PATIENT")
            .identifier(patientCdwId)
            .build();
    ResourceIdentity recorderResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("PRACTITIONER")
            .identifier(recorderCdwId)
            .build();
    ResourceIdentity authorResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("PRACTITIONER")
            .identifier(authorCdwId)
            .build();

    when(ids.lookup(aiPublicId)).thenReturn(List.of(aiResource));
    when(ids.register(any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(aiPublicId)
                    .resourceIdentities(List.of(aiResource))
                    .build(),
                Registration.builder()
                    .uuid(patientPublicId)
                    .resourceIdentities(List.of(patientResource))
                    .build(),
                Registration.builder()
                    .uuid(recorderPublicId)
                    .resourceIdentities(List.of(recorderResource))
                    .build(),
                Registration.builder()
                    .uuid(authorPublicId)
                    .resourceIdentities(List.of(authorResource))
                    .build()));
  }

  private Multimap<String, AllergyIntolerance> populateDataForPatients() {
    var fhir = R4.create();
    var datamart = Datamart.create();
    Multimap<String, AllergyIntolerance> allergyIntoleranceByPatient = LinkedHashMultimap.create();
    List<Registration> registrations = new ArrayList<>(10);
    for (int i = 0; i < 10; i++) {
      String patientId = "p" + i % 2;

      String cdwId = Integer.toString(i);
      String publicId = "I2-" + i;

      String recorderCdwId = "10" + i;
      String publicRecorderId = "I2-" + recorderCdwId;

      String authorCdwId = "100" + i;
      String publicAuthorId = "I2-" + authorCdwId;

      var dm = datamart.allergyIntolerance(cdwId, patientId, recorderCdwId, authorCdwId);
      repository.save(asEntity(dm));
      var allergyIntolerance =
          fhir.allergyIntolerance(publicId, patientId, publicRecorderId, publicAuthorId);
      allergyIntoleranceByPatient.put(patientId, allergyIntolerance);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .system("CDW")
              .resource("ALLERGY_INTOLERANCE")
              .identifier(cdwId)
              .build();
      Registration registration =
          Registration.builder()
              .uuid(publicId)
              .resourceIdentities(List.of(resourceIdentity))
              .build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(any())).thenReturn(registrations);
    return allergyIntoleranceByPatient;
  }

  @Test
  public void read() {
    String publicId = "I2-AI1";
    String cdwId = "1";
    String patientPublicId = "P666";
    String patientCdwId = "P666";
    String recorderPublicId = "I2-REC10";
    String recorderCdwId = "10";
    String authorPublicId = "I2-AUTH100";
    String authorCdwId = "100";
    DatamartAllergyIntolerance dm =
        AllergyIntoleranceSamples.Datamart.create()
            .allergyIntolerance(cdwId, patientCdwId, recorderCdwId, authorCdwId);
    repository.save(asEntity(dm));
    mockAllergyIntoleranceIdentity(
        publicId,
        cdwId,
        patientPublicId,
        patientCdwId,
        recorderPublicId,
        recorderCdwId,
        authorPublicId,
        authorCdwId);
    AllergyIntolerance actual = controller().read(publicId);
    assertThat(json(actual))
        .isEqualTo(
            json(
                R4.create()
                    .allergyIntolerance(
                        publicId, patientPublicId, recorderPublicId, authorPublicId)));
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
        R4.create().allergyIntolerance("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://fonzy.com/cool",
                    List.of(allergyIntolerance),
                    1,
                    R4.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    R4.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    R4.link(
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
                R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    1,
                    R4.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        0))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, AllergyIntolerance> allergyIntoleranceByPatient = populateDataForPatients();
    assertThat(json(controller().searchByPatient("p0", 1, 10)))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://fonzy.com/cool",
                    allergyIntoleranceByPatient.get("p0"),
                    allergyIntoleranceByPatient.get("p0").size(),
                    R4.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        10),
                    R4.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        10),
                    R4.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientWithCount0() {
    Multimap<String, AllergyIntolerance> allergyIntoleranceByPatient = populateDataForPatients();
    assertThat(json(controller().searchByPatient("p0", 1, 0)))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    allergyIntoleranceByPatient.get("p0").size(),
                    R4.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?patient=p0",
                        1,
                        0))));
  }

  @SneakyThrows
  private DatamartAllergyIntolerance toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartAllergyIntolerance.class);
  }

  // @Test
  //  public void validate() {
  //    DatamartAllergyIntolerance dm =
  //        AllergyIntoleranceSamples.Datamart.create().allergyIntolerance();
  //    AllergyIntolerance allergyIntolerance =
  //        AllergyIntoleranceSamples.R4
  //            .create()
  //            .allergyIntolerance("1", dm.patient().reference().get());
  //    assertThat(
  //            controller()
  //                .validate(
  //                    AllergyIntoleranceSamples.Dstu2.asBundle(
  //                        "http://fonzy.com/cool",
  //                        List.of(allergyIntolerance),
  //                        1,
  //                        R4.link(
  //                            LinkRelation.first,
  //                            "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
  //                            1,
  //                            1),
  //                        R4.link(
  //                            LinkRelation.self,
  //                            "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
  //                            1,
  //                            1),
  //                        R4.link(
  //                            LinkRelation.last,
  //                            "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
  //                            1,
  //                            1))))
  //        .isEqualTo(Dstu2Validator.ok());
  //  }

  private void validateSearchByIdResult(DatamartAllergyIntolerance dm, Bundle actual) {
    AllergyIntolerance allergyIntolerance =
        R4.create().allergyIntolerance("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://fonzy.com/cool",
                    List.of(allergyIntolerance),
                    1,
                    R4.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    R4.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1),
                    R4.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/AllergyIntolerance?identifier=1",
                        1,
                        1))));
  }
}
