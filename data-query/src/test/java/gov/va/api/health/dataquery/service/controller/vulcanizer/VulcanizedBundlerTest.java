package gov.va.api.health.dataquery.service.controller.vulcanizer;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.FooBundle;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.FooDatamart;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.FooEntity;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.FooEntry;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.FooResource;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Foos.Ids;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.r4.api.bundle.AbstractEntry.Search;
import gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import gov.va.api.lighthouse.vulcan.VulcanResult.Paging;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VulcanizedBundlerTest {
  @Mock IdentityService ids;

  @Test
  void apply() {

    when(ids.register(any()))
        .thenReturn(
            List.of(
                Ids.registration("WHATEVER", "p1", "pp1"),
                Ids.registration("FOO", "f1", "pf1"),
                Ids.registration("FOO", "f2", "pf2"),
                Ids.registration("FOO", "f3", "pf3")));

    VulcanResult<FooEntity> result =
        VulcanResult.<FooEntity>builder()
            .paging(paging("http://foo.com/4/Foo?patient=p1&page=%d&_count=%d", 1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    new FooEntity("f1", "p1"),
                    new FooEntity("f2", "p1"),
                    new FooEntity("f3", "p1")))
            .build();

    var bundle = bundler().apply(result);

    var expected = new FooBundle();
    expected.resourceType("Bundle");
    expected.type(BundleType.searchset);
    expected.total(999);
    expected.link(
        List.of(
            expectedLink(LinkRelation.first, "http://foo.com/4/Foo?patient=p1&page=1&_count=15"),
            expectedLink(LinkRelation.prev, "http://foo.com/4/Foo?patient=p1&page=4&_count=15"),
            expectedLink(LinkRelation.self, "http://foo.com/4/Foo?patient=p1&page=5&_count=15"),
            expectedLink(LinkRelation.next, "http://foo.com/4/Foo?patient=p1&page=6&_count=15"),
            expectedLink(LinkRelation.last, "http://foo.com/4/Foo?patient=p1&page=9&_count=15")));
    expected.entry(List.of(expectedEntry("pf1"), expectedEntry("pf2"), expectedEntry("pf3")));

    // easier to debug if we check parts before the whole.
    assertThat(bundle.link()).isEqualTo(expected.link());
    assertThat(bundle.entry().get(0)).isEqualTo(expected.entry().get(0));
    assertThat(bundle).isEqualTo(expected);
  }

  VulcanizedBundler<FooEntity, FooDatamart, FooResource, FooEntry, FooBundle> bundler() {
    return VulcanizedBundler.forTransformation(
            VulcanizedTransformation.toDatamart(FooEntity::toDatamart)
                .toResource(FooDatamart::toResource)
                .witnessProtection(WitnessProtection.builder().identityService(ids).build())
                .replaceReferences(d -> Stream.of(d.patient()))
                .build())
        .bundling(
            Bundling.newBundle(FooBundle::new)
                .newEntry(FooEntry::new)
                .linkProperties(
                    LinkProperties.builder()
                        .publicUrl("http://foo.com")
                        .publicDstu2BasePath("2")
                        .publicStu3BasePath("3")
                        .publicR4BasePath("4")
                        .defaultPageSize(20)
                        .maxPageSize(500)
                        .build())
                .build())
        .build();
  }

  FooEntry expectedEntry(final String publicId) {
    var e = new FooEntry();
    e.fullUrl("http://foo.com/4/FooResource/" + publicId);
    e.search(Search.builder().mode(SearchMode.match).build());
    e.resource(FooResource.builder().id(publicId).ref("pp1").build());
    return e;
  }

  private BundleLink expectedLink(LinkRelation rel, String url) {
    return BundleLink.builder().relation(rel).url(url).build();
  }

  private Paging linksFor(int first, int prev, int current, int next, int last) {
    var b = Paging.builder();
    if (first > 0) {
      b.firstPage(Optional.of(first)).firstPageUrl(Optional.of("url/" + first));
    } else {
      b.firstPage(Optional.empty()).firstPageUrl(Optional.empty());
    }
    if (prev > 0) {
      b.previousPage(Optional.of(prev)).previousPageUrl(Optional.of("url/" + prev));
    } else {
      b.previousPage(Optional.empty()).previousPageUrl(Optional.empty());
    }
    if (current > 0) {
      b.thisPage(Optional.of(current)).thisPageUrl(Optional.of("url/" + current));
    } else {
      b.thisPage(Optional.empty()).thisPageUrl(Optional.empty());
    }
    if (next > 0) {
      b.nextPage(Optional.of(next)).nextPageUrl(Optional.of("url/" + next));
    } else {
      b.nextPage(Optional.empty()).nextPageUrl(Optional.empty());
    }
    if (last > 0) {
      b.lastPage(Optional.of(last)).lastPageUrl(Optional.of("url/" + last));
    } else {
      b.lastPage(Optional.empty()).lastPageUrl(Optional.empty());
    }
    var paging = b.totalRecords(999).totalPages(99).build();
    return paging;
  }

  @Test
  void toLinks() {
    assertThat(bundler().toLinks(linksFor(1, 2, 3, 4, 5)))
        .containsExactly(
            expectedLink(LinkRelation.first, "url/1"),
            expectedLink(LinkRelation.prev, "url/2"),
            expectedLink(LinkRelation.self, "url/3"),
            expectedLink(LinkRelation.next, "url/4"),
            expectedLink(LinkRelation.last, "url/5"));
    assertThat(bundler().toLinks(linksFor(1, 0, 3, 0, 5)))
        .containsExactly(
            expectedLink(LinkRelation.first, "url/1"),
            expectedLink(LinkRelation.self, "url/3"),
            expectedLink(LinkRelation.last, "url/5"));
    assertThat(bundler().toLinks(linksFor(0, 0, 1, 0, 0)))
        .containsExactly(expectedLink(LinkRelation.self, "url/1"));
    assertThat(bundler().toLinks(linksFor(1, 0, 0, 0, 0)))
        .containsExactly(expectedLink(LinkRelation.first, "url/1"));
  }
}
