package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.DateTimeParameters;
import java.util.ArrayList;
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
public interface PatientRepositoryV2
    extends PagingAndSortingRepository<PatientEntityV2, String>,
        JpaSpecificationExecutor<PatientEntityV2> {
  /**
   * A paged search that returns a view of the patients with just the payload column read.
   *
   * @param page The page data to find
   * @return A page of patient payloads
   */
  Page<PatientPayloadDto> findAllProjectedBy(Pageable page);

  Page<PatientEntityV2> findByFirstName(String firstName, Pageable pageable);

  Page<PatientEntityV2> findByFirstNameAndGender(
      String firstName, String gender, Pageable pageable);

  Page<PatientEntityV2> findByFullName(String name, Pageable pageable);

  Page<PatientEntityV2> findByFullNameAndGender(String name, String gender, Pageable pageable);

  Page<PatientEntityV2> findByIcn(String icn, Pageable pageable);

  Page<PatientEntityV2> findByLastNameAndGender(String lastName, String gender, Pageable pageable);

  @Value
  class NameAndBirthdateSpecification implements Specification<PatientEntityV2> {
    String name;

    DateTimeParameters date1;

    DateTimeParameters date2;

    @Builder
    private NameAndBirthdateSpecification(String name, String[] dates) {
      this.name = name;
      date1 = (dates == null || dates.length < 1) ? null : new DateTimeParameters(dates[0]);
      date2 = (dates == null || dates.length < 2) ? null : new DateTimeParameters(dates[1]);
    }

    @Override
    public Predicate toPredicate(
        Root<PatientEntityV2> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      var predicates = new ArrayList<>(3);
      predicates.add(criteriaBuilder.equal(root.get("fullName"), name()));
      if (date1() != null) {
        predicates.add(date1().toInstantPredicate(root.get("birthDate"), criteriaBuilder));
      }
      if (date2() != null) {
        predicates.add(date2().toInstantPredicate(root.get("birthDate"), criteriaBuilder));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }

  @Value
  class BirthdateAndFamilySpecification implements Specification<PatientEntityV2> {
    DateTimeParameters date1;

    DateTimeParameters date2;

    String family;

    @Builder
    private BirthdateAndFamilySpecification(String family, String[] dates) {
      this.family = family;
      date1 = (dates == null || dates.length < 1) ? null : new DateTimeParameters(dates[0]);
      date2 = (dates == null || dates.length < 2) ? null : new DateTimeParameters(dates[1]);
    }

    @Override
    public Predicate toPredicate(
        Root<PatientEntityV2> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      var predicates = new ArrayList<>(3);
      predicates.add(criteriaBuilder.equal(root.get("lastName"), family()));
      if (date1() != null) {
        predicates.add(date1().toInstantPredicate(root.get("birthDate"), criteriaBuilder));
      }
      if (date2() != null) {
        predicates.add(date2().toInstantPredicate(root.get("birthDate"), criteriaBuilder));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }
}
