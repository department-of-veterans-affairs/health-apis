package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.autoconfig.logging.Loggable;
import java.util.ArrayList;
import java.util.List;
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
    String patient;

    String code;

    @Builder
    private CodeSpecification(String patient, String code) {
      this.patient = patient;
      this.code = code;
    }

    @Override
    public Predicate toPredicate(
        Root<ImmunizationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {

      List<Predicate> predicates = new ArrayList<>(4);
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));
      predicates.add(criteriaBuilder.equal(root.get("status"), code()));
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }
}
