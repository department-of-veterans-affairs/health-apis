package gov.va.api.health.dataquery.service.controller.procedure;

// import static gov.va.api.health.dataquery.service.controller.procedure.ProcedureSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class R4ProcedureControllerTest {
  HttpServletResponse response;

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private ProcedureRepository repository;

  @Before
  public void _init() {
    response = mock(HttpServletResponse.class);
  }

  @SneakyThrows
  private ProcedureEntity asEntity(DatamartProcedure dm) {
    return ProcedureEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .performedOnEpochTime(dm.performedDateTime().get().toEpochMilli())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  // TODO uncomment the R4ProcedureController once it is created for API-972
  // R4ProcedureController controller() {
  // return new R4ProcedureController(
  // "clark",
  // "superman",
  // "Clark Kent",
  // "Superman",
  // new R4Bundler(
  // new ConfigurableBaseUrlPageLinks("http://abed.com", "cool", "cool", "cool")),
  // repository,
  // WitnessProtection.builder().identityService(ids).build());
  // }
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
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  // TODO: Uncomment lines once ProcedureSamples.R4 exists
  private Multimap<String, Procedure> populateData() {
    // var fhir = ProcedureSamples.R4.create();
    var datamart = ProcedureSamples.Datamart.create();
    var procedureByPatient = LinkedHashMultimap.<String, Procedure>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String patientId = "p" + i % 2;
      String cdwId = "" + i;
      String publicId = "90" + i;
      String date = "2005-01-1" + i + "T07:57:00Z";
      var dm = datamart.procedure(cdwId, patientId, date);
      repository.save(asEntity(dm));
      // var procedure = fhir.procedure(publicId, patientId, date);
      // procedureByPatient.put(patientId, procedure);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder().system("CDW").resource("PROCEDURE").identifier(cdwId).build();
      Registration registration =
          Registration.builder()
              .uuid(publicId)
              .resourceIdentities(List.of(resourceIdentity))
              .build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return procedureByPatient;
  }

  @Test
  @Ignore
  public void read() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    repository.save(asEntity(dm));
    mockProcedureIdentity("1", dm.cdwId());
    // Procedure actual = controller().read("1", "1");
    String tempObj = "Replace Me";
    assertThat(json(tempObj)).isEqualTo(json(tempObj));
  }

  @Test
  @Ignore
  public void readRaw() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    ProcedureEntity entity = asEntity(dm);
    repository.save(entity);
    mockProcedureIdentity("1", dm.cdwId());
    // String json = controller().readRaw("1", "1", response);
    String tempObj = "Replace Me!";
    assertThat(toObject(tempObj)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test
  @Ignore
  public void readRawSuperman() {
    // clark - has procedures
    // superman - no procedures
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    DatamartProcedure dm =
        ProcedureSamples.Datamart.create()
            .procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    // String json = controller().readRaw("clrks-procedure", "superman", response);
    String tempObj = "Replace Me!";
    assertThat(toObject(tempObj)).isEqualTo(dm);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  @Ignore
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    // TODO: Uncomment line(s) below once R4 Procedure exists
    mockProcedureIdentity("1", "1");
    // controller().readRaw("1", "1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  @Ignore
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    // TODO: Uncomment line(s) below once R4 Procedure exists
    // controller().readRaw("1", "1", response);
  }

  @Test
  @Ignore
  public void readSuperman() {
    // clark - has procedures
    // superman - no procedures
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    DatamartProcedure dm =
        ProcedureSamples.Datamart.create()
            .procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    // Procedure actual = controller().read("clrks-procedure", "superman");
    String tempObj = "Replace Me!";
    assertThat(json(tempObj)).isEqualTo(json(tempObj));
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  @Ignore
  public void readThrowsNotFoundWhenDataIsMissing() {
    // TODO: Uncomment line(s) below once R4 Procedure exists
    mockProcedureIdentity("1", "1");
    // controller().read("1", "1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  @Ignore
  public void readThrowsNotFoundWhenIdIsUnknown() {
    // TODO: Uncomment line(s) below once R4 Procedure exists
    // controller().read("1", "1");
  }

  @Test
  @Ignore
  public void searchById() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    repository.save(asEntity(dm));
    mockProcedureIdentity("1", dm.cdwId());
    // Procedure.Bundle actual = controller().searchById("1", "1", 1, 1);
    String tempObj = "Replace Me!";
    // Procedure procedure =
    // ProcedureSamples.R4.create()
    // .procedure(
    // "1", dm.patient().reference().get(), dm.performedDateTime().get().toString());
    assertThat(json(tempObj)).isEqualTo(json(tempObj));
  }

  @Test
  @Ignore
  public void searchByIdSuperman() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    // clark - has procedures
    // superman - no procedures
    DatamartProcedure dm =
        ProcedureSamples.Datamart.create()
            .procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    // Procedure.Bundle actual = controller().searchById("superman", "clrks-procedure", 1, 1);
    // Procedure procedure =
    // ProcedureSamples.R4.create()
    // .procedure("clrks-procedure", "superman", dm.performedDateTime().get().toString());
    String tempObj = "Replace Me!";
    assertThat(json(tempObj)).isEqualTo(json(tempObj));
  }

  @Test
  @Ignore
  public void searchByIdentifier() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    repository.save(asEntity(dm));
    mockProcedureIdentity("1", dm.cdwId());
    // Procedure.Bundle actual = controller().searchByIdentifier("1", "1", 1, 1);
    // Procedure procedure =
    // ProcedureSamples.R4.create()
    // .procedure(
    // "1", dm.patient().reference().get(), dm.performedDateTime().get().toString());
    String tempObj = "Replace Me!";
    assertThat(json(tempObj)).isEqualTo(json(tempObj));
  }

  @Test
  @Ignore
  public void searchByPatientAndDateNoDates() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    Multimap<String, Procedure> procedureByPatient = populateData();
    String tempObj = "Replace Me!";
    assertThat(json(tempObj)).isEqualTo(json(tempObj));
  }

  @Test
  public void searchByPatientAndDateOneDate() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    String tempObj = "Replace Me!";
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
      List<Procedure> resources =
          procedureByPatient.get("p0").stream()
              .filter(p -> testDates.get(date).contains(p.performedDateTime()))
              .collect(Collectors.toList());
      assertThat(json(tempObj)).isEqualTo(json(tempObj));
    }
  }

  @Test
  @Ignore
  public void searchByPatientAndDateTwoDates() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    String tempObj = "Replace Me!";
    /*
     * The single date test verifies correct working of the different prefixes, like gt. So for two
     * date fields, we do not have to exhaustively test combinations.
     */
    Multimap<String, Procedure> procedureByPatient = populateData();
    // Procedure Dates for p0:
    // 2005-01-10T07:57:00Z
    // 2005-01-12T07:57:00Z
    // 2005-01-14T07:57:00Z
    // 2005-01-16T07:57:00Z
    // 2005-01-18T07:57:00Z
    Multimap<Pair<String, String>, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        Pair.of("gt2004", "lt2006"),
        List.of(
            "2005-01-10T07:57:00Z",
            "2005-01-12T07:57:00Z",
            "2005-01-14T07:57:00Z",
            "2005-01-16T07:57:00Z",
            "2005-01-18T07:57:00Z"));
    testDates.putAll(Pair.of("gt2005-01-13", "lt2005-01-15"), List.of("2005-01-14T07:57:00Z"));
    for (var date : testDates.keySet()) {
      List<Procedure> resources =
          procedureByPatient.get("p0").stream()
              .filter(p -> testDates.get(date).contains(p.performedDateTime()))
              .collect(Collectors.toList());
      assertThat(json(tempObj)).isEqualTo(json(tempObj));
    }
  }

  @Test
  @Ignore
  public void searchByPatientSuperman() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    String tempObj = "Replace Me!";
    // clark - has procedures
    // superman - no procedures
    DatamartProcedure dm =
        ProcedureSamples.Datamart.create()
            .procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    // Procedure.Bundle actual = controller().searchByPatientAndDate("superman", null, 1, 1);
    Procedure procedure =
        ProcedureSamples.Dstu2.create()
            .procedure("clrks-procedure", "superman", dm.performedDateTime().get().toString());
    assertThat(json(tempObj)).isEqualTo(json(tempObj));
  }

  @Test
  @Ignore
  public void searchByPatientWithCount0() {
    // TODO: Uncomment line(s) below and remove tempObj(s) once R4 Procedure exists
    String tempObj = "Replace Me!";
    Multimap<String, Procedure> procedureByPatient = populateData();
    assertThat(json(tempObj)).isEqualTo(json(tempObj));
  }

  @SneakyThrows
  private DatamartProcedure toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartProcedure.class);
  }

  @Test
  @Ignore
  public void validate() {
    // TODO: Uncomment line(s) below and create R4 Validator once R4 Procedure exists
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    // Procedure procedure =
    // ProcedureSamples.R4.create()
    // .procedure(
    // "1", dm.patient().reference().get(), dm.performedDateTime().get().toString());
    // assertThat(
    // controller()
    // .validate(
    // ProcedureSamples.R4.asBundle(
    // "http://fonzy.com/cool",
    // List.of(procedure),
    // 1,
    // link(
    // BundleLink.LinkRelation.first,
    // "http://fonzy.com/cool/Procedure?identifier=1",
    // 1,
    // 1),
    // link(
    // BundleLink.LinkRelation.self,
    // "http://fonzy.com/cool/Procedure?identifier=1",
    // 1,
    // 1),
    // link(
    // BundleLink.LinkRelation.last,
    // "http://fonzy.com/cool/Procedure?identifier=1",
    // 1,
    // 1))))
    // .isEqualTo(R4Validator.ok());
  }
}
