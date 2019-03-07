package gov.va.api.health.dataquery.service.mranderson.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient.BadRequest;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient.NotFound;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient.SearchFailed;
import gov.va.api.health.dataquery.service.mranderson.client.Query.Profile;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class RestMrAndersonClientTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Mock RestTemplate rt;

  private RestMrAndersonClient client;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    client = new RestMrAndersonClient("https://example.com", rt);
  }

  @Test
  public void badRequestIsThrownFor400Status() {
    thrown.expect(BadRequest.class);
    mockErrorResponse(
        HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "x", null, null, null));
    client.search(query());
  }

  @SuppressWarnings("unchecked")
  private void mockErrorResponse(Exception throwMe) {
    when(rt.exchange(
            Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(HttpEntity.class),
            Mockito.any(ParameterizedTypeReference.class)))
        .thenThrow(throwMe);
  }

  @Test
  public void notFoundIsThrownFor404Status() {
    thrown.expect(NotFound.class);
    mockErrorResponse(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "x", null, null, null));
    client.search(query());
  }

  private Query<CdwPatient103Root> query() {
    return Query.forType(CdwPatient103Root.class)
        .resource("Patient")
        .profile(Profile.ARGONAUT)
        .version("123")
        .parameters(Parameters.forIdentity("456"))
        .build();
  }

  @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
  @Test
  public void responseBodyIsReturnedFor200Status() {
    CdwPatient103Root root = new CdwPatient103Root();
    ResponseEntity<CdwPatient103Root> response = mock(ResponseEntity.class);
    when(response.getStatusCode()).thenReturn(HttpStatus.OK);
    when(response.getBody()).thenReturn(root);
    when(rt.exchange(
            Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(HttpEntity.class),
            Mockito.any(ParameterizedTypeReference.class)))
        .thenReturn(response);
    CdwPatient103Root actual = client.search(query());
    assertThat(actual).isSameAs(root);
    query().hashCode();
    query().equals(query());
  }

  @Test
  public void searchFailedIsThrownForNonOkStatus() {
    thrown.expect(SearchFailed.class);
    mockErrorResponse(
        HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "x", null, null, null));

    client.search(query());
  }
}
