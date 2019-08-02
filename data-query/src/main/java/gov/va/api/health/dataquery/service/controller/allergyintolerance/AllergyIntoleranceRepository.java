package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AllergyIntoleranceRepository
    extends PagingAndSortingRepository<AllergyIntoleranceEntity, String> {}
