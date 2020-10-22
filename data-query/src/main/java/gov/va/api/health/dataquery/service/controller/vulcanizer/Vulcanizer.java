package gov.va.api.health.dataquery.service.controller.vulcanizer;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class Vulcanizer {

  public static <E, D extends HasReplaceableId> DatamartTransformation<E, D> transformEntityUsing(
      Function<E, D> toDatamart) {
    return new DatamartTransformation<E, D>().toDatamart(toDatamart);
  }

  @RequiredArgsConstructor
  public static class BundleCreation<
          EntityT,
          DatamartT extends HasReplaceableId,
          ResourceT extends Resource,
          EntryT extends AbstractEntry<ResourceT>,
          BundleT extends AbstractBundle<EntryT>>
      implements Supplier<VulcanizedBundler<EntityT, DatamartT, ResourceT, EntryT, BundleT>> {

    private final ResourceTransformation<EntityT, DatamartT, ResourceT> resourceTransformation;

    @Setter private Supplier<BundleT> newBundle;

    @Setter private Supplier<EntryT> createEntriesUsing;

    @Setter private LinkProperties withLinkProperties;

    @Override
    public VulcanizedBundler<EntityT, DatamartT, ResourceT, EntryT, BundleT> get() {
      return VulcanizedBundler.<EntityT, DatamartT, ResourceT, EntryT, BundleT>builder()
          .witnessProtection(resourceTransformation.databaseTransformation.withWitnessProtection)
          .linkProperties(withLinkProperties)
          .newBundle(newBundle)
          .newEntry(createEntriesUsing)
          .toDatamart(resourceTransformation.databaseTransformation.toDatamart)
          .replaceReferences(resourceTransformation.databaseTransformation.replacingReferences)
          .toResource(resourceTransformation.toResource)
          .build();
    }
  }

  @Setter
  public static class DatamartTransformation<EntityT, DatamartT extends HasReplaceableId> {

    private WitnessProtection withWitnessProtection;

    private Function<EntityT, DatamartT> toDatamart;

    private Function<DatamartT, Stream<DatamartReference>> replacingReferences;

    public <ResourceT extends Resource>
        ResourceTransformation<EntityT, DatamartT, ResourceT> thenTransformToResourceUsing(
            Function<DatamartT, ResourceT> toResource) {
      return new ResourceTransformation<EntityT, DatamartT, ResourceT>(this).toResource(toResource);
    }
  }

  @RequiredArgsConstructor
  @Setter
  public static class ResourceTransformation<
      EntityT, DatamartT extends HasReplaceableId, ResourceT extends Resource> {

    private final DatamartTransformation<EntityT, DatamartT> databaseTransformation;

    @Setter private Function<DatamartT, ResourceT> toResource;

    public <EntryT extends AbstractEntry<ResourceT>, BundleT extends AbstractBundle<EntryT>>
        BundleCreation<EntityT, DatamartT, ResourceT, EntryT, BundleT> andBundleAs(
            Supplier<BundleT> newBundle) {
      return new BundleCreation<EntityT, DatamartT, ResourceT, EntryT, BundleT>(this)
          .newBundle(newBundle);
    }
  }
}
