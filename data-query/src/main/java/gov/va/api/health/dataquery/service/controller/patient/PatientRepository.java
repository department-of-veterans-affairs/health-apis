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

@Loggable
public interface PatientRepository
    extends PagingAndSortingRepository<PatientSearchEntity, String>,
        JpaSpecificationExecutor<PatientSearchEntity> {
  Page<PatientSearchEntity> findByFamilyAndGender(String family, String gender, Pageable pageable);

  Page<PatientSearchEntity> findByGivenAndGender(String given, String gender, Pageable pageable);

  Page<PatientSearchEntity> findByNameAndGender(String name, String gender, Pageable pageable);

  @Value
  class NameAndBirthdateSpecification implements Specification<PatientSearchEntity> {
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
        Root<PatientSearchEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      var predicates = new ArrayList<>(3);
      predicates.add(criteriaBuilder.equal(root.get("name"), name()));
      if (date1() != null) {
        predicates.add(date1().toPredicate2(root.get("birthDateTime"), criteriaBuilder));
      }
      if (date2() != null) {
        predicates.add(date2().toPredicate2(root.get("birthDateTime"), criteriaBuilder));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }
}
