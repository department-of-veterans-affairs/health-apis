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

  /**
   * If address and another address-* parameter is specified, assume the more specific parameter
   * value, e.g. address=FL&address-postalcode=32934 would use FL for state, street, city, and
   * country and 32934 for postal code.
   *
   * Example:
   * (state=FL or street=FL or city=FL or country=FL) and (postalCode=32934).
   */
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
      Predicate inferredPredicate =
          criteriaBuilder.or(inferredPredicates.toArray(new Predicate[0]));
      Predicate explicitPredicate =
          criteriaBuilder.and(explicitPredicates.toArray(new Predicate[0]));
      if (inferredPredicates.isEmpty() || street() == null) {
        return explicitPredicate;
      } else if (explicitPredicates.isEmpty()) {
        return inferredPredicate;
      }
      return criteriaBuilder.and(inferredPredicate, explicitPredicate);
    }
  }
}
