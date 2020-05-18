package gov.va.api.health.dataquery.tests.crawler;

import gov.va.api.health.fhir.api.FhirVersion;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;

/**
 * Process an object and extract URLs that can be followed while crawling. In general, this useful
 * for processing bundle classes.
 */
public interface UrlExtractor {
  /** Return a UrlExtractor that is suited for processing bundles of the given FHIR version. */
  static UrlExtractor forFhirVersion(@NonNull FhirVersion fhirVersion) {
    switch (fhirVersion) {
      case DSTU2:
        return new Dstu2BundleUrlExtractor();
      case R4:
        return new R4BundleUrlExtractor();
      default:
        throw new IllegalArgumentException("Do not understand FHIR version:" + fhirVersion);
    }
  }

  Stream<String> urlsOf(Object payload);

  class Dstu2BundleUrlExtractor implements UrlExtractor {
    @Override
    public Stream<String> urlsOf(Object payload) {
      if (!(payload instanceof gov.va.api.health.dstu2.api.bundle.AbstractBundle<?>)) {
        return Stream.empty();
      }
      var bundle = (gov.va.api.health.dstu2.api.bundle.AbstractBundle<?>) payload;
      Optional<gov.va.api.health.dstu2.api.bundle.BundleLink> next =
          bundle.link().stream()
              .filter(
                  l ->
                      l.relation()
                          == gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation.next)
              .findFirst();
      return Stream.concat(
          next.stream().map(gov.va.api.health.dstu2.api.bundle.BundleLink::url),
          bundle.entry().stream().map(gov.va.api.health.dstu2.api.bundle.AbstractEntry::fullUrl));
    }
  }

  class R4BundleUrlExtractor implements UrlExtractor {
    @Override
    public Stream<String> urlsOf(Object payload) {
      if (!(payload instanceof gov.va.api.health.r4.api.bundle.AbstractBundle<?>)) {
        return Stream.empty();
      }
      var bundle = (gov.va.api.health.r4.api.bundle.AbstractBundle<?>) payload;
      Optional<gov.va.api.health.r4.api.bundle.BundleLink> next =
          bundle.link().stream()
              .filter(
                  l -> l.relation() == gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation.next)
              .findFirst();
      return Stream.concat(
          next.stream().map(gov.va.api.health.r4.api.bundle.BundleLink::url),
          bundle.entry().stream().map(gov.va.api.health.r4.api.bundle.AbstractEntry::fullUrl));
    }
  }
}
