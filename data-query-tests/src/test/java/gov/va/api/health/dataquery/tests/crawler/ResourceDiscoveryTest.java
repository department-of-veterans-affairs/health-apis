package gov.va.api.health.dataquery.tests.crawler;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.tests.crawler.ResourceDiscovery.UnknownFhirVersion;
import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.dstu2.api.resources.Conformance.Rest;
import gov.va.api.health.dstu2.api.resources.Conformance.RestResource;
import gov.va.api.health.dstu2.api.resources.Conformance.SearchParam;
import gov.va.api.health.sentinel.categories.Local;
import io.restassured.response.Response;
import java.util.List;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Local.class)
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
            rd.context().url() + "Thing?patient=" + rd.context().patientId()
            //
            );
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

  @Test(expected = IllegalArgumentException.class)
  public void resourceThrowsIllegalArgumentExceptionIfNoSlashIsFound() {
    ResourceDiscovery.resource("garbage");
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

  @Test(expected = UnknownFhirVersion.class)
  public void unknownResponseThrowsException() {
    Response response = mock(Response.class);
    when(response.as(Conformance.class)).thenThrow(new RuntimeException("fugazi"));
    rd.queriesFor(response);
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
                    Rest.builder()
                        .resource(
                            List.of(
                                resource("ThingNotSearchable"),
                                resource("ThingNotSearchableByPatient", "_id"),
                                resource("Thing", "_id", "patient"),
                                resource("Patient", "_id")
                                //
                                ))
                        .build()))
            .build();

    RestResource resource(String type, String... searchParams) {
      List<SearchParam> params =
          Stream.of(searchParams).map(n -> SearchParam.builder().name(n).build()).collect(toList());
      return RestResource.builder()
          .type(type)
          .searchParam(params.isEmpty() ? null : params)
          .build();
    }
  }
}
