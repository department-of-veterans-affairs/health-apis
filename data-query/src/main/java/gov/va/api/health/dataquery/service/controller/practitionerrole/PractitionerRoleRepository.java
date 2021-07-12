package gov.va.api.health.dataquery.service.controller.practitionerrole;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
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
public interface PractitionerRoleRepository
    extends PagingAndSortingRepository<PractitionerRoleEntity, CompositeCdwId>,
        JpaSpecificationExecutor<PractitionerRoleEntity> {
  Page<PractitionerRoleEntity> findByFamilyNameAndGivenName(
      String familyName, String givenName, Pageable pageable);

  Page<PractitionerRoleEntity> findByNpi(String npi, Pageable pageable);

  List<PractitionerRoleEntity> findByPractitionerIdNumberAndPractitionerResourceCode(
      BigInteger idNumber, char practitionerResourceCode);

  @Value
  @RequiredArgsConstructor(staticName = "of")
  class SpecialtySpecification implements Specification<PractitionerRoleEntity> {
    String code;

    private Subquery<Boolean> specialtyMapSubquery(
        Root<PractitionerRoleEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      var subquery = criteriaQuery.subquery(Boolean.class);
      var subqueryRoot = subquery.from(PractitionerRoleSpecialtyMapEntity.class);
      subquery.select(criteriaBuilder.literal(true));
      var predicateSpecialtyCode = criteriaBuilder.equal(subqueryRoot.get("specialtyCode"), code);
      var predicateCdwId =
          criteriaBuilder.equal(
              subqueryRoot.get("practitionerRoleIdNumber"), root.get("cdwIdNumber"));
      var predicateCdwResourceCode =
          criteriaBuilder.equal(
              subqueryRoot.get("practitionerRoleResourceCode"), root.get("cdwIdResourceCode"));
      subquery.where(
          criteriaBuilder.and(predicateSpecialtyCode, predicateCdwId, predicateCdwResourceCode));
      return subquery;
    }

    @Override
    public Predicate toPredicate(
        Root<PractitionerRoleEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      Predicate roleIsActive = criteriaBuilder.equal(root.get("active"), true);
      Subquery<Boolean> subquery = specialtyMapSubquery(root, criteriaQuery, criteriaBuilder);
      return criteriaBuilder.and(roleIsActive, criteriaBuilder.exists(subquery));
    }
  }
}
