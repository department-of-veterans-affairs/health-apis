package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.DateTimeParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
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
  @AllArgsConstructor(staticName = "of")
  class DateSpecification implements Specification<DiagnosticReportEntity> {

    DateTimeParameters date1;

    DateTimeParameters date2;

    @Override
    public Predicate toPredicate(
        Root<DiagnosticReportEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(2);

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
  class PatientAndCategoryAndDateSpecification implements Specification<DiagnosticReportEntity> {
    String patient;

    Set<DiagnosticReportEntity.CategoryCodes> categories;

    DateTimeParameters date1;

    DateTimeParameters date2;

    @Builder
    private PatientAndCategoryAndDateSpecification(
        String patient, Set<DiagnosticReportEntity.CategoryCodes> categories, String[] dates) {
      this.patient = patient;
      this.categories = categories;
      this.date1 = (dates == null || dates.length < 1) ? null : new DateTimeParameters(dates[0]);
      this.date2 = (dates == null || dates.length < 2) ? null : new DateTimeParameters(dates[1]);
    }

    @Override
    public Predicate toPredicate(
        Root<DiagnosticReportEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(3);

      // Patient
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));

      // Category
      CriteriaBuilder.In<String> categoriesInClause = criteriaBuilder.in(root.get("category"));
      categories.forEach(c -> categoriesInClause.value(c.toString()));
      predicates.add(categoriesInClause);

      // Date(s)
      if (!allBlank(date1(), date2())) {
        predicates.add(
            DateSpecification.of(date1(), date2())
                .toPredicate(root, criteriaQuery, criteriaBuilder));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }

  @Value
  class PatientAndCodeAndDateSpecification implements Specification<DiagnosticReportEntity> {
    String patient;

    Set<String> codes;

    DateTimeParameters date1;

    DateTimeParameters date2;

    @Builder
    private PatientAndCodeAndDateSpecification(String patient, Set<String> codes, String[] dates) {
      this.patient = patient;
      this.codes = codes;
      date1 = (dates == null || dates.length < 1) ? null : new DateTimeParameters(dates[0]);
      date2 = (dates == null || dates.length < 2) ? null : new DateTimeParameters(dates[1]);
    }

    @Override
    public Predicate toPredicate(
        Root<DiagnosticReportEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(3);

      // Patient
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));

      // Code
      if (!isBlank(codes())) {
        CriteriaBuilder.In<String> codesInClause = criteriaBuilder.in(root.get("code"));
        codes().forEach(codesInClause::value);
        predicates.add(codesInClause);
      }

      // Date(s)
      if (!allBlank(date1(), date2())) {
        predicates.add(
            DateSpecification.of(date1(), date2())
                .toPredicate(root, criteriaQuery, criteriaBuilder));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }
}
