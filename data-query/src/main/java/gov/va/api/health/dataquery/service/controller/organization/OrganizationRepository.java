package gov.va.api.health.dataquery.service.controller.organization;

import gov.va.api.health.autoconfig.logging.Loggable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface OrganizationRepository
    extends PagingAndSortingRepository<OrganizationEntity, String>,
        JpaSpecificationExecutor<OrganizationEntity> {

  Page<OrganizationEntity> findByName(String name, Pageable pageable);

  Page<OrganizationEntity> findByNpi(String npi, Pageable pageable);

  @Value
  @Builder
  class AddressSpecification implements Specification<OrganizationEntity> {

    String street;

    String city;

    String state;

    String postalCode;

    @Override
    public Predicate toPredicate(
        Root<OrganizationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> explicitPredicates = new ArrayList<>(4);
      List<Predicate> inferredPredicates = new ArrayList<>(4);
      if (street != null) {
        inferredPredicates.add(criteriaBuilder.equal(root.get("street"), street()));
      }
      if (city != null) {
        explicitPredicates.add(criteriaBuilder.equal(root.get("city"), city()));
      } else {
        inferredPredicates.add(criteriaBuilder.equal(root.get("city"), street()));
      }
      if (state != null) {
        explicitPredicates.add(criteriaBuilder.equal(root.get("state"), state()));
      } else {
        inferredPredicates.add(criteriaBuilder.equal(root.get("state"), street()));
      }
      if (postalCode != null) {
        explicitPredicates.add(criteriaBuilder.equal(root.get("postalCode"), postalCode()));
      } else {
        inferredPredicates.add(criteriaBuilder.equal(root.get("postalCode"), street()));
      }
      criteriaBuilder.or(inferredPredicates.toArray(new Predicate[0]));
      return criteriaBuilder.and(explicitPredicates.toArray(new Predicate[0]));
    }
  }
}
