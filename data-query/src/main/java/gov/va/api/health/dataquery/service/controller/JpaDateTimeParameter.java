package gov.va.api.health.dataquery.service.controller;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import javax.persistence.TypedQuery;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Getter(AccessLevel.PRIVATE)
public final class JpaDateTimeParameter {
  int index;

  SearchPrefix prefix;

  Instant lowerBound;

  Instant upperBound;

  @Builder
  private JpaDateTimeParameter(int index, String paramString) {
    super();
    this.index = index;
    if (Character.isLetter(paramString.charAt(0))) {
      prefix = SearchPrefix.valueOf(paramString.substring(0, 2).toUpperCase(Locale.US));
      lowerBound = computeLowerBound(paramString.substring(2));
      upperBound = computeUpperBound(paramString.substring(2));
    } else {
      prefix = SearchPrefix.EQ;
      lowerBound = computeLowerBound(paramString);
      upperBound = computeUpperBound(paramString);
    }

    log.error("lower bound is " + lowerBound);
    log.error("upper bound is " + upperBound);
  }

  /** Add query parameters for the upper and lower bound of each date. */
  public static void addQueryParametersForEach(TypedQuery<?> query, List<String> dateParams) {
    if (dateParams == null || dateParams.isEmpty()) {
      return;
    }
    JpaDateTimeParameter.builder()
        .index(0)
        .paramString(dateParams.get(0))
        .build()
        .addQueryParameters(query);
    if (dateParams.size() >= 2) {
      JpaDateTimeParameter.builder()
          .index(1)
          .paramString(dateParams.get(1))
          .build()
          .addQueryParameters(query);
    }
  }

  /** Build a combined JPA query snippet representing all the date criteria. */
  public static String querySnippet(String[] dates) {
    if (dates == null || dates.length == 0) {
      return "";
    }
    if (dates.length == 1) {
      return JpaDateTimeParameter.builder().index(0).paramString(dates[0]).build().toQuerySnippet();
    }
    return JpaDateTimeParameter.builder().index(0).paramString(dates[0]).build().toQuerySnippet()
        + JpaDateTimeParameter.builder().index(1).paramString(dates[1]).build().toQuerySnippet();
  }

  private void addQueryParameters(TypedQuery<?> query) {
    switch (prefix()) {
      case EQ:
      case NE:
      case GE:
      case LE:
        query.setParameter(lowerBoundPlaceholder(), lowerBound());
        query.setParameter(upperBoundPlaceholder(), upperBound());
        return;

      case GT:
      case SA:
        query.setParameter(upperBoundPlaceholder(), upperBound());
        return;

      case LT:
      case EB:
        query.setParameter(lowerBoundPlaceholder(), lowerBound());
        return;

      case AP:
        throw new UnsupportedOperationException();

      default:
        throw new IllegalArgumentException();
    }
  }

  private Instant computeLowerBound(String paramString) {
    log.error("Computing lower bound for " + paramString);
    ZoneOffset localOffset = ZonedDateTime.now().getOffset();
    switch (paramString.length()) {
      case 4:
        return OffsetDateTime.parse(String.format("%s-01-01T00:00:00%s", paramString, localOffset))
            .toInstant();
      case 7:
        return OffsetDateTime.parse(String.format("%s-01T00:00:00%s", paramString, localOffset))
            .toInstant();
      case 10:
        return OffsetDateTime.parse(String.format("%sT00:00:00%s", paramString, localOffset))
            .toInstant();
      case 13:
        return OffsetDateTime.parse(String.format("%s:00:00%s", paramString, localOffset))
            .toInstant();
      case 16:
        return OffsetDateTime.parse(String.format("%s:00%s", paramString, localOffset)).toInstant();
      case 19:
        return OffsetDateTime.parse(paramString + localOffset).toInstant();
      case 20:
        // falls through
      case 25:
        return OffsetDateTime.parse(paramString).toInstant();
      default:
        throw new IllegalArgumentException();
    }
  }

  private Instant computeUpperBound(String paramString) {
    log.error("Computing upper bound for " + paramString);
    OffsetDateTime theLowerBound =
        OffsetDateTime.ofInstant(computeLowerBound(paramString), ZonedDateTime.now().getOffset());
    switch (paramString.length()) {
      case 4:
        return theLowerBound.plusYears(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 7:
        return theLowerBound.plusMonths(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 10:
        return theLowerBound.plusDays(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 13:
        return theLowerBound.plusHours(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 16:
        return theLowerBound.plusMinutes(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 19:
        // falls through
      case 20:
        // falls through
      case 25:
        return theLowerBound.plusSeconds(1).minus(1, ChronoUnit.MILLIS).toInstant();
      default:
        throw new IllegalArgumentException();
    }
  }

  private String lowerBoundPlaceholder() {
    return "date" + index + "UpperBound";
  }

  private String toQuerySnippet() {
    switch (prefix()) {
      case EQ:
        // the range of the search value fully contains the range of the target value
        return String.format(
            " and :%s <= dr.effectiveDateTime and dr.issuedDateTime <= :%s",
            lowerBoundPlaceholder(), upperBoundPlaceholder());

      case NE:
        // the range of the search value does not fully contain the range of the target value
        return String.format(
            " and (dr.effectiveDateTime < :%s or :%s < dr.issuedDateTime)",
            lowerBoundPlaceholder(), upperBoundPlaceholder());

      case GT:
        // the range above the search value intersects the range of the target value
        return String.format(" and :%s < dr.issuedDateTime", upperBoundPlaceholder());

      case LT:
        // the range below the search value intersects the range of the target value
        return String.format(" and dr.effectiveDateTime < :%s", lowerBoundPlaceholder());

      case GE:
        // the range above the search value intersects the range of the target value
        // or the range of the search value fully contains the range of the target value
        return String.format(
            " and (:%s <= dr.effectiveDateTime or :%s < dr.issuedDateTime)",
            lowerBoundPlaceholder(), upperBoundPlaceholder());

      case LE:
        // the range below the search value intersects the range of the target value
        // or the range of the search value fully contains the range of the target value
        return String.format(
            " and (dr.effectiveDateTime < :%s or dr.issuedDateTime <= :%s)",
            lowerBoundPlaceholder(), upperBoundPlaceholder());

      case SA:
        // the range of the search value does not intersect the range of the target value
        // and the range above the search value contains the range of the target value
        return String.format(" and :%s < dr.effectiveDateTime", upperBoundPlaceholder());

      case EB:
        // the range of the search value does not intersect the range of the target value,
        // and the range below the search value contains the range of the target value
        return String.format(" and dr.issuedDateTime < :%s", lowerBoundPlaceholder());

      case AP:
        throw new UnsupportedOperationException();

      default:
        throw new IllegalArgumentException();
    }
  }

  private String upperBoundPlaceholder() {
    return "date" + index + "LowerBound";
  }

  private static enum SearchPrefix {
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
