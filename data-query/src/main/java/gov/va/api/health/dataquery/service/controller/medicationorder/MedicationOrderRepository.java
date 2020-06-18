package gov.va.api.health.dataquery.service.controller.medicationorder;

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
public interface MedicationOrderRepository
    extends PagingAndSortingRepository<MedicationOrderEntity, String>,
        JpaSpecificationExecutor<MedicationOrderEntity> {
  Page<MedicationOrderEntity> findByIcn(String icn, Pageable pageable);

  @Value
  class PatientSpecification implements Specification<MedicationOrderEntity> {
    String patient;

    @Builder
    private PatientSpecification(String patient) {
      this.patient = patient;
    }

    @Override
    public Predicate toPredicate(
        Root<MedicationOrderEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {

      return criteriaBuilder.equal(root.get("icn"), patient());
    }
  }
}
