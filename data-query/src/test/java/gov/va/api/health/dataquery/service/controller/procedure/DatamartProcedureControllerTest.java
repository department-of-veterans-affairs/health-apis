package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedureSamples.Fhir.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.argonaut.api.resources.Procedure.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedureSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedureSamples.Fhir;
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
public class DatamartProcedureControllerTest {
  private IdentityService ids = mock(IdentityService.class);
  @Autowired private ProcedureRepository repository;
  @Autowired private TestEntityManager entityManager;

  @SneakyThrows
  private ProcedureEntity asEntity(DatamartProcedure dm) {
    return ProcedureEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  ProcedureController controller() {
    return new ProcedureController(
        true,
        "clark",
        "superman",
        "Clark Kent",
        "Superman",
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

  public void mockProcedureIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("PROCEDURE").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  private Multimap<String, Procedure> populateData() {
    var fhir = Fhir.create();
    var datamart = Datamart.create();
    var procedureByPatient = LinkedHashMultimap.<String, Procedure>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String patientId = "p" + i % 2;
      String cdwId = "" + i;
      String publicId = "90" + i;
      String date = "2005-01-1" + i + "T07:57:00Z";
      var dm = datamart.procedure(cdwId, patientId, date);
      repository.save(asEntity(dm));
      var procedure = fhir.procedure(publicId, patientId, date);
      procedureByPatient.put(patientId, procedure);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder().system("CDW").resource("PROCEDURE").identifier(cdwId).build();
      Registration registration =
          Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return procedureByPatient;
  }

  @Test
  public void read() {
    DatamartProcedure dm = Datamart.create().procedure();
    repository.save(asEntity(dm));
    mockProcedureIdentity("1", dm.cdwId());
    Procedure actual = controller().read("true", "1", "1");
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.create()
                    .procedure(
                        "1",
                        dm.patient().reference().get(),
                        dm.performedDateTime().get().toString())));
  }

  @Test
  public void readRaw() {
    DatamartProcedure dm = Datamart.create().procedure();
    repository.save(asEntity(dm));
    mockProcedureIdentity("1", dm.cdwId());
    String json = controller().readRaw("1");
    assertThat(toObject(json)).isEqualTo(dm);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockProcedureIdentity("1", "1");
    controller().readRaw("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1");
  }

  @Test
  public void readSuperman() {
    fail();
  }

  @Test
  public void searchById() {
    DatamartProcedure dm = Datamart.create().procedure();
    repository.save(asEntity(dm));
    mockProcedureIdentity("1", dm.cdwId());
    Bundle actual = controller().searchById("true", "1", "1", 1, 1);
    Procedure procedure =
        Fhir.create()
            .procedure(
                "1", dm.patient().reference().get(), dm.performedDateTime().get().toString());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(procedure),
                    link(LinkRelation.first, "http://fonzy.com/cool/Procedure?identifier=1", 1, 1),
                    link(LinkRelation.self, "http://fonzy.com/cool/Procedure?identifier=1", 1, 1),
                    link(
                        LinkRelation.last, "http://fonzy.com/cool/Procedure?identifier=1", 1, 1))));
  }

  @Test
  public void searchByIdSuperman() {
    fail();
  }

  @Test
  public void searchByPatientAndDateNoDates() {
    Multimap<String, Procedure> procedureByPatient = populateData();
    assertThat(json(controller().searchByPatientAndDate("true", "p0", null, 1, 10)))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    procedureByPatient.get("p0"),
                    link(LinkRelation.first, "http://fonzy.com/cool/Procedure?patient=p0", 1, 10),
                    link(LinkRelation.self, "http://fonzy.com/cool/Procedure?patient=p0", 1, 10),
                    link(LinkRelation.last, "http://fonzy.com/cool/Procedure?patient=p0", 1, 10))));
  }

  @Test
  public void searchByPatientAndDateOneDate() {
    fail();
  }

  @Test
  public void searchByPatientAndDateTwoDates() {
    fail();
  }

  @Test
  public void searchByPatientSuperman() {
    fail();
  }

  @SneakyThrows
  private DatamartProcedure toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartProcedure.class);
  }
}
