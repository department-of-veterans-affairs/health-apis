package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.fhir.api.FhirDateTimeParameter;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lombok.Value;

@Value
public class JpaDateTimeParameter implements Serializable {
  private FhirDateTimeParameter parameter;

  public JpaDateTimeParameter(String paramString) {
    parameter = new FhirDateTimeParameter(paramString);
  }

  /**
   * Convert this time parameter into a Criteria API predicate. The 'field' is represents the value
   * of the JPA entity property/column. The criteria builder will be used to create the predicates.
   */
  public Predicate toInstantPredicate(Expression<Instant> field, CriteriaBuilder criteriaBuilder) {
    Instant lowerBound = parameter.lowerBound();
    Instant upperBound = parameter.upperBound();
    switch (parameter.prefix()) {
      case EQ:
        return criteriaBuilder.and(
            criteriaBuilder.greaterThanOrEqualTo(field, lowerBound),
            criteriaBuilder.lessThanOrEqualTo(field, upperBound));
      case NE:
        return criteriaBuilder.or(
            criteriaBuilder.lessThan(field, lowerBound),
            criteriaBuilder.greaterThan(field, upperBound));
      case GT:
        // fall-through
      case SA:
        return criteriaBuilder.greaterThan(field, upperBound);
      case LT:
        // fall-through
      case EB:
        return criteriaBuilder.lessThan(field, lowerBound);
      case GE:
        return criteriaBuilder.greaterThanOrEqualTo(field, lowerBound);
      case LE:
        return criteriaBuilder.lessThanOrEqualTo(field, upperBound);
      case AP:
        throw new UnsupportedOperationException("AP search prefix not implemented");
      default:
        throw new IllegalArgumentException("Unknown search prefix: " + parameter.prefix());
    }
  }

  /**
   * Convert this time parameter into a Criteria API predicate. The 'field' is represents the value
   * of the JPA entity property/column. The criteria builder will be used to create the predicates.
   */
  public Predicate toPredicate(
      Expression<? extends Number> field, CriteriaBuilder criteriaBuilder) {
    long lowerBound = parameter.lowerBound().toEpochMilli();
    long upperBound = parameter.upperBound().toEpochMilli();
    switch (parameter.prefix()) {
      case EQ:
        return criteriaBuilder.and(
            criteriaBuilder.ge(field, lowerBound), criteriaBuilder.le(field, upperBound));
      case NE:
        return criteriaBuilder.or(
            criteriaBuilder.lt(field, lowerBound), criteriaBuilder.gt(field, upperBound));
      case GT: // fall-through
      case SA:
        return criteriaBuilder.gt(field, upperBound);
      case LT: // fall-through
      case EB:
        return criteriaBuilder.lt(field, lowerBound);
      case GE:
        return criteriaBuilder.ge(field, lowerBound);
      case LE:
        return criteriaBuilder.le(field, upperBound);
      case AP:
        throw new UnsupportedOperationException("AP search prefix not implemented");
      default:
        throw new IllegalArgumentException("Unknown search prefix: " + parameter.prefix());
    }
  }
}
