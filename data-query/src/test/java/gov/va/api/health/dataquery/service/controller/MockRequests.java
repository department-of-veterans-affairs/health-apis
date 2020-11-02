package gov.va.api.health.dataquery.service.controller;

import gov.va.api.lighthouse.vulcan.VulcanResult.Paging;
import java.util.Optional;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

public class MockRequests {

  public static Paging paging(
      String urlFormat, int first, int prev, int current, int next, int last, int count) {
    return Paging.builder()
        .firstPage(Optional.of(first))
        .firstPageUrl(Optional.of(String.format(urlFormat, first, count)))
        .previousPage(Optional.of(prev))
        .previousPageUrl(Optional.of(String.format(urlFormat, prev, count)))
        .thisPage(Optional.of(current))
        .thisPageUrl(Optional.of(String.format(urlFormat, current, count)))
        .nextPage(Optional.of(next))
        .nextPageUrl(Optional.of(String.format(urlFormat, next, count)))
        .lastPage(Optional.of(last))
        .lastPageUrl(Optional.of(String.format(urlFormat, last, count)))
        .totalRecords(999)
        .totalPages(last)
        .build();
  }

  public static MockHttpServletRequest requestFromUri(String uri) {
    var u = UriComponentsBuilder.fromUriString(uri).build();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(u.getPath());
    request.setRemoteHost(u.getHost());
    request.setProtocol(u.getScheme());
    request.setServerPort(u.getPort());
    u.getQueryParams()
        .entrySet()
        .forEach(e -> request.addParameter(e.getKey(), e.getValue().toArray(new String[0])));
    return request;
  }
}
