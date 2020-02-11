package gov.va.api.health.dataquery.service.controller.procedure;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.DateTimeParameters;
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

@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface ProcedureRepository
    extends PagingAndSortingRepository<ProcedureEntity, String>,
        JpaSpecificationExecutor<ProcedureEntity> {

  @Value
  class PatientAndDateSpecification implements Specification<ProcedureEntity> {
    String patient;
    DateTimeParameters date1;
    DateTimeParameters date2;

    @Builder
    private PatientAndDateSpecification(String patient, String[] dates) {
      this.patient = patient;
      date1 = (dates == null || dates.length < 1) ? null : new DateTimeParameters(dates[0]);
      date2 = (dates == null || dates.length < 2) ? null : new DateTimeParameters(dates[1]);
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
