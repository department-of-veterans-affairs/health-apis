package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.autoconfig.logging.Loggable;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

@Loggable
public interface LocationRepository
    extends PagingAndSortingRepository<LocationEntity, String>,
        JpaSpecificationExecutor<LocationEntity> {
  Page<LocationEntity> findByName(String name, Pageable pageable);

  @Value
  @Builder
  class AddressSpecification implements Specification<LocationEntity> {
    String street;

    String city;

    String state;

    String postalCode;

    @Override
    public Predicate toPredicate(
        Root<LocationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(4);
      if (street != null) {
        predicates.add(criteriaBuilder.equal(root.get("street"), street()));
      }
      if (city != null) {
        predicates.add(criteriaBuilder.equal(root.get("city"), city()));
      }
      if (state != null) {
        predicates.add(criteriaBuilder.equal(root.get("state"), state()));
      }
      if (postalCode != null) {
        predicates.add(criteriaBuilder.equal(root.get("postalCode"), postalCode()));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }
}
