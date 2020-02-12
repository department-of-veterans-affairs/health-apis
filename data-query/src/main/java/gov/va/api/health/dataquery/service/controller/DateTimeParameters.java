package gov.va.api.health.dataquery.service.controller;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lombok.Value;

@Value
public final class DateTimeParameters implements Serializable {

  private static final int YEAR = 4;

  private static final int YEAR_MONTH = 7;

  private static final int YEAR_MONTH_DAY = 10;

  private static final int TIME_ZONE = 20;

  private static final int TIME_ZONE_OFFSET = 25;

  SearchPrefix prefix;

  String date;

  /** Extract prefix and date from parameter string. */
  public DateTimeParameters(String paramString) {
    super();
    if (paramString.length() <= 1) {
      throw new IllegalArgumentException(
          String.format("'%s' is not a valid date-time parameter", paramString));
    }
    if (Character.isLetter(paramString.charAt(0))) {
      prefix = SearchPrefix.valueOf(paramString.substring(0, 2).toUpperCase(Locale.US));
      date = paramString.substring(2);
    } else {
      prefix = SearchPrefix.EQ;
      date = paramString;
    }
  }

  static UnsupportedOperationException apException() {
    return new UnsupportedOperationException("AP search prefix not implemented");
  }

  /**
   * Indicates if the given date range (epoch millis) satisfies this date-time parameter.
   *
   * <p>See JpaDateTimeParameter.toQuerySnippet(...)
   */
  public boolean isSatisfied(long lower, long upper) {
    checkArgument(lower <= upper);
    long lowerBound = lowerBound().toEpochMilli();
    long upperBound = upperBound().toEpochMilli();

    switch (prefix()) {
      case EQ:
        // the range of the search value fully contains the range of the target value
        return lowerBound <= lower && upper <= upperBound;

      case NE:
        // the range of the search value does not fully contain the range of the target value
        return lower < lowerBound || upperBound < upper;

      case GT:
        // the range above the search value intersects the range of the target value
        return upperBound < upper;

      case LT:
        // the range below the search value intersects the range of the target value
        return lower < lowerBound;

      case GE:
        // or the range of the search value fully contains the range of the target value
        return lowerBound <= lower || upperBound < upper;

      case LE:
        // or the range of the search value fully contains the range of the target value
        return lower < lowerBound || upper <= upperBound;

      case SA:
        // and the range above the search value contains the range of the target value
        return upperBound < lower;

      case EB:
        // and the range below the search value contains the range of the target value
        return upper < lowerBound;

      case AP:
        throw apException();

      default:
        throw new IllegalArgumentException("Unknown search prefix: " + prefix());
    }
  }

  Instant lowerBound() {
    ZoneOffset offset = ZonedDateTime.now().getOffset();
    switch (date().length()) {
      case YEAR:
        return OffsetDateTime.parse(String.format("%s-01-01T00:00:00%s", date(), offset))
            .toInstant();

      case YEAR_MONTH:
        return OffsetDateTime.parse(String.format("%s-01T00:00:00%s", date(), offset)).toInstant();

      case YEAR_MONTH_DAY:
        return OffsetDateTime.parse(String.format("%sT00:00:00%s", date(), offset)).toInstant();

      case TIME_ZONE:
        return Instant.parse(date());

      case TIME_ZONE_OFFSET:
        return OffsetDateTime.parse(date()).toInstant();

      default:
        throw new IllegalArgumentException("Cannot compute lower bound for date " + date());
    }
  }

  /**
   * Convert this time parameter into a Criteria API predicate. The 'field' is represents the value
   * of the JPA entity property/column. The criteria builder will be used to create the predicates.
   */
  public Predicate toInstantPredicate(Expression<Instant> field, CriteriaBuilder criteriaBuilder) {
    Instant lowerBound = lowerBound();
    Instant upperBound = upperBound();
    switch (prefix()) {
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
        throw apException();
      default:
        throw new IllegalArgumentException("Unknown search prefix: " + prefix());
    }
  }

  /**
   * Convert this time parameter into a Criteria API predicate. The 'field' is represents the value
   * of the JPA entity property/column. The criteria builder will be used to create the predicates.
   */
  public Predicate toPredicate(
      Expression<? extends Number> field, CriteriaBuilder criteriaBuilder) {
    long lowerBound = lowerBound().toEpochMilli();
    long upperBound = upperBound().toEpochMilli();
    switch (prefix()) {
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
        throw apException();
      default:
        throw new IllegalArgumentException("Unknown search prefix: " + prefix());
    }
  }

  Instant upperBound() {
    OffsetDateTime lowerBound =
        OffsetDateTime.ofInstant(lowerBound(), ZonedDateTime.now().getOffset());
    switch (date().length()) {
      case YEAR:
        return lowerBound.plusYears(1).minus(1, ChronoUnit.MILLIS).toInstant();

      case YEAR_MONTH:
        return lowerBound.plusMonths(1).minus(1, ChronoUnit.MILLIS).toInstant();

      case YEAR_MONTH_DAY:
        return lowerBound.plusDays(1).minus(1, ChronoUnit.MILLIS).toInstant();

      case TIME_ZONE:
        // falls through
      case TIME_ZONE_OFFSET:
        return lowerBound.plusSeconds(1).minus(1, ChronoUnit.MILLIS).toInstant();

      default:
        throw new IllegalArgumentException("Cannot compute upper bound for date " + date());
    }
  }

  static enum SearchPrefix {
    EQ,
    NE,
    GT,
    LT,
    GE,
    LE,
    SA,
    EB,
    AP
  }
}
