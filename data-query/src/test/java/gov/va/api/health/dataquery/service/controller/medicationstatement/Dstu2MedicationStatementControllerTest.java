package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementSamples.Dstu2;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class Dstu2MedicationStatementControllerTest {
  HttpServletResponse response;

  @Autowired private MedicationStatementRepository repository;

  private IdentityService ids = mock(IdentityService.class);

  @Before
  public void _init() {
    response = mock(HttpServletResponse.class);
  }

  @SneakyThrows
  private MedicationStatementEntity asEntity(DatamartMedicationStatement dm) {
    return MedicationStatementEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  Dstu2MedicationStatementController controller() {
    return new Dstu2MedicationStatementController(
        new Dstu2Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockMedicationStatementIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("MEDICATION_STATEMENT")
            .identifier(cdwId)
            .build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  private Multimap<String, MedicationStatement> populateData() {
    var fhir = Dstu2.create();
    var datamart = Datamart.create();
    var medicationStatementByPatient = LinkedHashMultimap.<String, MedicationStatement>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String patientId = "p" + i % 2;
      String cdwId = "" + i;
      String publicId = "90" + i;
      var dm = datamart.medicationStatement(cdwId, patientId);
      repository.save(asEntity(dm));
      var medicationStatement = fhir.medicationStatement(publicId, patientId);
      medicationStatementByPatient.put(patientId, medicationStatement);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .system("CDW")
              .resource("MEDICATION_STATEMENT")
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
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return medicationStatementByPatient;
  }

  @Test
  public void read() {
    DatamartMedicationStatement dm =
        MedicationStatementSamples.Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("1", dm.cdwId());
    MedicationStatement actual = controller().read("1");
    assertThat(json(actual)).isEqualTo(json(Dstu2.create().medicationStatement("1")));
  }

  @Test
  public void readRaw() {
    DatamartMedicationStatement dm =
        MedicationStatementSamples.Datamart.create().medicationStatement();
    MedicationStatementEntity entity = asEntity(dm);
    repository.save(entity);
    mockMedicationStatementIdentity("1", dm.cdwId());
    String json = controller().readRaw("1", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockMedicationStatementIdentity("1", "1");
    controller().readRaw("1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockMedicationStatementIdentity("1", "1");
    controller().read("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    controller().read("1");
  }

  @Test
  public void searchById() {
    DatamartMedicationStatement dm = Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("1", dm.cdwId());
    Bundle actual = controller().searchById("1", 1, 1);
    validateSearchByIdResult(dm, actual);
  }

  @Test
  public void searchByIdentifier() {
    DatamartMedicationStatement dm = Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("1", dm.cdwId());
    Bundle actual = controller().searchByIdentifier("1", 1, 1);
    validateSearchByIdResult(dm, actual);
  }

  @Test
  public void searchByPatient() {
    Multimap<String, MedicationStatement> medicationStatementByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 10)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    medicationStatementByPatient.get("p0"),
                    medicationStatementByPatient.get("p0").size(),
                    Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/MedicationStatement?patient=p0",
                        1,
                        10),
                    Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/MedicationStatement?patient=p0",
                        1,
                        10),
                    Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/MedicationStatement?patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientWithCount0() {
    Multimap<String, MedicationStatement> medicationStatementByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 0)))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    medicationStatementByPatient.get("p0").size(),
                    Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/MedicationStatement?patient=p0",
                        1,
                        0))));
  }

  @SneakyThrows
  private DatamartMedicationStatement toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedicationStatement.class);
  }

  private void validateSearchByIdResult(DatamartMedicationStatement dm, Bundle actual) {
    MedicationStatement medicationStatement =
        Dstu2.create().medicationStatement("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(medicationStatement),
                    1,
                    Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/MedicationStatement?identifier=1",
                        1,
                        1),
                    Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/MedicationStatement?identifier=1",
                        1,
                        1),
                    Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/MedicationStatement?identifier=1",
                        1,
                        1))));
  }
}
