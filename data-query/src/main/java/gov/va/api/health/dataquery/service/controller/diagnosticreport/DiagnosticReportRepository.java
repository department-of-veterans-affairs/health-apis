package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.DateTimeParameters;
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
public interface DiagnosticReportRepository
    extends PagingAndSortingRepository<DiagnosticReportEntity, String>,
        JpaSpecificationExecutor<DiagnosticReportEntity> {
  Page<DiagnosticReportEntity> findByIcn(String icn, Pageable pageable);

  @Value
  class PatientAndCategoryAndDateSpecification implements Specification<DiagnosticReportEntity> {
    String patient;

    String category;

    DateTimeParameters date1;

    DateTimeParameters date2;

    @Builder
    private PatientAndCategoryAndDateSpecification(
        String patient, String category, String[] dates) {
      this.patient = patient;
      this.category = category;
      date1 = (dates == null || dates.length < 1) ? null : new DateTimeParameters(dates[0]);
      date2 = (dates == null || dates.length < 2) ? null : new DateTimeParameters(dates[1]);
    }

    @Override
    public Predicate toPredicate(
        Root<DiagnosticReportEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(4);

      // Patient
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));

      // Category
      predicates.add(criteriaBuilder.equal(root.get("category"), category()));

      // Date(s)
      if (date1() != null) {
        predicates.add(date1().toInstantPredicate(root.get("dateUtc"), criteriaBuilder));
      }
      if (date2() != null) {
        predicates.add(date2().toInstantPredicate(root.get("dateUtc"), criteriaBuilder));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }

  @Value
  class PatientAndCodeSpecification implements Specification<DiagnosticReportEntity> {
    String patient;

    String code;

    @Builder
    private PatientAndCodeSpecification(String patient, String code) {
      this.patient = patient;
      this.code = code;
    }

    @Override
    public Predicate toPredicate(
        Root<DiagnosticReportEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(2);

      // Patient
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));

      // Code
      if (code() != null) {
        predicates.add(criteriaBuilder.equal(root.get("code"), code()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }
}
