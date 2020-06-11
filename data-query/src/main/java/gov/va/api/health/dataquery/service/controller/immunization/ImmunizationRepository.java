package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.autoconfig.logging.Loggable;
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
public interface ImmunizationRepository
    extends PagingAndSortingRepository<ImmunizationEntity, String>,
        JpaSpecificationExecutor<ImmunizationEntity> {
  Page<ImmunizationEntity> findByIcn(String icn, Pageable pageable);

  @Value
  class CodeSpecification implements Specification<ImmunizationEntity> {
    String code;

    @Builder
    private CodeSpecification(String code) {
      this.code = code;
    }

    @Override
    public Predicate toPredicate(
        Root<ImmunizationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {

      Predicate predicate = criteriaBuilder.equal(root.get("status"), code());
      return criteriaBuilder.and(predicate);
    }
  }

  @Value
  class SystemSpecification implements Specification<ImmunizationEntity> {

    @Builder
    private SystemSpecification(String ignored) {}

    @Override
    public Predicate toPredicate(
        Root<ImmunizationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {

      Predicate predicate = criteriaBuilder.isNotNull(root.get("status"));
      return criteriaBuilder.and(predicate);
    }
  }

  @Value
  class PatientSpecification implements Specification<ImmunizationEntity> {
    String patient;

    @Builder
    private PatientSpecification(String patient) {
      this.patient = patient;
    }

    @Override
    public Predicate toPredicate(
        Root<ImmunizationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {

      Predicate predicate = criteriaBuilder.equal(root.get("patient"), patient());
      return criteriaBuilder.and(predicate);
    }
  }
}
