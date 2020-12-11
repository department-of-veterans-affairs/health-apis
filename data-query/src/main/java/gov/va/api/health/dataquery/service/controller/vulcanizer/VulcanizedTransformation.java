package gov.va.api.health.dataquery.service.controller.vulcanizer;

import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.lighthouse.datamart.DatamartReference;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;

/**
 * Transformation represents the flow a database entity to a FHIR representation. Entities are
 * transformed to Datamart objects, which are transformed into FHIR resources.
 */
@Getter
@Builder
public class VulcanizedTransformation<EntityT, DatamartT extends HasReplaceableId, ResourceT> {

  /** Transform a database entity into a datamart object instance. */
  private final Function<EntityT, DatamartT> toDatamart;

  /** Witness protection is used transform datamart references. */
  private final WitnessProtection witnessProtection;

  /** Provide a stream of Datamart references that require witness protection. */
  private final Function<DatamartT, Stream<DatamartReference>> replaceReferences;

  /** Transform a datamart object into a FHIR object. */
  private final Function<DatamartT, ResourceT> toResource;

  /** Create builder starting with this entity to datamart transformation. */
  public static <E, D extends HasReplaceableId> VulcanizedTransformationPart1<E, D> toDatamart(
      Function<E, D> toDatamart) {
    return VulcanizedTransformationPart1.<E, D>builder().toDatamart(toDatamart).build();
  }

  /** Updates references inline of the given record. Returns the same object as passed in. */
  public <T extends Collection<DatamartT>> T applyWitnessProtection(T datamartRecords) {
    witnessProtection().registerAndUpdateReferences(datamartRecords, replaceReferences());
    return datamartRecords;
  }

  /** Updates references inline of the given records. Returns the same collection as passed in. */
  public DatamartT applyWitnessProtection(DatamartT datamartRecord) {
    applyWitnessProtection(List.of(datamartRecord));
    return datamartRecord;
  }

  /**
   * These builder parts are used to slowly infer the generics types based on the arguments vs.
   * specifying the types and requires arguments that match.
   */
  @Builder
  public static class VulcanizedTransformationPart1<E, D extends HasReplaceableId> {
    private final Function<E, D> toDatamart;

    /** Configure the datamart to FHIR resource transformation. */
    public <R extends Resource> VulcanizedTransformationPart2<E, D, R> toResource(
        Function<D, R> toResource) {
      return VulcanizedTransformationPart2.<E, D, R>builder()
          .part1(this)
          .toResource(toResource)
          .build();
    }
  }

  /**
   * These builder parts are used to slowly infer the generics types based on the arguments vs.
   * specifying the types and requires arguments that match.
   */
  @Builder
  public static class VulcanizedTransformationPart2<E, D extends HasReplaceableId, R> {
    private final VulcanizedTransformationPart1<E, D> part1;
    private final Function<D, R> toResource;

    /** Configure witnessProtection. */
    public VulcanizedTransformationBuilder<E, D, R> witnessProtection(
        WitnessProtection witnessProtection) {
      return VulcanizedTransformation.<E, D, R>builder()
          .toDatamart(part1.toDatamart)
          .toResource(toResource)
          .witnessProtection(witnessProtection);
    }
  }
}
