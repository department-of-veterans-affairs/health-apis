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
        .internalDataQuery(serviceDefinition("internal", url, 443, null, "/not-available/"))
        .cdwIds(labMitreIds())
        .build();
  }

  private static TestIds labMitreIds() {
    return TestIds.builder()
        .publicIds(true)
        .allergyIntolerance("I2-5XYSWFRZ637QKNR6IIRKYHA5RY000000")
        .condition("I2-FOBJ7YQOH3RIQ5UZ6TRM32ZSQA000000")
        .diagnosticReport("I2-3ACWF6E3HPG6GLOSVWR2CIQNPI000000")
        .diagnosticReports(diagnosticReports())
        .immunization("I2-55SQNNDBJUHYLVNXKTTYZSIVQE000000")
        .medication("I2-Q6VHYRTPQZ755P7JKKFUU5Q4TM000000")
        .medicationOrder("I2-J3UNHOOTERVSTBX4RMTN6MAMQ4000000")
        .medicationStatement("I2-AKEI5ITNUR5DGUNZXC33PYWXKU000000")
        .observation("I2-TSP35ALBRP4GSCBKRIWDO5CA54000000")
        .observations(observations())
        .patient("1011537977V693883")
        .procedure("I2-J2OUEVFHKESKUKIALZPTDTJNMQ000000")
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
        .dataQuery(serviceDefinition("argonaut", url, 8090, null, "/dstu2/"))
        .internalDataQuery(serviceDefinition("internal", url, 8090, null, "/"))
        .cdwIds(localIds())
        .build();
  }

  private static Procedures localAndLabProcedures() {
    return Procedures.builder().fromDate("ge2009").onDate("ge2009").toDate("le2010").build();
  }

  private static TestIds localIds() {
    /* IDS encoded with key: data-query */
    return TestIds.builder()
        .publicIds(true)
        .allergyIntolerance("I2-6PEP3VSTE3TIHUPLHXRZBG4QTY000000")
        .appointment("I2-YFGSH7XZIDAPOYL24GECYKVG5MXYAY6D5AXK24LLFDIFP6GD4LGQ0000")
        .condition("I2-NHQ2GKYCVNIOUULQCYTK2K6EQ4000000")
        .diagnosticReport("I2-NVJU4EWW3YBUEM2EFYP6VYA4JM000000")
        .encounter("I2-U4SUMY5UCFBIDMJBTD6AQB37XHD5G56UK4TESCKITDDOQ5YU7XJA0000")
        .diagnosticReports(diagnosticReports())
        .immunization("I2-SUIW57VEBLELRLBDYF3LKXB5ZA000000")
        .location("I2-K7WNFKZA3JCXL3CLT6D2HP7RRU000000")
        .medication("I2-EMFL5CBY25CCZPXLHVMM4JEOX4000000")
        .medicationDispense("I2-22CRMSOEQUG7QIPH4DLC4V5FFH2C5OZRTCGW2QDOCFD5SESUFKUQ0000")
        .medicationOrder("I2-LM6LHSWIRQPLNRO5XUKAQUXWI4000000")
        .medicationStatement("I2-CRBOB5CEO2YTFDNYTAGAUCREVA000000")
        .observation("I2-2RCKPYB63RBIONGQCHJKHWZCJY000000")
        .observations(observations())
        .organization("I2-CU32H73D562BVWKWLSRRHBY4WFWYJ7447EZD5HWII6CKS6YQZCTA0000")
        .pii(
            PersonallyIdentifiableInformation.builder()
                .gender("female")
                .birthdate("1998-01-26")
                .given("Carlita746")
                .name("Ms. Carlita746 Kautzer186")
                .family("Kautzer186")
                .build())
        .patient("43000199")
        .practitioner("I2-WDRQEV46TZAZCKF236GEVA7JQ4000000")
        .procedure("I2-JJ3KKRP45LEYYEEMLIWYBE473U000000")
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
        .internalDataQuery(serviceDefinition("internal", url, 443, null, "/not-available/"))
        .cdwIds(productionCdwIds())
        .build();
  }

  private static TestIds productionCdwIds() {
    return TestIds.builder()
        .publicIds(true)
        .allergyIntolerance("I2-VEQLSCH5XMYBN3EIBIQ3LDWDUJMUVKKKUB5Z4E7QCYESQ53FMOJA0000")
        .appointment("I2-XPTA6C5Q7GP6JR25UNTMQGY6P5WYMMLMCP2YVFDGOTPCPHQXDH3A0000")
        .condition("I2-H7TWOL6IPU27YRF3OKZIUJM5D27UCDVBMBWSONEYQ66OTFL4OVYQ0000")
        .diagnosticReport("I2-M2QUOOXL3O73NUZCB7HEOVQ2GAGQFOATAYXW5FMU3I57IYQDE6RQ0000")
        .diagnosticReports(diagnosticReports())
        .encounter("I2-KC33Z5CFSUOVTRCIVKQ2JUB2ESFWMFFVGIFOIESBWOITMOPLFP7A0000")
        .immunization("I2-LR6MEWBUXWJGD75WXF5BFXXTTLTYR3S3AHUTW55G25J4UOG3ZQIQ0000")
        .location("I2-WEIZUDRRQFULJACUVBXZO7EFOU000000")
        .medication("I2-H6VWTWQS5U454XKHOM6ZTUPCHA000000")
        .medicationDispense("I2-UJSIEUXIDQ6PNNMJMNG44VOCWNOUCKZJJ5SWPN2G2XOWVAHIPYEQ0000")
        .medicationOrder("I2-IB456XUS7OJUVJBC5ESLW3IZ2R6773XSYHA7V63BLTV6YSG4QJ6A0000")
        .medicationStatement("I2-EIQB74V2APLMGKQJPRRT7LIPABT43MYPA2TEUW36N6BTEAJC65RA0000")
        .observation("I2-QSUC3WVCAOC7PWYON5HMETFYBQWCULOIQWLKHG6OP3DXH7M7MUTQ0000")
        .observations(observations())
        .organization("I2-MTZXK35OVTTKNYEEL4MVCPPGJM000000")
        .patient("1011537977V693883")
        .practitioner("I2-NCVNJO4N54ES6ZLYLYKKNFORRY000000")
        .procedure("I2-DTO3TGZDH7VWRWL6CVHS4NTMTUBWUNA5D4U2L45RJJ3LG6LY6XXA0000")
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
        .internalDataQuery(serviceDefinition("internal", url, 443, null, "/data-query/"))
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
        .internalDataQuery(serviceDefinition("internal", url, 443, null, "/data-query/"))
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
        .internalDataQuery(serviceDefinition("internal", url, 443, null, "/data-query/"))
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
