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

  String date;

  @Builder
  private JpaDateTimeParameter(int index, String paramString) {
    super();
    this.index = index;
    if (Character.isLetter(paramString.charAt(0))) {
      prefix = SearchPrefix.valueOf(paramString.substring(0, 2).toUpperCase(Locale.US));
      date = paramString.substring(2);
    } else {
      prefix = SearchPrefix.EQ;
      date = paramString;
    }
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
    Instant lowerBound = lowerBound();
    Instant upperBound = upperBound();
    log.info("Date {} has bounds: [{}, {}]", date(), lowerBound, upperBound);
    switch (prefix()) {
      case EQ:
      case NE:
      case GE:
      case LE:
        query.setParameter(placeholderLowerBound(), lowerBound);
        query.setParameter(placeholderUpperBound(), upperBound);
        return;
      case GT:
      case SA:
        query.setParameter(placeholderUpperBound(), upperBound);
        return;
      case LT:
      case EB:
        query.setParameter(placeholderLowerBound(), lowerBound);
        return;
      case AP:
        throw new UnsupportedOperationException();
      default:
        throw new IllegalArgumentException();
    }
  }

  private Instant lowerBound() {
    ZoneOffset offset = ZonedDateTime.now().getOffset();
    switch (date().length()) {
      case 4:
        return OffsetDateTime.parse(String.format("%s-01-01T00:00:00%s", date(), offset))
            .toInstant();
      case 7:
        return OffsetDateTime.parse(String.format("%s-01T00:00:00%s", date(), offset)).toInstant();
      case 10:
        return OffsetDateTime.parse(String.format("%sT00:00:00%s", date(), offset)).toInstant();
      case 13:
        return OffsetDateTime.parse(String.format("%s:00:00%s", date(), offset)).toInstant();
      case 16:
        return OffsetDateTime.parse(String.format("%s:00%s", date(), offset)).toInstant();
      case 19:
        return OffsetDateTime.parse(date() + offset).toInstant();
      case 20:
        // falls through
      case 25:
        return OffsetDateTime.parse(date()).toInstant();
      default:
        throw new IllegalArgumentException();
    }
  }

  private String placeholderLowerBound() {
    return "date" + index + "LowerBound";
  }

  private String placeholderUpperBound() {
    return "date" + index + "UpperBound";
  }

  private String toQuerySnippet() {
    switch (prefix()) {
      case EQ:
        // the range of the search value fully contains the range of the target value
        return String.format(
            " and :%s <= dr.effectiveDateTime and dr.issuedDateTime <= :%s",
            placeholderLowerBound(), placeholderUpperBound());
      case NE:
        // the range of the search value does not fully contain the range of the target value
        return String.format(
            " and (dr.effectiveDateTime < :%s or :%s < dr.issuedDateTime)",
            placeholderLowerBound(), placeholderUpperBound());
      case GT:
        // the range above the search value intersects the range of the target value
        return String.format(" and :%s < dr.issuedDateTime", placeholderUpperBound());
      case LT:
        // the range below the search value intersects the range of the target value
        return String.format(" and dr.effectiveDateTime < :%s", placeholderLowerBound());
      case GE:
        // or the range of the search value fully contains the range of the target value
        return String.format(
            " and (:%s <= dr.effectiveDateTime or :%s < dr.issuedDateTime)",
            placeholderLowerBound(), placeholderUpperBound());
      case LE:
        // or the range of the search value fully contains the range of the target value
        return String.format(
            " and (dr.effectiveDateTime < :%s or dr.issuedDateTime <= :%s)",
            placeholderLowerBound(), placeholderUpperBound());
      case SA:
        // and the range above the search value contains the range of the target value
        return String.format(" and :%s < dr.effectiveDateTime", placeholderUpperBound());
      case EB:
        // and the range below the search value contains the range of the target value
        return String.format(" and dr.issuedDateTime < :%s", placeholderLowerBound());
      case AP:
        throw new UnsupportedOperationException();
      default:
        throw new IllegalArgumentException();
    }
  }

  private Instant upperBound() {
    OffsetDateTime lowerBound =
        OffsetDateTime.ofInstant(lowerBound(), ZonedDateTime.now().getOffset());
    switch (date().length()) {
      case 4:
        return lowerBound.plusYears(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 7:
        return lowerBound.plusMonths(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 10:
        return lowerBound.plusDays(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 13:
        return lowerBound.plusHours(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 16:
        return lowerBound.plusMinutes(1).minus(1, ChronoUnit.MILLIS).toInstant();
      case 19:
        // falls through
      case 20:
        // falls through
      case 25:
        return lowerBound.plusSeconds(1).minus(1, ChronoUnit.MILLIS).toInstant();
      default:
        throw new IllegalArgumentException();
    }
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
