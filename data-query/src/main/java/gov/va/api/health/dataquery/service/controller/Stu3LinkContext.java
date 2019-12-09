package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.stu3.api.bundle.BundleLink;

final class Stu3LinkContext extends ConfigurableBaseUrlPageLinks.AbstractLinkContext {
  public Stu3LinkContext(String baseUrl, String basePath, PageLinks.LinkConfig config) {
    super(baseUrl, basePath, config);
  }

  BundleLink first() {
    return BundleLink.builder().relation(BundleLink.LinkRelation.first).url(toUrl(1)).build();
  }

  BundleLink last() {
    return BundleLink.builder()
        .relation(BundleLink.LinkRelation.last)
        .url(toUrl(lastPage()))
        .build();
  }

  BundleLink next() {
    return BundleLink.builder()
        .relation(BundleLink.LinkRelation.next)
        .url(toUrl(config().page() + 1))
        .build();
  }

  BundleLink previous() {
    return BundleLink.builder()
        .relation(BundleLink.LinkRelation.prev)
        .url(toUrl(config().page() - 1))
        .build();
  }

  BundleLink self() {
    return BundleLink.builder()
        .relation(BundleLink.LinkRelation.self)
        .url(toUrl(config().page()))
        .build();
  }
}
