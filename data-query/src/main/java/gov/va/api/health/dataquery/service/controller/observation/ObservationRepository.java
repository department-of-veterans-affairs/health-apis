package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.JpaDateTimeParameter;
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
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Interact with the database and retrieve Observation entities. */
@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface ObservationRepository
    extends PagingAndSortingRepository<ObservationEntity, String>,
        JpaSpecificationExecutor<ObservationEntity> {
  String OBSERVATION_CODE_SYSTEM = "http://loinc.org";

  /**
   * This is a magic value used by code specification to include results that have any non-null code
   * value.
   */
  String ANY_CODE_VALUE = ObservationRepository.class.getName() + ".ANY_CODE_VALUE";

  Page<ObservationEntity> findByIcn(String icn, Pageable pageable);

  /** Search by category. */
  @Value
  @RequiredArgsConstructor(staticName = "of")
  class CategorySpecification implements Specification<ObservationEntity> {
    Set<String> categories;

    @Override
    public Predicate toPredicate(
        Root<ObservationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      In<String> categoriesInClause = criteriaBuilder.in(root.get("category"));
      categories.forEach(categoriesInClause::value);
      return criteriaBuilder.or(categoriesInClause);
    }
  }

  /**
   * When creating the CodeSpecification, if the given 'codes' set contains {@link #ANY_CODE_VALUE},
   * then all other specific codes will be ignored and the predicate will include records with any
   * non-null code value.
   */
  @Value
  @RequiredArgsConstructor(staticName = "of")
  class CodeSpecification implements Specification<ObservationEntity> {
    Set<String> codes;

    @Override
    public Predicate toPredicate(
        Root<ObservationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      if (codes.contains(ANY_CODE_VALUE)) {
        return criteriaBuilder.isNotNull(root.get("code"));
      } else {
        In<String> categoriesInClause = criteriaBuilder.in(root.get("code"));
        codes.forEach(categoriesInClause::value);
        return criteriaBuilder.or(categoriesInClause);
      }
    }
  }

  /** Search by patient-icn, observation category, and date. */
  @Value
  class PatientAndCategoryAndDateSpecification implements Specification<ObservationEntity> {
    String patient;

    Set<String> categories;

    String[] dates;

    @Builder
    private PatientAndCategoryAndDateSpecification(
        String patient, Collection<String> categories, String[] dates) {
      this.patient = patient;
      this.categories = new HashSet<>(categories);
      this.dates = dates;
    }

    @Override
    public Predicate toPredicate(
        Root<ObservationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(2);

      predicates.add(
          PatientAndDateSpecification.builder()
              .patient(patient())
              .dates(dates())
              .build()
              .toPredicate(root, criteriaQuery, criteriaBuilder));

      predicates.add(
          CategorySpecification.of(categories()).toPredicate(root, criteriaQuery, criteriaBuilder));

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }

  /** Search by patient-icn, observation code, and date. */
  @Value
  class PatientAndCodeAndDateSpecification implements Specification<ObservationEntity> {
    String patient;

    Set<String> codes;

    String[] dates;

    @Builder
    private PatientAndCodeAndDateSpecification(
        String patient, Collection<String> codes, String[] dates) {
      this.patient = patient;
      this.codes = new HashSet<>(codes);
      this.dates = dates;
    }

    @Override
    public Predicate toPredicate(
        Root<ObservationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(2);
      predicates.add(
          PatientAndDateSpecification.builder()
              .patient(patient())
              .dates(dates())
              .build()
              .toPredicate(root, criteriaQuery, criteriaBuilder));

      In<String> codesInClause = criteriaBuilder.in(root.get("code"));
      codes.forEach(codesInClause::value);
      predicates.add(codesInClause);

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }

  /** Perform a search by patient-icn and date. */
  @Value
  class PatientAndDateSpecification implements Specification<ObservationEntity> {

    String patient;

    JpaDateTimeParameter date1;

    JpaDateTimeParameter date2;

    @Builder
    private PatientAndDateSpecification(String patient, String[] dates) {
      this.patient = patient;
      date1 = (dates == null || dates.length < 1) ? null : new JpaDateTimeParameter(dates[0]);
      date2 = (dates == null || dates.length < 2) ? null : new JpaDateTimeParameter(dates[1]);
    }

    @Override
    public Predicate toPredicate(
        Root<ObservationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(3);
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));

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
