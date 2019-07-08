package gov.va.api.health.dataquery.service.controller;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import lombok.Value;

@Value
public final class DateTimeParameters {
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
          String.format("'{}' is not a valid date-time parameter", paramString));
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

  /** Indicates if the date range satisfies this date-time parameter. */
  public boolean isSatisfied(long lower, long upper) {
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
