package gov.va.api.health.dataquery.service.controller.vulcanizer;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import org.springframework.data.repository.CrudRepository;

@Builder
public class VulcanizedReader<
    EntityT, DatamartT extends HasReplaceableId, ResourceT extends Resource> {

  Function<EntityT, DatamartT> toDatamart;
  WitnessProtection witnessProtection;
  Function<DatamartT, Stream<DatamartReference>> replaceReferences;
  Function<DatamartT, ResourceT> toResource;
  CrudRepository<EntityT, String> repository;
  Function<EntityT, Optional<String>> toPatientId;
  Function<EntityT, String> toPayload;

  private EntityT find(String publicId) {
    String cdwId = witnessProtection.toCdwId(publicId);
    Optional<EntityT> entity = repository.findById(cdwId);
    entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
    return entity.get();
  }

  public ResourceT read(String publicId) {
    EntityT entity = find(publicId);
    DatamartT datamart = toDatamart.apply(entity);
    witnessProtection.registerAndUpdateReferences(List.of(datamart), replaceReferences);
    return toResource.apply(datamart);
  }

  public String readRaw(String publicId, HttpServletResponse response) {
    EntityT entity = find(publicId);
    toPatientId.apply(entity).ifPresent(p -> IncludesIcnMajig.addHeader(response, p));
    return toPayload.apply(entity);
  }
}
