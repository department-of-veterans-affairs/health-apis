package gov.va.api.health.dataquery.service.controller.vulcanizer;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import gov.va.api.lighthouse.vulcan.VulcanResult.Paging;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;

/** Provides standard mapping from a VulcanResult to a FHIR bundle. */
@Builder
public class VulcanizedBundler<
        EntityT,
        DatamartT extends HasReplaceableId,
        ResourceT extends Resource,
        EntryT extends AbstractEntry<ResourceT>,
        BundleT extends AbstractBundle<EntryT>>
    implements Function<VulcanResult<EntityT>, BundleT> {

  /** The transformation process that will be applied to the results. */
  VulcanizedTransformation<EntityT, DatamartT, ResourceT> transformation;
  /** The bundling configuration that will be used to create the actual bundle. */
  Bundling<ResourceT, EntryT, BundleT> bundling;

  /** Create a new instance for the given bundling. */
  public static <EntityT, DatamartT extends HasReplaceableId, ResourceT extends Resource>
      VulcanizedBundlerPart1<EntityT, DatamartT, ResourceT> forTransformation(
          VulcanizedTransformation<EntityT, DatamartT, ResourceT> transformation) {
    return VulcanizedBundlerPart1.<EntityT, DatamartT, ResourceT>builder()
        .transformation(transformation)
        .build();
  }

  @Override
  public BundleT apply(VulcanResult<EntityT> result) {
    List<DatamartT> datamartRecords =
        result.entities().map(transformation.toDatamart()).collect(toList());
    transformation.applyWitnessProtection(datamartRecords);
    List<EntryT> entries =
        datamartRecords.stream()
            .map(transformation.toResource())
            .map(this::toEntry)
            .collect(toList());

    BundleT bundle = bundling.newBundle().get();
    bundle.resourceType("Bundle");
    bundle.type(AbstractBundle.BundleType.searchset);
    bundle.total((int) result.paging().totalRecords());
    bundle.link(toLinks(result.paging()));
    bundle.entry(entries);
    return bundle;
  }

  private EntryT toEntry(ResourceT resource) {
    EntryT entry = bundling.newEntry().get();
    entry.resource(resource);
    entry.fullUrl(bundling.linkProperties().r4().readUrl(resource));
    entry.search(AbstractEntry.Search.builder().mode(AbstractEntry.SearchMode.match).build());
    return entry;
  }

  private Function<String, BundleLink> toLink(BundleLink.LinkRelation relation) {
    return url -> BundleLink.builder().relation(relation).url(url).build();
  }

  List<BundleLink> toLinks(Paging paging) {
    List<BundleLink> links = new ArrayList<>(5);
    paging.firstPageUrl().map(toLink(LinkRelation.first)).ifPresent(links::add);
    paging.previousPageUrl().map(toLink(LinkRelation.prev)).ifPresent(links::add);
    paging.thisPageUrl().map(toLink(LinkRelation.self)).ifPresent(links::add);
    paging.nextPageUrl().map(toLink(LinkRelation.next)).ifPresent(links::add);
    paging.lastPageUrl().map(toLink(LinkRelation.last)).ifPresent(links::add);
    return links.isEmpty() ? null : links;
  }

  /**
   * These builder parts are used to slowly infer the generics types based on the arguments vs.
   * specifying the types and requires arguments that match.
   */
  @Builder
  public static class VulcanizedBundlerPart1<E, D extends HasReplaceableId, R extends Resource> {
    private final VulcanizedTransformation<E, D, R> transformation;

    /** Set the bundling. */
    public <N extends AbstractEntry<R>, B extends AbstractBundle<N>>
        VulcanizedBundlerBuilder<E, D, R, N, B> bundling(Bundling<R, N, B> bundling) {
      return VulcanizedBundler.<E, D, R, N, B>builder()
          .transformation(transformation)
          .bundling(bundling);
    }
  }
}
