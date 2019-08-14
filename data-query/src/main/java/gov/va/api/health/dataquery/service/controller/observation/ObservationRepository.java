package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.dataquery.service.controller.DateTimeParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

@Loggable
public interface ObservationRepository
    extends PagingAndSortingRepository<ObservationEntity, String>,
        JpaSpecificationExecutor<ObservationEntity> {
  Page<ObservationEntity> findByIcn(String icn, Pageable pageable);

  @Value
  class PatientAndCategoryAndDateSpecification implements Specification<ObservationEntity> {
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
        Root<ObservationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> predicates = new ArrayList<>(4);
      predicates.add(criteriaBuilder.equal(root.get("icn"), patient()));
      predicates.add(criteriaBuilder.equal(root.get("category"), category()));
      if (date1() != null) {
        predicates.add(date1().toPredicate(root.get("epochTime"), criteriaBuilder));
      }
      if (date2() != null) {
        predicates.add(date2().toPredicate(root.get("epochTime"), criteriaBuilder));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
  }

  @Value
  class PatientAndCodesSpecification implements Specification<ObservationEntity> {
    String patient;

    Set<String> codes;

    @Builder
    private PatientAndCodesSpecification(String patient, Collection<String> codes) {
      this.patient = patient;
      this.codes = new HashSet<>(codes);
    }

    @Override
    public Predicate toPredicate(
        Root<ObservationEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      return criteriaBuilder.and(
          criteriaBuilder.equal(root.get("icn"), patient()),
          criteriaBuilder.or(
              codes
                  .stream()
                  .map(c -> criteriaBuilder.equal(root.get("code"), c))
                  .toArray(Predicate[]::new)));
    }
  }
}
