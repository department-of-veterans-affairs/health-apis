package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.autoconfig.logging.Loggable;
import java.util.Set;
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
public interface ConditionRepository
    extends PagingAndSortingRepository<ConditionEntity, String>,
        JpaSpecificationExecutor<ConditionEntity> {
  Page<ConditionEntity> findByIcn(String icn, Pageable pageable);

  Page<ConditionEntity> findByIcnAndCategory(String icn, String category, Pageable pageable);

  Page<ConditionEntity> findByIcnAndClinicalStatusIn(
      String icn, Set<String> clinicalStatus, Pageable pageable);

  @Value
  class CodeSpecification implements Specification<ConditionEntity> {
    String code;

    @Builder
    private CodeSpecification(String code) {
      this.code = code;
    }

    @Override
    public Predicate toPredicate(
        Root<ConditionEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {

      Predicate predicate = criteriaBuilder.equal(root.get("category"), code());
      return criteriaBuilder.and(predicate);
    }
  }

  @Value
  class AnyCodeSpecification implements Specification<ConditionEntity> {

    @Builder
    private AnyCodeSpecification() {}

    @Override
    public Predicate toPredicate(
        Root<ConditionEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {

      Predicate predicate = criteriaBuilder.isNotNull(root.get("category"));
      return criteriaBuilder.and(predicate);
    }
  }

  @Value
  class PatientSpecification implements Specification<ConditionEntity> {
    String icn;

    @Builder
    private PatientSpecification(String icn) {
      this.icn = icn;
    }

    @Override
    public Predicate toPredicate(
        Root<ConditionEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {

      Predicate predicate = criteriaBuilder.equal(root.get("icn"), icn());
      return criteriaBuilder.and(predicate);
    }
  }
}
