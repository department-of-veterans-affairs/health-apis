package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface AppointmentRepository
    extends PagingAndSortingRepository<AppointmentEntity, CompositeCdwId>,
        JpaSpecificationExecutor<AppointmentEntity>,
        JpaRepository<AppointmentEntity, CompositeCdwId> {
  @Value
  @RequiredArgsConstructor(staticName = "of")
  class PatientSpecification implements Specification<AppointmentEntity> {
    String icn;

    @Override
    public Predicate toPredicate(
        Root<AppointmentEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      return criteriaBuilder.equal(root.get("icn"), icn());
    }
  }
}
