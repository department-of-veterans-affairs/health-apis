package gov.va.api.health.dataquery.service.controller;

import java.time.Instant;
import java.util.List;
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

  DateTimeParameters parameter;

  @Builder
  private JpaDateTimeParameter(int index, String paramString) {
    super();
    this.index = index;
    this.parameter = new DateTimeParameters(paramString);
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

  /** Build a combined JPA query snippet representing all the date criteria. */
  public static String querySnippet(String[] dates, String lowerTimeField, String higherTimeField) {
    if (dates == null) {
      return "";
    }

    StringBuilder querySnippet = new StringBuilder();
    for (int i = 0; i < dates.length; i++) {
      querySnippet.append(
          JpaDateTimeParameter.builder()
              .index(i)
              .paramString(dates[i])
              .build()
              .toQuerySnippet(lowerTimeField, higherTimeField));
    }
    return querySnippet.toString();
  }

  private void addQueryParameters(TypedQuery<?> query) {
    Instant lowerBound = parameter.lowerBound();
    Instant upperBound = parameter.upperBound();
    log.info("Date {} has bounds: [{}, {}]", parameter.date(), lowerBound, upperBound);

    switch (parameter.prefix()) {
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
        throw DateTimeParameters.apException();

      default:
        throw new IllegalArgumentException("Unknown search prefix: " + parameter.prefix());
    }
  }

  private String placeholderLowerBound() {
    return "date" + index + "LowerBound";
  }

  private String placeholderUpperBound() {
    return "date" + index + "UpperBound";
  }

  private String toQuerySnippet(String lowerTimeField, String higherTimeField) {
    switch (parameter.prefix()) {
      case EQ:
        // the range of the search value fully contains the range of the target value
        return String.format(
            " and :%s <= %s and %s <= :%s",
            placeholderLowerBound(), lowerTimeField, higherTimeField, placeholderUpperBound());

      case NE:
        // the range of the search value does not fully contain the range of the target value
        return String.format(
            " and (%s < :%s or :%s < %s)",
            lowerTimeField, placeholderLowerBound(), placeholderUpperBound(), higherTimeField);

      case GT:
        // the range above the search value intersects the range of the target value
        return String.format(" and :%s < %s", placeholderUpperBound(), higherTimeField);

      case LT:
        // the range below the search value intersects the range of the target value
        return String.format(" and %s < :%s", lowerTimeField, placeholderLowerBound());

      case GE:
        // the range above the search value intersects the range of the target value
        // or the range of the search value fully contains the range of the target value
        return String.format(
            " and (:%s <= %s or :%s < %s)",
            placeholderLowerBound(), lowerTimeField, placeholderUpperBound(), higherTimeField);

      case LE:
        // the range below the search value intersects the range of the target value
        // or the range of the search value fully contains the range of the target value
        return String.format(
            " and (%s < :%s or %s <= :%s)",
            lowerTimeField, placeholderLowerBound(), higherTimeField, placeholderUpperBound());

      case SA:
        // the range of the search value does not intersect the range of the target value
        // and the range above the search value contains the range of the target value
        return String.format(" and :%s < %s", placeholderUpperBound(), lowerTimeField);

      case EB:
        // the range of the search value does not intersect the range of the target value,
        // and the range below the search value contains the range of the target value
        return String.format(" and %s < :%s", higherTimeField, placeholderLowerBound());

      case AP:
        throw DateTimeParameters.apException();

      default:
        throw new IllegalArgumentException("Unknown search prefix: " + parameter.prefix());
    }
  }
}
