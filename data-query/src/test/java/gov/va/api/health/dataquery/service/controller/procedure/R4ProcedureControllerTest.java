package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.health.dataquery.service.controller.procedure.ProcedureSamples.R4.link;
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
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureSamples.R4;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.uscorer4.api.resources.Procedure;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class R4ProcedureControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private ProcedureRepository repository;

  @SneakyThrows
  private ProcedureEntity asEntity(DatamartProcedure dm) {
    return ProcedureEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .performedOnEpochTime(dm.performedDateTime().get().toEpochMilli())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  R4ProcedureController controller() {
    return new R4ProcedureController(
        ProcedureHack.builder()
            .withRecordsId("clark")
            .withoutRecordsId("superman")
            .withRecordsDisplay("Clark Kent")
            .withoutRecordsDisplay("Superman")
            .build(),
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://abed.com", "cool", "cool", "cool")),
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
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  private Multimap<String, gov.va.api.health.uscorer4.api.resources.Procedure> populateData() {
    var fhir = ProcedureSamples.R4.create();
    var datamart = ProcedureSamples.Datamart.create();
    var procedureByPatient =
        LinkedHashMultimap.<String, gov.va.api.health.uscorer4.api.resources.Procedure>create();
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
  public void read() {
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    repository.save(asEntity(dm));
    mockProcedureIdentity("1", dm.cdwId());
    Procedure actual = controller().read("1", "1");
    assertThat(json(actual))
        .isEqualTo(
            json(
                ProcedureSamples.R4
                    .create()
                    .procedure(
                        "1",
                        dm.patient().reference().get(),
                        dm.performedDateTime().get().toString())));
  }

  @Test
  public void readRaw() {
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    ProcedureEntity entity = asEntity(dm);
    repository.save(entity);
    mockProcedureIdentity("1", dm.cdwId());
    String json = controller().readRaw("1", "1", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test
  public void readRawSuperman() {
    // clark - has procedures
    // superman - no procedures
    DatamartProcedure dm =
        ProcedureSamples.Datamart.create()
            .procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    String json = controller().readRaw("clrks-procedure", "superman", response);
    assertThat(toObject(json)).isEqualTo(dm);
  }

  @Test
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockProcedureIdentity("1", "1");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("1", "1", response));
  }

  @Test
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("1", "1", response));
  }

  @Test
  public void readSuperman() {
    // clark - has procedures
    // superman - no procedures
    DatamartProcedure dm =
        ProcedureSamples.Datamart.create()
            .procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    Procedure actual = controller().read("clrks-procedure", "superman");
    assertThat(json(actual))
        .isEqualTo(
            json(
                R4.create()
                    .procedure(
                        "clrks-procedure", "superman", dm.performedDateTime().get().toString())));
  }

  @Test
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockProcedureIdentity("1", "1");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("1", "1"));
  }

  @Test
  public void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("1", "1"));
  }

  @Test
  public void searchById() {
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    repository.save(asEntity(dm));
    mockProcedureIdentity("1", dm.cdwId());
    Procedure.Bundle actual = controller().searchById("1", "1", 1, 1);
    Procedure procedure =
        R4.create()
            .procedure(
                "1", dm.patient().reference().get(), dm.performedDateTime().get().toString());
    assertThat(json(actual))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://abed.com/cool",
                    List.of(procedure),
                    1,
                    link(LinkRelation.first, "http://abed.com/cool/Procedure?identifier=1", 1, 1),
                    link(LinkRelation.self, "http://abed.com/cool/Procedure?identifier=1", 1, 1),
                    link(LinkRelation.last, "http://abed.com/cool/Procedure?identifier=1", 1, 1))));
  }

  @Test
  public void searchByIdSuperman() {
    // clark - has procedures
    // superman - no procedures
    DatamartProcedure dm =
        ProcedureSamples.Datamart.create()
            .procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    Procedure.Bundle actual = controller().searchById("superman", "clrks-procedure", 1, 1);
    Procedure procedure =
        R4.create()
            .procedure("clrks-procedure", "superman", dm.performedDateTime().get().toString());
    assertThat(json(actual))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://abed.com/cool",
                    List.of(procedure),
                    1,
                    link(
                        LinkRelation.first,
                        "http://abed.com/cool/Procedure?identifier=clrks-procedure",
                        1,
                        1),
                    link(
                        LinkRelation.self,
                        "http://abed.com/cool/Procedure?identifier=clrks-procedure",
                        1,
                        1),
                    link(
                        LinkRelation.last,
                        "http://abed.com/cool/Procedure?identifier=clrks-procedure",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    repository.save(asEntity(dm));
    mockProcedureIdentity("1", dm.cdwId());
    Procedure.Bundle actual = controller().searchByIdentifier("1", "1", 1, 1);
    Procedure procedure =
        R4.create()
            .procedure(
                "1", dm.patient().reference().get(), dm.performedDateTime().get().toString());
    assertThat(json(actual))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://abed.com/cool",
                    List.of(procedure),
                    1,
                    link(LinkRelation.first, "http://abed.com/cool/Procedure?identifier=1", 1, 1),
                    link(LinkRelation.self, "http://abed.com/cool/Procedure?identifier=1", 1, 1),
                    link(LinkRelation.last, "http://abed.com/cool/Procedure?identifier=1", 1, 1))));
  }

  @Test
  public void searchByPatientAndDateNoDates() {
    Multimap<String, Procedure> procedureByPatient = populateData();
    assertThat(json(controller().searchByPatientAndDate("p0", null, 1, 10)))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://abed.com/cool",
                    procedureByPatient.get("p0"),
                    procedureByPatient.get("p0").size(),
                    link(LinkRelation.first, "http://abed.com/cool/Procedure?patient=p0", 1, 10),
                    link(LinkRelation.self, "http://abed.com/cool/Procedure?patient=p0", 1, 10),
                    link(LinkRelation.last, "http://abed.com/cool/Procedure?patient=p0", 1, 10))));
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
      List<Procedure> resources =
          procedureByPatient.get("p0").stream()
              .filter(p -> testDates.get(date).contains(p.performedDateTime()))
              .collect(Collectors.toList());
      assertThat(json(controller().searchByPatientAndDate("p0", new String[] {date}, 1, 10)))
          .isEqualTo(
              json(
                  R4.asBundle(
                      "http://abed.com/cool",
                      resources,
                      resources.size(),
                      link(
                          LinkRelation.first,
                          "http://abed.com/cool/Procedure?date=" + date + "&patient=p0",
                          1,
                          10),
                      link(
                          LinkRelation.self,
                          "http://abed.com/cool/Procedure?date=" + date + "&patient=p0",
                          1,
                          10),
                      link(
                          LinkRelation.last,
                          "http://abed.com/cool/Procedure?date=" + date + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  public void searchByPatientAndDateTwoDates() {
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
      assertThat(
              json(
                  controller()
                      .searchByPatientAndDate(
                          "p0", new String[] {date.getLeft(), date.getRight()}, 1, 10)))
          .isEqualTo(
              json(
                  R4.asBundle(
                      "http://abed.com/cool",
                      resources,
                      resources.size(),
                      link(
                          LinkRelation.first,
                          "http://abed.com/cool/Procedure?date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          LinkRelation.self,
                          "http://abed.com/cool/Procedure?date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          LinkRelation.last,
                          "http://abed.com/cool/Procedure?date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  public void searchByPatientSuperman() {
    // clark - has procedures
    // superman - no procedures
    DatamartProcedure dm =
        ProcedureSamples.Datamart.create()
            .procedure("clrks-cdw-procedure", "clark", "2005-01-21T07:57:00Z");
    repository.save(asEntity(dm));
    mockProcedureIdentity("clrks-procedure", "clrks-cdw-procedure");
    Procedure.Bundle actual = controller().searchByPatientAndDate("superman", null, 1, 1);
    Procedure procedure =
        R4.create()
            .procedure("clrks-procedure", "superman", dm.performedDateTime().get().toString());
    assertThat(json(actual))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://abed.com/cool",
                    List.of(procedure),
                    1,
                    link(
                        LinkRelation.first,
                        "http://abed.com/cool/Procedure?patient=superman",
                        1,
                        1),
                    link(
                        LinkRelation.self, "http://abed.com/cool/Procedure?patient=superman", 1, 1),
                    link(
                        LinkRelation.last,
                        "http://abed.com/cool/Procedure?patient=superman",
                        1,
                        1))));
  }

  @Test
  public void searchByPatientWithCount0() {
    Multimap<String, Procedure> procedureByPatient = populateData();
    assertThat(json(controller().searchByPatientAndDate("p0", null, 1, 0)))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://abed.com/cool",
                    Collections.emptyList(),
                    procedureByPatient.get("p0").size(),
                    link(LinkRelation.self, "http://abed.com/cool/Procedure?patient=p0", 1, 0))));
  }

  @SneakyThrows
  private DatamartProcedure toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartProcedure.class);
  }
}
