package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The bundler is capable of producing type specific bundles for resources. It leverages supporting
 * helper functions in a provided context to create new instances of specific bundle and entry
 * types. Paging links are supported via an injectable PageLinks.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class R4Bundler {
  private final PageLinks links;

  /**
   * Return new bundle, filled with entries created from the resources.
   *
   * @param linkConfig link and paging data
   * @param resources The FHIR resources to compose the bundle
   * @param newEntry Used to create new instances for entries, one for each resource
   * @param newBundle Used to create a new instance of the bundle (called once)
   */
  public <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>> B bundle(
      PageLinks.LinkConfig linkConfig,
      List<R> resources,
      Supplier<E> newEntry,
      Supplier<B> newBundle) {
    B bundle = newBundle.get();
    bundle.resourceType("Bundle");
    bundle.type(AbstractBundle.BundleType.searchset);
    bundle.total(linkConfig.totalRecords());
    bundle.link(links.r4Links(linkConfig));
    bundle.entry(
        resources.stream()
            .map(
                t -> {
                  E entry = newEntry.get();
                  entry.resource(t);
                  entry.fullUrl(links.r4ReadLink(linkConfig.path(), t.id()));
                  entry.search(
                      AbstractEntry.Search.builder().mode(AbstractEntry.SearchMode.match).build());
                  return entry;
                })
            .collect(Collectors.toList()));
    return bundle;
  }
}
