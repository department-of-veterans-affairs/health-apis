package gov.va.api.health.dataquery.tests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Collection of IDs needed by the tests. */
@Value
@Builder(toBuilder = true)
public final class TestIds {
  @NonNull String allergyIntolerance;
  @NonNull String appointment;
  @NonNull String condition;
  @NonNull String device;
  @NonNull String diagnosticReport;
  @NonNull String immunization;
  @NonNull String location;
  @NonNull String medication;
  @NonNull String medicationOrder;
  @NonNull String medicationStatement;
  @NonNull String observation;
  @NonNull String organization;
  @NonNull String patient;
  @NonNull String practitioner;
  @NonNull String practitionerRole;
  @NonNull String procedure;
  @NonNull String unknown;
  @NonNull String uuid;

  @NonNull DiagnosticReports diagnosticReports;
  @NonNull Locations locations;
  @NonNull Observations observations;
  PersonallyIdentifiableInformation pii;
  @NonNull Procedures procedures;
  @NonNull Organizations organizations;
  @NonNull Practitioners practitioners;
  @NonNull Appointments appointments;

  /** Appointment filtering information. */
  @Value
  @Builder
  public static class Appointments {
    @NonNull String date;
    @NonNull String lastUpdated;
    @NonNull String location;
    @NonNull String oauthPatient;
  }

  /** DiagnosticReport filtering information. */
  @Value
  @Builder
  public static class DiagnosticReports {
    @NonNull String loinc1;
    @NonNull String loinc2;
    @NonNull String badLoinc;
    @NonNull String onDate;
    @NonNull String fromDate;
    @NonNull String toDate;
    @NonNull String dateYear;
    @NonNull String dateYearMonth;
    @NonNull String dateYearMonthDay;
    // Invalid Dates
    @NonNull String dateYearMonthDayHour;
    @NonNull String dateYearMonthDayHourMinute;
    @NonNull String dateYearMonthDayHourMinuteSecond;
    //
    @NonNull String dateYearMonthDayHourMinuteSecondTimezone;
    @NonNull String dateYearMonthDayHourMinuteSecondZulu;
    @NonNull String dateGreaterThan;
    @NonNull String dateNotEqual;
    @NonNull String dateStartsWith;
    @NonNull String dateNoPrefix;
    @NonNull String dateEqual;
    @NonNull String dateLessOrEqual;
    @NonNull String dateLessThan;
  }

  /** Identifying information about a location. */
  @Value
  @Builder
  public static class Locations {
    @NonNull String addressStreet;
    @NonNull String addressCity;
    @NonNull String addressState;
    @NonNull String addressPostalCode;
    @NonNull String clinicIdentifier;
    @NonNull String clinicIdentifierUnknown;
    @NonNull String name;
  }

  /** Observation filtering information. */
  @Value
  @Builder
  public static class Observations {
    @NonNull Range dateRange;
    @NonNull String onDate;
    @NonNull String loinc1;
    @NonNull String loinc2;
    @NonNull String badLoinc;
  }

  /** Identifying information about an organization. */
  @Value
  @Builder
  public static class Organizations {
    @NonNull String addressStreet;
    @NonNull String addressCity;
    @NonNull String addressState;
    @NonNull String addressPostalCode;
    @NonNull String facilityId;
    @NonNull String name;
    @NonNull String npi;
  }

  /** PII for a patient. */
  @Value
  @Builder
  public static class PersonallyIdentifiableInformation {
    @NonNull String name;
    @NonNull String given;
    @NonNull String family;
    @NonNull String birthdate;
    @NonNull String gender;
    @NonNull String ssn;
    @NonNull String organization;
  }

  /** Identifying information about a practitioner. */
  @Value
  @Builder
  public static class Practitioners {
    @NonNull String family;
    @NonNull String given;
    @NonNull String npi;
    @NonNull String specialty;
  }

  /** Dates specific to known procedures. */
  @Value
  @Builder
  public static class Procedures {
    @NonNull String onDate;
    @NonNull String fromDate;
    @NonNull String toDate;
  }

  /** A date range. */
  @Value
  @Builder
  @AllArgsConstructor(staticName = "of")
  public static class Range {
    @NonNull String from;
    @NonNull String to;

    public static final Range allTime() {
      return Range.of("gt1970-01-01", "lt2038-01-19");
    }
  }
}
