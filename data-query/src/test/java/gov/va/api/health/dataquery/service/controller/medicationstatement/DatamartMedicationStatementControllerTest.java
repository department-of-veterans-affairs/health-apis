package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatementSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatementSamples.Fhir;
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
public class DatamartMedicationStatementControllerTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private MedicationStatementRepository repository;

  private IdentityService ids = mock(IdentityService.class);

  @SneakyThrows
  private MedicationStatementEntity asEntity(DatamartMedicationStatement dm) {
    return MedicationStatementEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  MedicationStatementController controller() {
    return new MedicationStatementController(
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
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  private Multimap<String, MedicationStatement> populateData() {
    var fhir = Fhir.create();
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
          Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return medicationStatementByPatient;
  }

  @Test
  public void read() {
    DatamartMedicationStatement dm =
        DatamartMedicationStatementSamples.Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("1", dm.cdwId());
    MedicationStatement actual = controller().read("true", "1");
    assertThat(json(actual)).isEqualTo(json(Fhir.create().medicationStatement("1")));
  }

  @Test
  public void readRaw() {
    DatamartMedicationStatement dm =
        DatamartMedicationStatementSamples.Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("1", dm.cdwId());
    String json = controller().readRaw("1");
    assertThat(toObject(json)).isEqualTo(dm);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockMedicationStatementIdentity("1", "1");
    controller().readRaw("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1");
  }

  @Test
  public void searchById() {
    DatamartMedicationStatement dm = Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("1", dm.cdwId());
    Bundle actual = controller().searchById("true", "1", 1, 1);
    MedicationStatement medicationStatement =
        Fhir.create().medicationStatement("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(medicationStatement),
                    Fhir.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/MedicationStatement?identifier=1",
                        1,
                        1),
                    Fhir.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/MedicationStatement?identifier=1",
                        1,
                        1),
                    Fhir.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/MedicationStatement?identifier=1",
                        1,
                        1))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, MedicationStatement> medicationStatementByPatient = populateData();
    assertThat(json(controller().searchByPatient("true", "p0", 1, 10)))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    medicationStatementByPatient.get("p0"),
                    Fhir.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/MedicationStatement?patient=p0",
                        1,
                        10),
                    Fhir.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/MedicationStatement?patient=p0",
                        1,
                        10),
                    Fhir.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/MedicationStatement?patient=p0",
                        1,
                        10))));
  }

  @SneakyThrows
  private DatamartMedicationStatement toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedicationStatement.class);
  }
}
