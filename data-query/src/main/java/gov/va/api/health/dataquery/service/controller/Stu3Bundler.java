package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.stu3.api.bundle.AbstractBundle;
import gov.va.api.health.stu3.api.bundle.AbstractEntry;
import gov.va.api.health.stu3.api.resources.Resource;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Stu3Bundler {
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
    bundle.link(links.stu3Links(linkConfig));
    bundle.entry(
        resources
            .stream()
            .map(
                t -> {
                  E entry = newEntry.get();
                  entry.resource(t);
                  entry.fullUrl(links.readLink(linkConfig.path(), t.id()));
                  entry.search(
                      AbstractEntry.Search.builder().mode(AbstractEntry.SearchMode.match).build());
                  return entry;
                })
            .collect(Collectors.toList()));
    return bundle;
  }
}
