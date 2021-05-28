package gov.va.api.health.dataquery.service.controller.procedure;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.JpaDateTimeParameter;
import java.util.ArrayList;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Procedure DB. */
@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface ProcedureRepository
    extends PagingAndSortingRepository<ProcedureEntity, String>,
        JpaSpecificationExecutor<ProcedureEntity> {

  /** Query specification for searching by patient and date. */
  @Value
  class PatientAndDateSpecification implements Specification<ProcedureEntity> {
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
        Root<ProcedureEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      var predicates = new ArrayList<>(3);
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));
      if (date1() != null) {
        predicates.add(date1().toPredicate(root.get("performedOnEpochTime"), criteriaBuilder));
      }
      if (date2() != null) {
        predicates.add(date2().toPredicate(root.get("performedOnEpochTime"), criteriaBuilder));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }
}
