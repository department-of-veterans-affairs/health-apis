package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.autoconfig.logging.Loggable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public interface LocationRepository
    extends PagingAndSortingRepository<LocationEntity, String>,
        JpaSpecificationExecutor<LocationEntity> {
  Page<LocationEntity> findByName(String name, Pageable pageable);

  /**
   * If address and another address-* parameter is specified, assume the more specific parameter
   * value, e.g. address=FL&address-postalcode=32934 would use FL for state, street, city, and
   * country and 32934 for postal code.
   *
   * <p>Example: (state=FL or street=FL or city=FL or country=FL) and (postalCode=32934).
   */
  @Value
  @Builder
  class AddressSpecification implements Specification<LocationEntity> {

    String address;

    String street;

    String city;

    String state;

    String postalCode;

    @Override
    public Predicate toPredicate(
        Root<LocationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> explicitPredicates = new ArrayList<>(4);
      List<Predicate> inferredPredicates = new ArrayList<>(4);

      if (address != null) {
        String addressPattern = "%" + address().toLowerCase(Locale.US) + "%";
        for (String property : new String[] {"street", "city", "state", "postalCode"}) {
          inferredPredicates.add(
              criteriaBuilder.like(criteriaBuilder.lower(root.get(property)), addressPattern));
        }
      }
      if (street != null) {
        explicitPredicates.add(criteriaBuilder.equal(root.get("street"), street()));
      }
      if (city != null) {
        explicitPredicates.add(criteriaBuilder.equal(root.get("city"), city()));
      }
      if (state != null) {
        explicitPredicates.add(criteriaBuilder.equal(root.get("state"), state()));
      }
      if (postalCode != null) {
        explicitPredicates.add(criteriaBuilder.equal(root.get("postalCode"), postalCode()));
      }

      Predicate anyInferredPredicate =
          criteriaBuilder.or(inferredPredicates.toArray(new Predicate[0]));
      Predicate everyExplicitPredicate =
          criteriaBuilder.and(explicitPredicates.toArray(new Predicate[0]));

      if (inferredPredicates.isEmpty() && explicitPredicates.isEmpty()) {
        throw new IllegalArgumentException(
            "At least one of address, street, city, state, or postalCode must be specified.");
      } else if (inferredPredicates.isEmpty()) {
        return everyExplicitPredicate;
      } else if (explicitPredicates.isEmpty()) {
        return anyInferredPredicate;
      } else {
        return criteriaBuilder.and(anyInferredPredicate, everyExplicitPredicate);
      }
    }
  }
}
