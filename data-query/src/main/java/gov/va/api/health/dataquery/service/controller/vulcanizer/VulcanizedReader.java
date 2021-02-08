package gov.va.api.health.dataquery.service.controller.vulcanizer;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import org.springframework.data.repository.CrudRepository;

/** Provides standard read and read raw capability for resources. */
@Builder
public class VulcanizedReader<
    EntityT, DatamartT extends HasReplaceableId, ResourceT extends Resource, PkT> {

  /** The transformation process that will be applied to the results. */
  VulcanizedTransformation<EntityT, DatamartT, ResourceT> transformation;
  /** The repository for the resource. */
  CrudRepository<EntityT, PkT> repository;
  /**
   * This function will be applied to the resource. If an ID is returned (non-empty), the response
   * header will be updated to indicate which patient the records applies to.
   */
  Function<EntityT, Optional<String>> toPatientId;

  /** This function is used to extract the raw payload from the entity. */
  Function<EntityT, String> toPayload;

  public static <EntityT, DatamartT extends HasReplaceableId, ResourceT extends Resource, PkT>
      VulcanizedReaderBuilder<EntityT, DatamartT, ResourceT, PkT> forTransformation(
          VulcanizedTransformation<EntityT, DatamartT, ResourceT> transformation) {
    return VulcanizedReader.<EntityT, DatamartT, ResourceT, PkT>builder()
        .transformation(transformation);
  }

  private EntityT find(Function<String, PkT> toPrimaryKey, String publicId) {
    String cdwId = transformation.witnessProtection().toCdwId(publicId);
    Optional<EntityT> entity = repository.findById(toPrimaryKey.apply(cdwId));
    entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
    return entity.get();
  }

  /** Perform a read for the given public ID and transform the results. */
  public ResourceT read(Function<String, PkT> toPrimaryKey, String publicId) {
    EntityT entity = find(toPrimaryKey, publicId);
    DatamartT datamart = transformation.toDatamart().apply(entity);
    return transformation.toResource().apply(transformation.applyWitnessProtection(datamart));
  }

  /**
   * Perform a raw read for the given public ID. The response headers will be updated if a patient
   * ID is associated with results.
   */
  public String readRaw(
      Function<String, PkT> toPrimaryKey, String publicId, HttpServletResponse response) {
    EntityT entity = find(toPrimaryKey, publicId);
    Optional<String> patientId = toPatientId.apply(entity);
    if (patientId.isPresent()) {
      IncludesIcnMajig.addHeader(response, patientId.get());
    } else {
      IncludesIcnMajig.addHeaderForNoPatients(response);
    }
    return toPayload.apply(entity);
  }
}
