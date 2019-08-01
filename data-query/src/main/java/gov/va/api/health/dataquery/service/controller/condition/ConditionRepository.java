package gov.va.api.health.dataquery.service.controller.condition;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface ConditionRepository extends PagingAndSortingRepository<ConditionEntity, String> {}
