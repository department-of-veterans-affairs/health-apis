package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.datatypes.Signature;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Meta;
import gov.va.api.health.dstu2.api.resources.Resource;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Delegate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

public class IncludesIcnMajigTest {
  @Test(expected = InvalidParameterException.class)
  public void beforeBodyWriteThrowsExceptionForUnsupportedType() {
    new FakeMajg().beforeBodyWrite(null, null, null, null, null, null);
  }

  @Test
  public void icnHeaderIsPresentForResource() {
    ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);
    HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockResponse.getHeaders()).thenReturn(mockHeaders);
    new FakeMajg()
        .beforeBodyWrite(
            FakeResource.builder().id("666V666").build(), null, null, null, null, mockResponse);
    verify(mockHeaders, Mockito.atLeastOnce()).get("X-VA-INCLUDES-ICN");
    verify(mockHeaders).add("X-VA-INCLUDES-ICN", "666V666");
    verifyNoMoreInteractions(mockHeaders);
  }

  @Test
  public void icnHeadersAreDistictValuesForResourceBundle() {
    ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);
    HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockResponse.getHeaders()).thenReturn(mockHeaders);
    var payload =
        FakeBundle.builder()
            .entry(
                List.of(
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("666V666").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("777V777").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("888V888").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("666V666").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("777V777").build())
                        .build()))
            .build();
    new FakeMajg().beforeBodyWrite(payload, null, null, null, null, mockResponse);
    verify(mockHeaders, Mockito.atLeastOnce()).get("X-VA-INCLUDES-ICN");
    verify(mockHeaders).add("X-VA-INCLUDES-ICN", "666V666,777V777,888V888");
    verifyNoMoreInteractions(mockHeaders);
  }

  @Test
  public void icnHeadersArePresentForResourceBundle() {
    ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);
    HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockResponse.getHeaders()).thenReturn(mockHeaders);
    var payload =
        FakeBundle.builder()
            .entry(
                List.of(
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("666V666").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("777V777").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("888V888").build())
                        .build()))
            .build();
    new FakeMajg().beforeBodyWrite(payload, null, null, null, null, mockResponse);
    verify(mockHeaders, Mockito.atLeastOnce()).get("X-VA-INCLUDES-ICN");
    verify(mockHeaders).add("X-VA-INCLUDES-ICN", "666V666,777V777,888V888");
    verifyNoMoreInteractions(mockHeaders);
  }

  @Test
  public void supportedAcceptsResourceOrResourceBundle() {
    MethodParameter supportedResource = mock(MethodParameter.class);
    doReturn(FakeResource.class).when(supportedResource).getParameterType();
    assertThat(new FakeMajg().supports(supportedResource, null)).isTrue();
    MethodParameter supportedResourceBundle = mock(MethodParameter.class);
    doReturn(FakeBundle.class).when(supportedResourceBundle).getParameterType();
    assertThat(new FakeMajg().supports(supportedResourceBundle, null)).isTrue();
    MethodParameter unsupportedResource = mock(MethodParameter.class);
    doReturn(String.class).when(unsupportedResource).getParameterType();
    assertThat(new FakeMajg().supports(unsupportedResource, null)).isFalse();
  }

  /**
   * Silly Test implementation of the AbstractIncludesIcnMajig.java Because we are using Templates,
   * we also need a a fake Resource, Entry, and Bundle class
   */
  public static final class FakeMajg implements ResponseBodyAdvice<Object> {
    @Delegate
    private final ResponseBodyAdvice<Object> delegate =
        IncludesIcnMajig.<FakeResource, FakeBundle>builder()
            .type(FakeResource.class)
            .bundleType(FakeBundle.class)
            .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
            .extractIcns(body -> Stream.of(body.id))
            .build();
  }

  @Builder
  @Data
  static final class FakeResource implements Resource {
    String id;

    String implicitRules;

    String language;

    Meta meta;
  }

  static final class FakeEntry extends AbstractEntry<FakeResource> {
    @Builder
    FakeEntry(
        String id,
        List<Extension> extension,
        List<Extension> modifierExtension,
        List<BundleLink> link,
        String fullUrl,
        FakeResource resource,
        Search search,
        Request request,
        Response response) {
      super(id, extension, modifierExtension, link, fullUrl, resource, search, request, response);
    }
  }

  static final class FakeBundle extends AbstractBundle<FakeEntry> {
    @Builder
    FakeBundle(
        String resourceType,
        String id,
        Meta meta,
        String implicitRules,
        String language,
        BundleType type,
        Integer total,
        List<BundleLink> link,
        List<FakeEntry> entry,
        Signature signature) {
      super(resourceType, id, meta, implicitRules, language, type, total, link, entry, signature);
    }
  }
}
