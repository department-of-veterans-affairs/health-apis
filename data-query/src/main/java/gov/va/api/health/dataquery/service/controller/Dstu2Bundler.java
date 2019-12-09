package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.resources.Resource;
import java.util.List;
import java.util.function.Function;
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
public class Dstu2Bundler {
  private final PageLinks links;

  /** Return new bundle, filled with entries created by transforming the XML items. */
  public <X, T extends Resource, E extends AbstractEntry<T>, B extends AbstractBundle<E>> B bundle(
      BundleContext<X, T, E, B> context) {
    B bundle = context.newBundle().get();
    bundle.resourceType("Bundle");
    bundle.type(AbstractBundle.BundleType.searchset);
    bundle.total(context.linkConfig().totalRecords());
    bundle.link(links.dstu2Links(context.linkConfig()));
    bundle.entry(
        context
            .xmlItems()
            .stream()
            .map(context.transformer())
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
   * The context provides the two key types of information: 1) The XML types to be converted and
   * paging data, and 2) The machinery to create type specific bundles, entries, and converted
   * objects.
   *
   * @param <X> The CDW Xml item type. This should not be the root, but the resource type itself,
   *     e.g. CdwPatient103Root.CdwPatients.CdwPatient
   * @param <T> The Data Query type to produce, e.g. Patient
   * @param <E> The entry type, e.g. Patient.Entry
   * @param <B> The bundle type, e.g. Patient.Bundle
   */
  @Value
  public static final class BundleContext<
      X, T extends Resource, E extends AbstractEntry<T>, B extends AbstractBundle<E>> {
    private final PageLinks.LinkConfig linkConfig;

    private final List<X> xmlItems;

    /** Invoked for each item in the XML items list to convert it to the final published form. */
    private final Function<X, T> transformer;

    /** Used to create new instances for entries, one for each item in the XML items list. */
    private final Supplier<E> newEntry;

    /** Used to create a new instance of the bundle. Called once. */
    private final Supplier<B> newBundle;

    public static <T extends Resource, E extends AbstractEntry<T>, B extends AbstractBundle<E>>
        BundleContext<T, T, E, B> of(
            PageLinks.LinkConfig linkConfig,
            List<T> resources,
            Supplier<E> newEntry,
            Supplier<B> newBundle) {
      return of(linkConfig, resources, Function.identity(), newEntry, newBundle);
    }

    public static <X, T extends Resource, E extends AbstractEntry<T>, B extends AbstractBundle<E>>
        BundleContext<X, T, E, B> of(
            PageLinks.LinkConfig linkConfig,
            List<X> xmlItems,
            Function<X, T> transformer,
            Supplier<E> newEntry,
            Supplier<B> newBundle) {
      return new BundleContext<>(linkConfig, xmlItems, transformer, newEntry, newBundle);
    }
  }
}
