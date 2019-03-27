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
  private static final int YEAR = 4;

  private static final int YEAR_MONTH = 7;

  private static final int YEAR_MONTH_DAY = 10;

  private static final int TIME_ZONE = 20;

  private static final int TIME_ZONE_OFFSET = 25;

  int index;

  SearchPrefix prefix;

  String date;

  @Builder
  private JpaDateTimeParameter(int index, String paramString) {
    super();
    if (paramString.length() <= 1) {
      throw new IllegalArgumentException(
          String.format("'{}' is not a valid date-time parameter", paramString));
    }
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
    if (dateParams == null) {
      return;
    }
    for (int i = 0; i < dateParams.size(); i++) {
      JpaDateTimeParameter.builder()
          .index(i)
          .paramString(dateParams.get(i))
          .build()
          .addQueryParameters(query);
    }
  }

  private static UnsupportedOperationException apException() {
    return new UnsupportedOperationException("AP search prefix not implemented");
  }

  /** Build a combined JPA query snippet representing all the date criteria. */
  public static String querySnippet(String[] dates) {
    if (dates == null) {
      return "";
    }

    StringBuilder querySnippet = new StringBuilder();
    for (int i = 0; i < dates.length; i++) {
      querySnippet.append(
          JpaDateTimeParameter.builder().index(i).paramString(dates[i]).build().toQuerySnippet());
    }
    return querySnippet.toString();
  }

  private void addQueryParameters(TypedQuery<?> query) {
    Instant lowerBound = lowerBound();
    Instant upperBound = upperBound();
    log.info("Date {} has bounds: [{}, {}]", date(), lowerBound, upperBound);

    switch (prefix()) {
      case EQ:
        // falls through
      case NE:
        // falls through
      case GE:
        // falls through
      case LE:
        query.setParameter(placeholderLowerBound(), lowerBound);
        query.setParameter(placeholderUpperBound(), upperBound);
        return;

      case GT:
        // falls through
      case SA:
        query.setParameter(placeholderUpperBound(), upperBound);
        return;

      case LT:
        // falls through
      case EB:
        query.setParameter(placeholderLowerBound(), lowerBound);
        return;

      case AP:
        throw apException();

      default:
        throw new IllegalArgumentException("Unknown search prefix: " + prefix());
    }
  }

  private Instant lowerBound() {
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
        // the range above the search value intersects the range of the target value
        // or the range of the search value fully contains the range of the target value
        return String.format(
            " and (:%s <= dr.effectiveDateTime or :%s < dr.issuedDateTime)",
            placeholderLowerBound(), placeholderUpperBound());

      case LE:
        // the range below the search value intersects the range of the target value
        // or the range of the search value fully contains the range of the target value
        return String.format(
            " and (dr.effectiveDateTime < :%s or dr.issuedDateTime <= :%s)",
            placeholderLowerBound(), placeholderUpperBound());

      case SA:
        // the range of the search value does not intersect the range of the target value
        // and the range above the search value contains the range of the target value
        return String.format(" and :%s < dr.effectiveDateTime", placeholderUpperBound());

      case EB:
        // the range of the search value does not intersect the range of the target value,
        // and the range below the search value contains the range of the target value
        return String.format(" and dr.issuedDateTime < :%s", placeholderLowerBound());

      case AP:
        throw apException();

      default:
        throw new IllegalArgumentException("Unknown search prefix: " + prefix());
    }
  }

  private Instant upperBound() {
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
