package gov.va.api.health.dataquery.service.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import gov.va.api.health.dstu2.api.resources.Resource;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Builder
@Value
public class ExtractIcnValidator<R extends Resource> {
  ResponseBodyAdvice<Object> majig;

  R body;

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
