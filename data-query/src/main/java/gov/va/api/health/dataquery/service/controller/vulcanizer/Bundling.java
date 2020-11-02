package gov.va.api.health.dataquery.service.controller.vulcanizer;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Getter;

/** This structure represents the components needed to construct new bundles. */
@Builder
@Getter
public class Bundling<
    ResourceT extends Resource,
    EntryT extends AbstractEntry<ResourceT>,
    BundleT extends AbstractBundle<EntryT>> {

  /** How to create a new Bundle instance, typically a method reference to `new`. */
  private final Supplier<BundleT> newBundle;

  /** How to create a new Entry instance, typically a method reference to `new`. */
  private final Supplier<EntryT> newEntry;

  /** The properties that will be used to create links. */
  private final LinkProperties linkProperties;

  public static <
          ResourceT extends Resource,
          EntryT extends AbstractEntry<ResourceT>,
          BundleT extends AbstractBundle<EntryT>>
      BundlingBuilder<ResourceT, EntryT, BundleT> newBundle(Supplier<BundleT> newBundle) {
    return Bundling.<ResourceT, EntryT, BundleT>builder().newBundle(newBundle);
  }
}
