package gov.va.api.health.dataquery.service.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Value
@Builder
public final class ExtractIcnValidator {
  ResponseBodyAdvice<Object> majig;

  Object body;

  List<String> expectedIcns;

  /** Assert that the ICNs from the Majig's extract function match the payload ICNs */
  @SuppressWarnings("unchecked")
  public void assertIcn() {
    ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);
    HttpHeaders mockHeaders = mock(HttpHeaders.class);
    Mockito.when(mockResponse.getHeaders()).thenReturn(mockHeaders);
    majig.beforeBodyWrite(body, null, null, null, null, mockResponse);
    verify(mockHeaders, Mockito.atLeastOnce()).get("X-VA-INCLUDES-ICN");
    verify(mockHeaders).add("X-VA-INCLUDES-ICN", String.join(",", expectedIcns));
    verifyNoMoreInteractions(mockHeaders);
  }
}
