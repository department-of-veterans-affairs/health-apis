package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
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
import gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.uscorer4.api.resources.AllergyIntolerance;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class R4AllergyIntoleranceControllerTest {
  @Autowired private AllergyIntoleranceRepository repository;

  private HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @SneakyThrows
  private static AllergyIntoleranceEntity asEntity(DatamartAllergyIntolerance dm) {
    return AllergyIntoleranceEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @SneakyThrows
  private static String json(Object obj) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
  }

  @SneakyThrows
  private static DatamartAllergyIntolerance toDatamart(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartAllergyIntolerance.class);
  }

  private R4AllergyIntoleranceController _controller() {
    return new R4AllergyIntoleranceController(
        new R4Bundler(
            new ConfigurableBaseUrlPageLinks("http://fhir.com", "unused", "unused", "r4")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  private void _mockAllergyIntoleranceIdentity(
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

  private Multimap<String, AllergyIntolerance> _populateDataForPatients() {
    var fhir = AllergyIntoleranceSamples.R4.create();
    var datamart = AllergyIntoleranceSamples.Datamart.create();
    Multimap<String, AllergyIntolerance> allergyIntoleranceByPatient = LinkedHashMultimap.create();
    List<Registration> registrations = new ArrayList<>(10);
    for (int i = 0; i < 10; i++) {
      String patientCdwId = "p" + i % 2;
      String patientPublicId = patientCdwId;
      String aiCdwId = Integer.toString(i);
      String aiPublicId = "I2-" + i;
      String recorderCdwId = "10" + i;
      String recorderPublicId = "I2-" + recorderCdwId;
      String authorCdwId = "100" + i;
      String authorPublicId = "I2-" + authorCdwId;
      var dm = datamart.allergyIntolerance(aiCdwId, patientCdwId, recorderCdwId, authorCdwId);
      repository.save(asEntity(dm));
      var allergyIntolerance =
          fhir.allergyIntolerance(aiPublicId, patientPublicId, recorderPublicId, authorPublicId);
      allergyIntoleranceByPatient.put(patientPublicId, allergyIntolerance);

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

      registrations.addAll(
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
    _mockAllergyIntoleranceIdentity(
        publicId,
        cdwId,
        patientPublicId,
        patientCdwId,
        recorderPublicId,
        recorderCdwId,
        authorPublicId,
        authorCdwId);
    AllergyIntolerance actual = _controller().read(publicId);
    assertThat(json(actual))
        .isEqualTo(
            json(
                AllergyIntoleranceSamples.R4
                    .create()
                    .allergyIntolerance(
                        publicId, patientPublicId, recorderPublicId, authorPublicId)));
  }

  @Test
  public void readRaw() {
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
    AllergyIntoleranceEntity entity = asEntity(dm);
    repository.save(entity);
    _mockAllergyIntoleranceIdentity(
        publicId,
        cdwId,
        patientPublicId,
        patientCdwId,
        recorderPublicId,
        recorderCdwId,
        authorPublicId,
        authorCdwId);
    String json = _controller().readRaw(publicId, response);
    assertThat(toDatamart(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    _mockAllergyIntoleranceIdentity("1", "1", "1", "1", "1", "1", "1", "1");
    _controller().readRaw("1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    _controller().readRaw("1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    _mockAllergyIntoleranceIdentity("1", "1", "1", "1", "1", "1", "1", "1");
    _controller().read("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    _controller().read("1");
  }

  @Test
  public void searchById() {
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
    _mockAllergyIntoleranceIdentity(
        publicId,
        cdwId,
        patientPublicId,
        patientCdwId,
        recorderPublicId,
        recorderCdwId,
        authorPublicId,
        authorCdwId);
    AllergyIntolerance.Bundle actual = _controller().searchById(publicId, 1, 1);
    AllergyIntolerance allergyIntolerance =
        AllergyIntoleranceSamples.R4
            .create()
            .allergyIntolerance(publicId, patientPublicId, recorderPublicId, authorPublicId);
    assertThat(json(actual))
        .isEqualTo(
            json(
                AllergyIntoleranceSamples.R4.asBundle(
                    "http://fhir.com/r4",
                    List.of(allergyIntolerance),
                    1,
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.first,
                        "http://fhir.com/r4/AllergyIntolerance?identifier=I2-AI1",
                        1,
                        1),
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.self,
                        "http://fhir.com/r4/AllergyIntolerance?identifier=I2-AI1",
                        1,
                        1),
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.last,
                        "http://fhir.com/r4/AllergyIntolerance?identifier=I2-AI1",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
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
    _mockAllergyIntoleranceIdentity(
        publicId,
        cdwId,
        patientPublicId,
        patientCdwId,
        recorderPublicId,
        recorderCdwId,
        authorPublicId,
        authorCdwId);
    AllergyIntolerance.Bundle actual = _controller().searchByIdentifier(publicId, 1, 1);
    AllergyIntolerance allergyIntolerance =
        AllergyIntoleranceSamples.R4
            .create()
            .allergyIntolerance(publicId, patientPublicId, recorderPublicId, authorPublicId);
    assertThat(json(actual))
        .isEqualTo(
            json(
                AllergyIntoleranceSamples.R4.asBundle(
                    "http://fhir.com/r4",
                    List.of(allergyIntolerance),
                    1,
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.first,
                        "http://fhir.com/r4/AllergyIntolerance?identifier=I2-AI1",
                        1,
                        1),
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.self,
                        "http://fhir.com/r4/AllergyIntolerance?identifier=I2-AI1",
                        1,
                        1),
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.last,
                        "http://fhir.com/r4/AllergyIntolerance?identifier=I2-AI1",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifierWithCount0() {
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
    _mockAllergyIntoleranceIdentity(
        publicId,
        cdwId,
        patientPublicId,
        patientCdwId,
        recorderPublicId,
        recorderCdwId,
        authorPublicId,
        authorCdwId);
    assertThat(json(_controller().searchByIdentifier(publicId, 1, 0)))
        .isEqualTo(
            json(
                AllergyIntoleranceSamples.R4.asBundle(
                    "http://fhir.com/r4",
                    emptyList(),
                    1,
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.self,
                        "http://fhir.com/r4/AllergyIntolerance?identifier=I2-AI1",
                        1,
                        0))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, AllergyIntolerance> allergyIntoleranceByPatient = _populateDataForPatients();
    assertThat(json(_controller().searchByPatient("p0", 1, 10)))
        .isEqualTo(
            json(
                AllergyIntoleranceSamples.R4.asBundle(
                    "http://fhir.com/r4",
                    allergyIntoleranceByPatient.get("p0"),
                    allergyIntoleranceByPatient.get("p0").size(),
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.first,
                        "http://fhir.com/r4/AllergyIntolerance?patient=p0",
                        1,
                        10),
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.self,
                        "http://fhir.com/r4/AllergyIntolerance?patient=p0",
                        1,
                        10),
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.last,
                        "http://fhir.com/r4/AllergyIntolerance?patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientWithCount0() {
    Multimap<String, AllergyIntolerance> allergyIntoleranceByPatient = _populateDataForPatients();
    assertThat(json(_controller().searchByPatient("p0", 1, 0)))
        .isEqualTo(
            json(
                AllergyIntoleranceSamples.R4.asBundle(
                    "http://fhir.com/r4",
                    emptyList(),
                    allergyIntoleranceByPatient.get("p0").size(),
                    AllergyIntoleranceSamples.R4.link(
                        LinkRelation.self,
                        "http://fhir.com/r4/AllergyIntolerance?patient=p0",
                        1,
                        0))));
  }
}
