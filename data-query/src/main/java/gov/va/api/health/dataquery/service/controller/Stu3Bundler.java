package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.stu3.api.bundle.AbstractBundle;
import gov.va.api.health.stu3.api.bundle.AbstractEntry;
import gov.va.api.health.stu3.api.resources.Resource;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The bundler is capable of producing type specific bundles for resources. It leverages supporting
 * helper functions in a provided context to create new instances of specific bundle and entry
 * types. Paging links are supported via an injectable PageLinks.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Stu3Bundler {
  private final Stu3PageLinks links;

  /** Return new bundle, filled with entries created from the resources. */
  public <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>> B bundle(
      BundleContext<R, E, B> context) {
    B bundle = context.newBundle().get();
    bundle.resourceType("Bundle");
    bundle.type(AbstractBundle.BundleType.searchset);
    bundle.total(context.linkConfig().totalRecords());
    bundle.link(links.create(context.linkConfig()));
    bundle.entry(
        context
            .resources()
            .stream()
            .map(
                t -> {
                  E entry = context.newEntry().get();
                  entry.resource(t);
                  entry.fullUrl(links.readLink(context.linkConfig().path(), t.id()));
                  entry.search(
                      AbstractEntry.Search.builder().mode(AbstractEntry.SearchMode.match).build());
                  return entry;
                })
            .collect(Collectors.toList()));
    return bundle;
  }

  /**
   * The context provides the two key types of information: 1) The resources and paging data, and 2)
   * The machinery to create type specific bundles, entries, and converted objects.
   *
   * @param <R> The Data Query resource type
   * @param <E> The entry type, e.g. Patient.Entry
   * @param <B> The bundle type, e.g. Patient.Bundle
   */
  @Value
  public static final class BundleContext<
      R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>> {
    private final LinkConfig linkConfig;

    private final List<R> resources;

    /** Used to create new instances for entries, one for each resource. */
    private final Supplier<E> newEntry;

    /** Used to create a new instance of the bundle. Called once. */
    private final Supplier<B> newBundle;

    public static <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
        BundleContext<R, E, B> of(
            LinkConfig linkConfig, List<R> resources, Supplier<E> newEntry, Supplier<B> newBundle) {
      return new BundleContext<>(linkConfig, resources, newEntry, newBundle);
    }
  }
}
