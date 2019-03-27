package gov.va.api.health.dataquery.service.controller;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
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
  String name;

  SearchPrefix prefix;

  Instant lowerBound;

  Instant upperBound;

  @Builder
  private JpaDateTimeParameter(String name, String paramString) {
    super();
    this.name = name;
    if (Character.isLetter(paramString.charAt(0))) {
      prefix = SearchPrefix.valueOf(paramString.substring(0, 2).toUpperCase(Locale.US));
      lowerBound = computeLowerBound(paramString.substring(2));
      log.error("lower bound is " + lowerBound);
      upperBound = computeUpperBound(paramString.substring(2));
      log.error("upper bound is " + upperBound);
    } else {
      prefix = SearchPrefix.EQ;
      lowerBound = computeLowerBound(paramString);
      log.error("lower bound is " + lowerBound);
      upperBound = computeUpperBound(paramString);
      log.error("upper bound is " + upperBound);
    }
  }

  public static void addQueryParameters(TypedQuery<?> query, List<String> dateParams) {
    if (dateParams == null || dateParams.isEmpty()) {
      return;
    }
    JpaDateTimeParameter.builder()
        .name("date0")
        .paramString(dateParams.get(0))
        .build()
        .addQueryParameters(query);
    if (dateParams.size() >= 2) {
      JpaDateTimeParameter.builder()
          .name("date1")
          .paramString(dateParams.get(1))
          .build()
          .addQueryParameters(query);
    }
  }

  public static String querySnippet(String[] dates) {
    if (dates == null || dates.length == 0) {
      return "";
    }
    if (dates.length == 1) {
      return JpaDateTimeParameter.builder()
          .name("date0")
          .paramString(dates[0])
          .build()
          .querySnippet();
    }
    return JpaDateTimeParameter.builder().name("date0").paramString(dates[0]).build().querySnippet()
        + JpaDateTimeParameter.builder().name("date1").paramString(dates[1]).build().querySnippet();
  }

  private void addQueryParameters(TypedQuery<?> query) {
    switch (prefix()) {
      case EQ:
      case NE:
        query.setParameter(lowerBoundPlaceholder(), lowerBound());
        query.setParameter(upperBoundPlaceholder(), upperBound());
        return;
      case GT:
        query.setParameter(upperBoundPlaceholder(), upperBound());
        return;
      case LT:
      case GE:
      case LE:
      case SA:
      case EB:
      case AP:
      default:
        throw new IllegalArgumentException();
    }
  }

  private Instant computeLowerBound(String paramString) {
    log.error("Computing lower bound for " + paramString);
    switch (paramString.length()) {
      case 4:
        return OffsetDateTime.parse(
                String.format(
                    "%s-01-01T00:00:00.000%s", paramString, ZonedDateTime.now().getOffset()))
            .toInstant();
      case 7:
        // .dateYearMonth("1970-01")
      case 10:
        // .dateYearMonthDay("1970-01-01")
      case 13:
        // .dateYearMonthDayHour("1970-01-01T07")
      case 16:
        // .dateYearMonthDayHourMinute("1970-01-01T07:00")
      case 19:
        // .dateYearMonthDayHourMinuteSecond("1970-01-01T07:00:00")
      case 20:
        // .dateYearMonthDayHourMinuteSecondZulu("1970-01-01T07:00:00Z")
      case 25:
        // .dateYearMonthDayHourMinuteSecondTimezone("1970-01-01T07:00:00+05:00")
      default:
        throw new IllegalArgumentException();
    }
  }

  private Instant computeUpperBound(String paramString) {
    log.error("Computing upper bound for " + paramString);
    switch (paramString.length()) {
      case 4:
        return OffsetDateTime.parse(
                String.format(
                    "%s-12-31T23:59:59.999%s", paramString, ZonedDateTime.now().getOffset()))
            .toInstant();
      case 7:
        // .dateYearMonth("1970-01")
      case 10:
        // .dateYearMonthDay("1970-01-01")
      case 13:
        // .dateYearMonthDayHour("1970-01-01T07")
      case 16:
        // .dateYearMonthDayHourMinute("1970-01-01T07:00")
      case 19:
        // .dateYearMonthDayHourMinuteSecond("1970-01-01T07:00:00")
      case 20:
        // .dateYearMonthDayHourMinuteSecondZulu("1970-01-01T07:00:00Z")
      case 25:
        // .dateYearMonthDayHourMinuteSecondTimezone("1970-01-01T07:00:00+05:00")
      default:
        throw new IllegalArgumentException();
    }
  }

  private String lowerBoundPlaceholder() {
    return name + "UpperBound";
  }

  private String querySnippet() {
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
      case GE:
      case LE:
      case SA:
      case EB:
      case AP:
      default:
        throw new IllegalArgumentException();
    }
  }

  private String upperBoundPlaceholder() {
    return name + "LowerBound";
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
