package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.autoconfig.logging.Loggable;
import java.util.ArrayList;
import java.util.List;
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

      return criteriaBuilder.equal(root.get("category"), code());
    }
  }

  @Value
  class ExplicitSystemSpecification implements Specification<ConditionEntity> {
    List<String> codes;

    @Builder
    private ExplicitSystemSpecification(List<String> codes) {
      this.codes = codes;
    }

    @Override
    public Predicate toPredicate(
        Root<ConditionEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(2);
      codes.forEach(c -> predicates.add(criteriaBuilder.equal(root.get("category"), c)));
      return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
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

      return criteriaBuilder.equal(root.get("icn"), icn());
    }
  }
}
