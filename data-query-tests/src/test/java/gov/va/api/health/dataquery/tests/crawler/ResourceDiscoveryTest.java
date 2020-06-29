package gov.va.api.health.dataquery.tests.crawler;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.tests.crawler.ResourceDiscovery.UnknownFhirVersion;
import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import io.restassured.response.Response;
import java.util.List;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

public class ResourceDiscoveryTest {
  ResourceDiscovery rd =
      ResourceDiscovery.of(
          ResourceDiscovery.Context.builder()
              .url("http://localhost:8090/api/")
              .patientId("185601V825290")
              .build());

  public Dstu2TestData dstu2() {
    return Dstu2TestData.get();
  }

  @Test
  public void dstu2UselessConformanceStatementsReturnNoQueries() {
    Response response = mock(Response.class);
    when(response.as(Conformance.class)).thenReturn(dstu2().conformanceWithNoRestResourceList);
    assertThat(rd.queriesFor(response)).isEmpty();
    when(response.as(Conformance.class)).thenReturn(dstu2().conformanceWithEmptyRestResourceList);
    assertThat(rd.queriesFor(response)).isEmpty();
    when(response.as(Conformance.class)).thenReturn(dstu2().conformanceWithResources);
    assertThat(rd.queriesFor(response))
        .containsExactlyInAnyOrder(
            rd.context().url() + "Patient/" + rd.context().patientId(),
            rd.context().url() + "Patient?_id=" + rd.context().patientId(),
            rd.context().url() + "Thing?patient=" + rd.context().patientId());
  }

  public R4TestData r4() {
    return R4TestData.get();
  }

  @Test
  public void r4UselessConformanceStatementsReturnNoQueries() {
    Response response = mock(Response.class);
    when(response.as(CapabilityStatement.class))
        .thenReturn(r4().capabilityStatementWithNoRestResourceList);
    assertThat(rd.queriesFor(response)).isEmpty();
    when(response.as(CapabilityStatement.class))
        .thenReturn(r4().capabilityStatementWithEmptyRestResourceList);
    assertThat(rd.queriesFor(response)).isEmpty();
    when(response.as(CapabilityStatement.class)).thenReturn(r4().capabilityStatementWithResources);
    assertThat(rd.queriesFor(response))
        .containsExactlyInAnyOrder(
            rd.context().url() + "Patient/" + rd.context().patientId(),
            rd.context().url() + "Patient?_id=" + rd.context().patientId(),
            rd.context().url() + "Thing?patient=" + rd.context().patientId());
  }

  @Test
  public void resourceExtractsTypeFromWellFormedUrls() {
    assertThat(
            ResourceDiscovery.resource("https://dev-api.va.gov/services/argonaut/v0/Patient/12345"))
        .isEqualTo("Patient");
    assertThat(
            ResourceDiscovery.resource(
                "https://dev-api.va.gov/services/argonaut/v0/AllergyIntolerance?patient=12345"))
        .isEqualTo("AllergyIntolerance");
  }

  @Test
  public void resourceThrowsIllegalArgumentExceptionIfNoSlashIsFound() {
    assertThrows(IllegalArgumentException.class, () -> ResourceDiscovery.resource("garbage"));
  }

  @Test
  public void resourceUrlForPoorlyFormedUrls() {
    assertThat(
            ResourceDiscovery.resource(
                "https://dev-api.va.gov/AllergyIntolerance?patient/what=12345"))
        .isEqualTo("https://dev-api.va.gov/AllergyIntolerance?patient/what=12345");
    assertThat(ResourceDiscovery.resource("https://dev-api.va.gov/Patient//12345"))
        .isEqualTo("https://dev-api.va.gov/Patient//12345");
  }

  @Test
  public void unknownResponseThrowsException() {
    Response response = mock(Response.class);
    when(response.as(Conformance.class)).thenThrow(new RuntimeException("fugazi"));
    when(response.as(CapabilityStatement.class)).thenThrow(new RuntimeException("fugazi"));
    assertThrows(UnknownFhirVersion.class, () -> rd.queriesFor(response));
  }

  @NoArgsConstructor(staticName = "get")
  public static class Dstu2TestData {
    Conformance conformanceWithNoRestResourceList = Conformance.builder().build();

    Conformance conformanceWithEmptyRestResourceList =
        Conformance.builder().rest(List.of()).build();

    Conformance conformanceWithResources =
        Conformance.builder()
            .rest(
                List.of(
                    Conformance.Rest.builder()
                        .resource(
                            List.of(
                                resource("ThingNotSearchable"),
                                resource("ThingNotSearchableByPatient", "_id"),
                                resource("Thing", "_id", "patient"),
                                resource("Patient", "_id")))
                        .build()))
            .build();

    Conformance.RestResource resource(String type, String... searchParams) {
      List<Conformance.SearchParam> params =
          Stream.of(searchParams)
              .map(n -> Conformance.SearchParam.builder().name(n).build())
              .collect(toList());
      return Conformance.RestResource.builder()
          .type(type)
          .searchParam(params.isEmpty() ? null : params)
          .build();
    }
  }

  @NoArgsConstructor(staticName = "get")
  public static class R4TestData {
    CapabilityStatement capabilityStatementWithNoRestResourceList =
        CapabilityStatement.builder().build();

    CapabilityStatement capabilityStatementWithEmptyRestResourceList =
        CapabilityStatement.builder().rest(List.of()).build();

    CapabilityStatement capabilityStatementWithResources =
        CapabilityStatement.builder()
            .rest(
                List.of(
                    CapabilityStatement.Rest.builder()
                        .resource(
                            List.of(
                                resource("ThingNotSearchable"),
                                resource("ThingNotSearchableByPatient", "_id"),
                                resource("Thing", "_id", "patient"),
                                resource("Patient", "_id")))
                        .build()))
            .build();

    CapabilityStatement.CapabilityResource resource(String type, String... searchParams) {
      List<CapabilityStatement.SearchParam> params =
          Stream.of(searchParams)
              .map(n -> CapabilityStatement.SearchParam.builder().name(n).build())
              .collect(toList());
      return CapabilityStatement.CapabilityResource.builder()
          .type(type)
          .searchParam(params.isEmpty() ? null : params)
          .build();
    }
  }
}
