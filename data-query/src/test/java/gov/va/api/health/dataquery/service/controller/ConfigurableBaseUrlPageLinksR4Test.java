package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.bundle.BundleLink;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ConfigurableBaseUrlPageLinksR4Test {
  private static ConfigurableBaseUrlPageLinks links;

  @BeforeAll
  public static void _init() {
    links = new ConfigurableBaseUrlPageLinks("https://awesome.com", "unused", "api/stu3", "r4");
  }

  @Test
  public void allLinksPresentWhenInTheMiddle() {
    List<BundleLink> actual = links.r4Links(forCurrentPage(2, 3, 15));
    assertThat(actual)
        .containsExactlyInAnyOrder(
            link(BundleLink.LinkRelation.first, 1, 3),
            link(BundleLink.LinkRelation.prev, 1, 3),
            link(BundleLink.LinkRelation.self, 2, 3),
            link(BundleLink.LinkRelation.next, 3, 3),
            link(BundleLink.LinkRelation.last, 5, 3));
  }

  private PageLinks.LinkConfig forCurrentPage(int page, int count, int total) {
    return PageLinks.LinkConfig.builder()
        .path("Whatever")
        .queryParams(
            Parameters.builder()
                .add("a", "apple")
                .add("b", "banana")
                .add("page", String.valueOf(page))
                .add("_count", String.valueOf(count))
                .build())
        .page(page)
        .recordsPerPage(count)
        .totalRecords(total)
        .build();
  }

  private BundleLink link(BundleLink.LinkRelation relation, int page, int count) {
    return BundleLink.builder()
        .relation(relation)
        .url("https://awesome.com/r4/Whatever?a=apple&b=banana&page=" + page + "&_count=" + count)
        .build();
  }

  @Test
  public void nextAndPreviousLinksOmittedWhenOnlyOnePage() {
    List<BundleLink> actual = links.r4Links(forCurrentPage(1, 2, 2));
    assertThat(actual)
        .containsExactlyInAnyOrder(
            link(BundleLink.LinkRelation.first, 1, 2),
            link(BundleLink.LinkRelation.self, 1, 2),
            link(BundleLink.LinkRelation.last, 1, 2));
  }

  @Test
  public void nextAndPreviousLinksOmittedWhenRequestedPageIsNotKnown() {
    List<BundleLink> actual = links.r4Links(forCurrentPage(99, 3, 15));
    assertThat(actual)
        .containsExactlyInAnyOrder(
            link(BundleLink.LinkRelation.first, 1, 3),
            link(BundleLink.LinkRelation.self, 99, 3),
            link(BundleLink.LinkRelation.last, 5, 3));
  }

  @Test
  public void nextLinkOmittedWhenOnLastPage() {
    List<BundleLink> actual = links.r4Links(forCurrentPage(5, 3, 15));
    assertThat(actual)
        .containsExactlyInAnyOrder(
            link(BundleLink.LinkRelation.first, 1, 3),
            link(BundleLink.LinkRelation.prev, 4, 3),
            link(BundleLink.LinkRelation.self, 5, 3),
            link(BundleLink.LinkRelation.last, 5, 3));
  }

  @Test
  public void onlySelfLinkWhenCountIsZero() {
    List<BundleLink> actual = links.r4Links(forCurrentPage(5, 0, 15));
    assertThat(actual).containsExactlyInAnyOrder(link(BundleLink.LinkRelation.self, 5, 0));
  }

  @Test
  public void previousLinkOmittedWhenOnFirstPage() {
    List<BundleLink> actual = links.r4Links(forCurrentPage(1, 3, 15));
    assertThat(actual)
        .containsExactlyInAnyOrder(
            link(BundleLink.LinkRelation.first, 1, 3),
            link(BundleLink.LinkRelation.self, 1, 3),
            link(BundleLink.LinkRelation.next, 2, 3),
            link(BundleLink.LinkRelation.last, 5, 3));
  }

  @Test
  public void readLinkCombinesConfiguredUrl() {
    assertThat(links.r4ReadLink("Whatever", "123"))
        .isEqualTo("https://awesome.com/r4/Whatever/123");
  }
}
