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
import java.util.stream.Collectors;
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
        .performedOnEpochTime(dm.performedDateTime().get().toEpochMilli())
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
    // clark - has procedures
    // superman - no procedures
    DatamartProcedure dm =
        Datamart.create().procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    Procedure actual = controller().read("true", "clrks-procedure", "superman");
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.create()
                    .procedure(
                        "clrks-procedure", "superman", dm.performedDateTime().get().toString())));
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
    // clark - has procedures
    // superman - no procedures
    DatamartProcedure dm =
        Datamart.create().procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    Bundle actual = controller().searchById("true", "superman", "clrks-procedure", 1, 1);
    Procedure procedure =
        Fhir.create()
            .procedure("clrks-procedure", "superman", dm.performedDateTime().get().toString());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(procedure),
                    link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Procedure?identifier=clrks-procedure",
                        1,
                        1),
                    link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Procedure?identifier=clrks-procedure",
                        1,
                        1),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Procedure?identifier=clrks-procedure",
                        1,
                        1))));
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
    Multimap<String, Procedure> procedureByPatient = populateData();
    // Procedure Dates for p0:
    // 2005-01-10T07:57:00Z
    // 2005-01-12T07:57:00Z
    // 2005-01-14T07:57:00Z
    // 2005-01-16T07:57:00Z
    // 2005-01-18T07:57:00Z
    Multimap<String, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        "gt2004",
        List.of(
            "2005-01-10T07:57:00Z",
            "2005-01-12T07:57:00Z",
            "2005-01-14T07:57:00Z",
            "2005-01-16T07:57:00Z",
            "2005-01-18T07:57:00Z"));
    testDates.putAll("eq2005-01-14", List.of("2005-01-14T07:57:00Z"));
    testDates.putAll(
        "ne2005-01-14",
        List.of(
            "2005-01-10T07:57:00Z",
            "2005-01-12T07:57:00Z",
            "2005-01-16T07:57:00Z",
            "2005-01-18T07:57:00Z"));
    testDates.putAll(
        "le2005-01-14",
        List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z", "2005-01-14T07:57:00Z"));
    testDates.putAll("lt2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll("eb2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll(
        "ge2005-01-14",
        List.of("2005-01-14T07:57:00Z", "2005-01-16T07:57:00Z", "2005-01-18T07:57:00Z"));
    testDates.putAll("gt2005-01-14", List.of("2005-01-16T07:57:00Z", "2005-01-18T07:57:00Z"));
    testDates.putAll("sa2005-01-14", List.of("2005-01-16T07:57:00Z", "2005-01-18T07:57:00Z"));

    for (var date : testDates.keySet()) {
      assertThat(
              json(controller().searchByPatientAndDate("true", "p0", new String[] {date}, 1, 10)))
          .isEqualTo(
              json(
                  Fhir.asBundle(
                      "http://fonzy.com/cool",
                      procedureByPatient.get("p0").stream()
                          .filter(p -> testDates.get(date).contains(p.performedDateTime()))
                          .collect(Collectors.toList()),
                      link(
                          LinkRelation.first,
                          "http://fonzy.com/cool/Procedure?date=" + date + "&patient=p0",
                          1,
                          10),
                      link(
                          LinkRelation.self,
                          "http://fonzy.com/cool/Procedure?date=" + date + "&patient=p0",
                          1,
                          10),
                      link(
                          LinkRelation.last,
                          "http://fonzy.com/cool/Procedure?date=" + date + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  public void searchByPatientAndDateTwoDates() {
    fail();
  }

  @Test
  public void searchByPatientSuperman() {
    // clark - has procedures
    // superman - no procedures
    DatamartProcedure dm =
        Datamart.create().procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    Bundle actual = controller().searchByPatientAndDate("true", "superman", null, 1, 1);
    Procedure procedure =
        Fhir.create()
            .procedure("clrks-procedure", "superman", dm.performedDateTime().get().toString());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(procedure),
                    link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Procedure?patient=superman",
                        1,
                        1),
                    link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Procedure?patient=superman",
                        1,
                        1),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Procedure?patient=superman",
                        1,
                        1))));
  }

  @SneakyThrows
  private DatamartProcedure toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartProcedure.class);
  }
}
