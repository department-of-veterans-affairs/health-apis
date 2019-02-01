package gov.va.health.api.sentinel;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.health.api.sentinel.TestIds.DiagnosticReports;
import gov.va.health.api.sentinel.TestIds.Observations;
import gov.va.health.api.sentinel.TestIds.PersonallyIdentifiableInformation;
import gov.va.health.api.sentinel.TestIds.Procedures;
import gov.va.health.api.sentinel.TestIds.Range;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.NoArgsConstructor;

/** The standard system configurations for typical environments like QA or PROD. */
@NoArgsConstructor(staticName = "get")
public class SystemDefinitions {

  private DiagnosticReports diagnosticReports() {
    return DiagnosticReports.builder()
        .loinc1("10000-8")
        .loinc2("99999-9")
        .onDate("eq1970-01-01")
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
  public SystemDefinition lab() {
    return SystemDefinition.builder()
        .ids(
            ServiceDefinition.builder()
                .url("https://dev-api.va.gov/services/argonaut/v0")
                .port(443)
                .accessToken(noAccessToken())
                .build())
        .mrAnderson(
            ServiceDefinition.builder()
                .url("https://dev-api.va.gov/services/argonaut/v0")
                .port(443)
                .accessToken(noAccessToken())
                .build())
        .argonaut(
            ServiceDefinition.builder()
                .url("https://dev-api.va.gov/services/argonaut/v0")
                .port(443)
                .accessToken(magicAccessToken())
                .build())
        .cdwIds(labAndStagingIds())
        .build();
  }

  private TestIds labAndStagingIds() {
    return TestIds.builder()
        .allergyIntolerance("17a7e128-8cf2-521f-ba99-b5eadb6ca598")
        .condition("31945344-4132-5c41-b8e0-b9df553ee401")
        .diagnosticReport("a7f0ea54-34c9-5d3e-ada9-9b741bb97b88")
        .diagnosticReports(diagnosticReports())
        .immunization("79735eb7-418d-5b31-aa76-6388ecb26422")
        .medication("928e1b50-7bf5-5e66-922d-da5ad2c3f7b4")
        .medicationOrder("07daea10-b1e7-5b00-a6cd-af57acdc97a5")
        .medicationStatement("bee7e47d-a06b-5442-941d-9995852786b4")
        .observation("7db0770e-6a2b-583e-8b70-98bed5ffab93")
        .observations(observations())
        .patient("1017283132V631076")
        .procedure("fd79a9d9-e85b-58b5-b993-b550d5b7802e")
        .procedures(procedures())
        .build();
  }

  /**
   * Return system definitions for local running applications as started by the Maven build process.
   */
  public SystemDefinition local() {
    return SystemDefinition.builder()
        .ids(
            ServiceDefinition.builder()
                .url("https://localhost")
                .port(8089)
                .accessToken(noAccessToken())
                .build())
        .mrAnderson(
            ServiceDefinition.builder()
                .url("https://localhost")
                .port(8088)
                .accessToken(noAccessToken())
                .build())
        .argonaut(
            ServiceDefinition.builder()
                .url("https://localhost")
                .port(8090)
                .accessToken(noAccessToken())
                .build())
        .cdwIds(
            TestIds.builder()
                .publicIds(false)
                .allergyIntolerance("1000001782544")
                .appointment("1200438317388")
                .condition("1400007575530:P")
                .diagnosticReport("1000000031384:L")
                .encounter("1200753214085")
                .diagnosticReports(diagnosticReports())
                .immunization("1000000043979")
                .location("166365:L")
                .medication("212846")
                .medicationDispense("1200738474343:R")
                .medicationOrder("1200389904206:O")
                .medicationStatement("1400000182116")
                .observation("1201051417263:V")
                .observations(observations())
                .organization("1000025431:C")
                .pii(
                    PersonallyIdentifiableInformation.builder()
                        .gender("male")
                        .birthdate("1970-01-01")
                        .given("JOHN Q")
                        .name("VETERAN,JOHN")
                        .family("VETERAN")
                        .build())
                .patient("185601V825290")
                .practitioner("10092125")
                .procedure("1400000140034")
                .procedures(procedures())
                .unknown("5555555555555")
                .build())
        .build();
  }

  /**
   * Checks for system property access-token. Supplies it if it exists and throws an exception if it
   * doesn't.
   */
  public static Supplier<Optional<String>> magicAccessToken() {
    String magic = System.getProperty("access-token");
    if (isBlank(magic)) {
      throw new IllegalStateException("Access token not specified, -Daccess-token=<value>");
    }
    return () -> Optional.of(magic);
  }

  private Supplier<Optional<String>> noAccessToken() {
    return () -> Optional.empty();
  }

  private Observations observations() {
    return Observations.builder()
        .loinc1("72166-2")
        .loinc2("777-3")
        .onDate("2015-04-15")
        .dateRange(Range.allTime())
        .build();
  }

  private Procedures procedures() {
    return Procedures.builder().fromDate("ge2009").onDate("ge2009").toDate("le2010").build();
  }

  /** Return definitions for the production environment. */
  public SystemDefinition prod() {
    return SystemDefinition.builder()
        .ids(
            ServiceDefinition.builder()
                .url("https://argonaut.lighthouse.va.gov")
                .port(443)
                .accessToken(noAccessToken())
                .build())
        .mrAnderson(
            ServiceDefinition.builder()
                .url("https://argonaut.lighthouse.va.gov")
                .port(443)
                .accessToken(noAccessToken())
                .build())
        .argonaut(
            ServiceDefinition.builder()
                .url("https://argonaut.lighthouse.va.gov")
                .port(443)
                .accessToken(magicAccessToken())
                .build())
        .cdwIds(prodAndQaIds())
        .build();
  }

  private TestIds prodAndQaIds() {
    return TestIds.builder()
        .publicIds(true)
        .allergyIntolerance("3be00408-b0ff-598d-8ba1-1e0bbfb02b99")
        .appointment("f7721341-03ad-56cf-b0e5-e96fded23a1b")
        .condition("ea59bc29-d507-571b-a4c6-9ac0d2146c45")
        .diagnosticReport("0bca2c42-8d23-5d36-90b8-81a8b12bb1b5")
        .diagnosticReports(diagnosticReports())
        .encounter("05d66afc-3a1a-5277-8b26-a8084ac46a08")
        .immunization("00f4000a-b1c9-5190-993a-644569d2722b")
        .location("a146313b-9a77-5337-a442-bee6ceb4aa5c")
        .medication("89a46bce-8b95-5a91-bbef-1fb5f8a2a292")
        .medicationDispense("773bb1ab-4430-5012-b203-a88c41c5dde9")
        .medicationOrder("91f4a9d2-e7fa-5b34-a875-6d75761221c7")
        .medicationStatement("e4573ebc-40e4-51bb-9da1-20a91b31ff24")
        .observation("40e2ced6-32e2-503e-85b8-198690f6611b")
        .observations(observations())
        .organization("3e5dbe7a-72ca-5441-9287-0b639ae7a1bc")
        .patient("1011537977V693883")
        .practitioner("7b4c6b83-2c5a-5cbf-836c-875253fb9bf9")
        .procedure("c416df15-fc1d-5a04-ab11-34d7bf453d15")
        .procedures(procedures())
        .unknown("5555555555555")
        .build();
  }

  /** Return definitions for the qa environment. */
  public SystemDefinition qa() {
    return SystemDefinition.builder()
        .ids(
            ServiceDefinition.builder()
                .url("https://qa-argonaut.lighthouse.va.gov")
                .port(443)
                .accessToken(noAccessToken())
                .build())
        .mrAnderson(
            ServiceDefinition.builder()
                .url("https://qa-argonaut.lighthouse.va.gov")
                .port(443)
                .accessToken(noAccessToken())
                .build())
        .argonaut(
            ServiceDefinition.builder()
                .url("https://qa-argonaut.lighthouse.va.gov")
                .port(443)
                .accessToken(magicAccessToken())
                .build())
        .cdwIds(prodAndQaIds())
        .build();
  }

  /** Return definitions for the staging environment. */
  public SystemDefinition staging() {
    return SystemDefinition.builder()
        .ids(
            ServiceDefinition.builder()
                .url("https://staging-argonaut.lighthouse.va.gov")
                .port(443)
                .accessToken(noAccessToken())
                .build())
        .mrAnderson(
            ServiceDefinition.builder()
                .url("https://staging-argonaut.lighthouse.va.gov")
                .port(443)
                .accessToken(noAccessToken())
                .build())
        .argonaut(
            ServiceDefinition.builder()
                .url("https://staging-argonaut.lighthouse.va.gov")
                .port(443)
                .accessToken(magicAccessToken())
                .build())
        .cdwIds(labAndStagingIds())
        .build();
  }
}
