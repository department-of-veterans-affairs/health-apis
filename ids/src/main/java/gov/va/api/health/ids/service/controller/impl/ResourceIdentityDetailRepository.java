package gov.va.api.health.ids.service.controller.impl;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ResourceIdentityDetailRepository
    extends CrudRepository<ResourceIdentityDetail, Integer> {
  List<ResourceIdentityDetail> findBySystemAndResourceAndIdentifier(
      String system, String resource, String identifier);

  List<ResourceIdentityDetail> findByUuid(String uuid);
}
