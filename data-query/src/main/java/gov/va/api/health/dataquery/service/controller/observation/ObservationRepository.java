package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.DateTimeParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
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
public interface ObservationRepository
    extends PagingAndSortingRepository<ObservationEntity, String>,
        JpaSpecificationExecutor<ObservationEntity> {
  Page<ObservationEntity> findByIcn(String icn, Pageable pageable);

  @Value
  class PatientAndCategoryAndDateSpecification implements Specification<ObservationEntity> {
    String patient;

    Set<String> categories;

    DateTimeParameters date1;

    DateTimeParameters date2;

    @Builder
    private PatientAndCategoryAndDateSpecification(
        String patient, Collection<String> categories, String[] dates) {
      this.patient = patient;
      this.categories = new HashSet<>(categories);
      date1 = (dates == null || dates.length < 1) ? null : new DateTimeParameters(dates[0]);
      date2 = (dates == null || dates.length < 2) ? null : new DateTimeParameters(dates[1]);
    }

    @Override
    public Predicate toPredicate(
        Root<ObservationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(4);
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));

      In<String> categoriesInClause = criteriaBuilder.in(root.get("category"));
      categories.forEach(categoriesInClause::value);
      predicates.add(categoriesInClause);

      if (date1() != null) {
        predicates.add(date1().toInstantPredicate(root.get("dateUtc"), criteriaBuilder));
      }
      if (date2() != null) {
        predicates.add(date2().toInstantPredicate(root.get("dateUtc"), criteriaBuilder));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }

  @Value
  class PatientAndCodeAndDateSpecification implements Specification<ObservationEntity> {
    String patient;

    Set<String> codes;

    DateTimeParameters date1;

    DateTimeParameters date2;

    @Builder
    private PatientAndCodeAndDateSpecification(
        String patient, Collection<String> codes, String[] dates) {
      this.patient = patient;
      this.codes = new HashSet<>(codes);
      date1 = (dates == null || dates.length < 1) ? null : new DateTimeParameters(dates[0]);
      date2 = (dates == null || dates.length < 2) ? null : new DateTimeParameters(dates[1]);
    }

    @Override
    public Predicate toPredicate(
        Root<ObservationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {

      List<Predicate> predicates = new ArrayList<>(4);
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));

      In<String> codesInClause = criteriaBuilder.in(root.get("code"));
      codes.forEach(codesInClause::value);
      predicates.add(codesInClause);

      if (date1() != null) {
        predicates.add(date1().toInstantPredicate(root.get("dateUtc"), criteriaBuilder));
      }
      if (date2() != null) {
        predicates.add(date2().toInstantPredicate(root.get("dateUtc"), criteriaBuilder));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }
}
