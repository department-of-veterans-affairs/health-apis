package gov.va.api.health.dataquery.service.controller.vulcanizer;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
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
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.Builder;

@Builder
public class VulcanizedBundler<
        EntityT,
        DatamartT extends HasReplaceableId,
        ResourceT extends Resource,
        EntryT extends AbstractEntry<ResourceT>,
        BundleT extends AbstractBundle<EntryT>>
    implements Function<VulcanResult<EntityT>, BundleT> {

  WitnessProtection witnessProtection;

  Function<EntityT, DatamartT> toDatamart;

  Function<DatamartT, Stream<DatamartReference>> replaceReferences;

  LinkProperties linkProperties;

  Supplier<BundleT> newBundle;

  Supplier<EntryT> newEntry;

  Function<DatamartT, ResourceT> toResource;

  @Override
  public BundleT apply(VulcanResult<EntityT> result) {
    List<DatamartT> datamartRecords = result.entities().map(toDatamart).collect(toList());
    witnessProtection.registerAndUpdateReferences(datamartRecords, replaceReferences);
    List<EntryT> entries =
        datamartRecords.stream().map(toResource).map(this::toEntry).collect(toList());

    BundleT bundle = newBundle.get();
    bundle.resourceType("Bundle");
    bundle.type(AbstractBundle.BundleType.searchset);
    bundle.total((int) result.paging().totalRecords()); // TODO make totalRecords int
    bundle.link(toLinks(result.paging()));
    bundle.entry(entries);
    return bundle;
  }

  private EntryT toEntry(ResourceT resource) {
    EntryT entry = newEntry.get();
    entry.resource(resource);
    entry.fullUrl(linkProperties.r4().readUrl(resource));
    entry.search(AbstractEntry.Search.builder().mode(AbstractEntry.SearchMode.match).build());
    return entry;
  }

  private Function<String, BundleLink> toLink(BundleLink.LinkRelation relation) {
    return url -> BundleLink.builder().relation(relation).url(url).build();
  }

  private List<BundleLink> toLinks(Paging paging) {
    List<BundleLink> links = new ArrayList<>(5);
    paging.firstPageUrl().map(toLink(LinkRelation.first)).ifPresent(links::add);
    paging.previousPageUrl().map(toLink(LinkRelation.prev)).ifPresent(links::add);
    paging.thisPageUrl().map(toLink(LinkRelation.self)).ifPresent(links::add);
    paging.nextPageUrl().map(toLink(LinkRelation.next)).ifPresent(links::add);
    paging.lastPageUrl().map(toLink(LinkRelation.last)).ifPresent(links::add);
    return links.isEmpty() ? null : links;
  }
}
