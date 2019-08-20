package gov.va.api.health.dataquery.tests;

import static gov.va.api.health.sentinel.SentinelProperties.magicAccessToken;

import gov.va.api.health.dataquery.tests.TestIds.DiagnosticReports;
import gov.va.api.health.dataquery.tests.TestIds.Observations;
import gov.va.api.health.dataquery.tests.TestIds.PersonallyIdentifiableInformation;
import gov.va.api.health.dataquery.tests.TestIds.Procedures;
import gov.va.api.health.dataquery.tests.TestIds.Range;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.SentinelProperties;
import gov.va.api.health.sentinel.ServiceDefinition;
import java.util.Optional;
import lombok.experimental.UtilityClass;

/**
 * {@link SystemDefinition}s for different environments. {@link #systemDefinition()} method provides
 * the appropriate implementation for the current environment.
 */
@UtilityClass
public final class SystemDefinitions {

  private static DiagnosticReports diagnosticReports() {
    return DiagnosticReports.builder()
        .loinc1("10000-8")
        .loinc2("10001-6")
        .badLoinc("99999-9")
        .onDate("eq2013-03-21")
        .fromDate("gt1970-01-01")
        .toDate("lt2038-01-01")
        .dateYear("ge1970")
        .dateYearMonth("ge1970-01")
        .dateYearMonthDay("ge1970-01-01")
        .dateYearMonthDayHour("ge1970-01-01T07")
        .dateYearMonthDayHourMinute("ge1970-01-01T07:00")
        .dateYearMonthDayHourMinuteSecond("ge1970-01-01T07:00:00")
        .dateYearMonthDayHourMinuteSecondTimezone("ge1970-01-01T07:00:00+05:00")
        .dateYearMonthDayHourMinuteSecondZulu("ge1970-01-01T07:00:00Z")
        .dateGreaterThan("ge1970-01-01")
        .dateNotEqual("ne1970-01-01")
        .dateStartsWith("sa1970-01-01")
        .dateNoPrefix("1970-01-01")
        .dateEqual("1970-01-01")
        .dateLessOrEqual("le2038-01-19")
        .dateLessThan("lt2038-01-19")
        .build();
  }

  /** Return definitions for the lab environment. */
  private static SystemDefinition lab() {
    String url = "https://dev-api.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .mrAnderson(serviceDefinition("mr-anderson", url, 443, null, "/not-available/"))
        .dataQuery(
            serviceDefinition("argonaut", url, 443, magicAccessToken(), "/services/argonaut/v0/"))
        .cdwIds(labMitreIds())
        .build();
  }

  private static TestIds labMitreIds() {
    return TestIds.builder()
        .publicIds(true)
        .allergyIntolerance("2f7241a3-2f43-58f0-a6e7-ebb85fdf3f84")
        .condition("0a812fa6-318b-5f8e-84fe-828ed8448be4")
        .diagnosticReport("580973dd-2f9a-57ac-b2e4-5897ad1c4322")
        .diagnosticReports(diagnosticReports())
        .immunization("1b350f07-a1ce-5078-bb60-5d0122fbec50")
        .medication("30c08673-77e0-5acd-b334-cd5ba153d86d")
        .medicationOrder("0e4d47c4-dbf1-514b-b0ec-1f29bacaa13b")
        .medicationStatement("08578f3e-17ea-5454-b88a-4706ab54a95f")
        .observation("02ef60c8-ab65-5322-9cbb-db01083ec245")
        .observations(observations())
        .patient("1011537977V693883")
        .procedure("02b9078b-9665-52ff-b360-9d618ac34df0")
        .procedures(localAndLabProcedures())
        .location("unused")
        .appointment("unused")
        .medicationDispense("unused")
        .encounter("unused")
        .organization("unused")
        .practitioner("unused")
        .unknown("5555555555555")
        .build();
  }

  /**
   * Return system definitions for local running applications as started by the Maven build process.
   */
  private static SystemDefinition local() {
    String url = "http://localhost";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 8089, null, "/api/"))
        .mrAnderson(serviceDefinition("mr-anderson", url, 8088, null, "/api/"))
        .dataQuery(serviceDefinition("argonaut", url, 8090, null, "/"))
        .cdwIds(localIds())
        .build();
  }

  private static Procedures localAndLabProcedures() {
    return Procedures.builder().fromDate("ge2009").onDate("ge2009").toDate("le2010").build();
  }

  private static TestIds localIds() {
    return TestIds.builder()
        .publicIds(false)
        .allergyIntolerance("10000020531")
        .appointment("1200438317388")
        .condition("1234567:D")
        .diagnosticReport("1400096309217:L")
        .encounter("1200753214085")
        .diagnosticReports(diagnosticReports())
        .immunization("12345678")
        .location("166365:L")
        .medication("18")
        .medicationDispense("1200738474343:R")
        .medicationOrder("1400162277477:O")
        .medicationStatement("800000000707")
        .observation("1201472002950:V")
        .observations(observations())
        .organization("1000025431:C")
        .pii(
            PersonallyIdentifiableInformation.builder()
                .gender("male")
                .birthdate("1948-06-28")
                .given("Conrad619")
                .name("Olson653, Conrad619")
                .family("Olson653")
                .build())
        .patient("111222333V000999")
        .practitioner("10092125")
        .procedure("1000001259996")
        .procedures(localAndLabProcedures())
        .unknown("5555555555555")
        .build();
  }

  private static Observations observations() {
    return Observations.builder()
        .loinc1("72166-2")
        .loinc2("777-3")
        .badLoinc("99999-9")
        .onDate("2015-04-15")
        .dateRange(Range.allTime())
        .build();
  }

  /** Return definitions for the production environment. */
  private static SystemDefinition prod() {
    // Mr Anderson not accessible in this environment
    String url = "https://api.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .mrAnderson(serviceDefinition("mr-anderson", url, 443, null, "/not-available/"))
        .dataQuery(
            serviceDefinition("argonaut", url, 443, magicAccessToken(), "/services/argonaut/v0/"))
        .cdwIds(productionCdwIds())
        .build();
  }

  private static TestIds productionCdwIds() {
    return TestIds.builder()
        .publicIds(true)
        .allergyIntolerance("3be00408-b0ff-598d-8ba1-1e0bbfb02b99")
        .appointment("f7721341-03ad-56cf-b0e5-e96fded23a1b")
        .condition("e4bc4b8f-a51d-58aa-b62a-6b1e6a02a22b")
        .diagnosticReport("708d00e3-c753-50c9-9da6-6d87ce618f0b")
        .diagnosticReports(diagnosticReports())
        .encounter("05d66afc-3a1a-5277-8b26-a8084ac46a08")
        .immunization("3c009e39-7960-56d6-8597-fb1f9d169360")
        .location("a146313b-9a77-5337-a442-bee6ceb4aa5c")
        .medication("89a46bce-8b95-5a91-bbef-1fb5f8a2a292")
        .medicationDispense("773bb1ab-4430-5012-b203-a88c41c5dde9")
        .medicationOrder("91f4a9d2-e7fa-5b34-a875-6d75761221c7")
        .medicationStatement("4cb8152a-67aa-569c-bc30-9e672b5c72fd")
        .observation("40e2ced6-32e2-503e-85b8-198690f6611b")
        .observations(observations())
        .organization("3e5dbe7a-72ca-5441-9287-0b639ae7a1bc")
        .patient("1011537977V693883")
        .practitioner("7b4c6b83-2c5a-5cbf-836c-875253fb9bf9")
        .procedure("6f260748-c3be-5333-9f90-a2d429249c7f")
        .procedures(productionCdwProcedures())
        .unknown("5555555555555")
        .build();
  }

  private static Procedures productionCdwProcedures() {
    return Procedures.builder().fromDate("ge2009").onDate("ge2009").toDate("le2014").build();
  }

  /** Return definitions for the qa environment. */
  private static SystemDefinition qa() {
    // ID service and Mr Anderson not accessible in this environment
    String url = "https://blue.qa.lighthouse.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .mrAnderson(serviceDefinition("mr-anderson", url, 443, null, "/not-available/"))
        .dataQuery(serviceDefinition("argonaut", url, 443, magicAccessToken(), "/"))
        .cdwIds(productionCdwIds())
        .build();
  }

  private static ServiceDefinition serviceDefinition(
      String name, String url, int port, String accessToken, String apiPath) {
    return ServiceDefinition.builder()
        .url(SentinelProperties.optionUrl(name, url))
        .port(port)
        .accessToken(() -> Optional.ofNullable(accessToken))
        .apiPath(SentinelProperties.optionApiPath(name, apiPath))
        .build();
  }

  /** Return definitions for the staging environment. */
  private static SystemDefinition staging() {
    // ID service and Mr Anderson not accessible in this environment
    String url = "https://blue.staging.lighthouse.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .mrAnderson(serviceDefinition("mr-anderson", url, 443, null, "/not-available/"))
        .dataQuery(serviceDefinition("argonaut", url, 443, magicAccessToken(), "/"))
        .cdwIds(productionCdwIds())
        .build();
  }

  /** Return definitions for the lab environment. */
  private static SystemDefinition stagingLab() {
    String url = "https://blue.staging-lab.lighthouse.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .mrAnderson(serviceDefinition("mr-anderson", url, 443, null, "/not-available/"))
        .dataQuery(
            serviceDefinition("argonaut", url, 443, magicAccessToken(), "/services/argonaut/v0/"))
        .cdwIds(labMitreIds())
        .build();
  }

  /** Return the applicable system definition for the current environment. */
  public static SystemDefinition systemDefinition() {
    switch (Environment.get()) {
      case LAB:
        return lab();
      case LOCAL:
        return local();
      case PROD:
        return prod();
      case QA:
        return qa();
      case STAGING:
        return staging();
      case STAGING_LAB:
        return stagingLab();
      default:
        throw new IllegalArgumentException("Unknown sentinel environment: " + Environment.get());
    }
  }
}
