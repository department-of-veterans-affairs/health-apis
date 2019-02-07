package gov.va.health.api.sentinel;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Collection of IDs needed by the tests. */
@Value
@Builder(toBuilder = true)
public class TestIds {
  boolean publicIds;
  @NonNull String allergyIntolerance;
  @NonNull String appointment;
  @NonNull String condition;
  @NonNull String diagnosticReport;
  @NonNull String encounter;
  @NonNull String immunization;
  @NonNull String location;
  @NonNull String medication;
  @NonNull String medicationDispense;
  @NonNull String medicationOrder;
  @NonNull String medicationStatement;
  @NonNull String observation;
  @NonNull String organization;
  @NonNull String patient;
  @NonNull String practitioner;
  @NonNull String procedure;
  @NonNull String unknown;

  @NonNull DiagnosticReports diagnosticReports;
  @NonNull Observations observations;
  PersonallyIdentifiableInformation pii;
  @NonNull Procedures procedures;

  @Value
  @Builder
  public static class DiagnosticReports {
    @NotNull String loinc1;
    @NotNull String loinc2;
    @NotNull String badLoinc;
    @NotNull String onDate;
    @NotNull String fromDate;
    @NotNull String toDate;
    @NotNull String dateYear;
    @NotNull String dateYearMonth;
    @NotNull String dateYearMonthDay;
    // Invalid Dates
    @NotNull String dateYearMonthDayHour;
    @NotNull String dateYearMonthDayHourMinute;
    @NotNull String dateYearMonthDayHourMinuteSecond;
    //
    @NotNull String dateYearMonthDayHourMinuteSecondTimezone;
    @NotNull String dateYearMonthDayHourMinuteSecondZulu;
    @NotNull String dateGreaterThan;
    @NotNull String dateNotEqual;
    @NotNull String dateStartsWith;
    @NotNull String dateNoPrefix;
    @NotNull String dateEqual;
    @NotNull String dateLessOrEqual;
    @NotNull String dateLessThan;
  }

  @Value
  @Builder
  public static class Observations {
    @NonNull Range dateRange;
    @NonNull String onDate;
    @NonNull String loinc1;
    @NonNull String loinc2;
    @NonNull String badLoinc;
  }

  @Value
  @Builder
  public static class PersonallyIdentifiableInformation {
    @NonNull String name;
    @NonNull String given;
    @NonNull String family;
    @NonNull String birthdate;
    @NonNull String gender;
  }

  @Value
  @Builder
  public static class Procedures {
    @NonNull String onDate;
    @NonNull String fromDate;
    @NonNull String toDate;
  }

  @Value
  @Builder
  @AllArgsConstructor(staticName = "of")
  public static class Range {
    @NotNull String from;
    @NotNull String to;

    public static final Range allTime() {
      return Range.of("gt1970-01-01", "lt2038-01-19");
    }
  }
}
