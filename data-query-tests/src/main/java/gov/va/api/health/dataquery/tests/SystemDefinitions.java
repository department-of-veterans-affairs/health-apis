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
    String url = "https://sandbox-api.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .dstu2DataQuery(
            serviceDefinition("dstu2", url, 443, magicAccessToken(), "/services/fhir/v0/dstu2/"))
        .stu3DataQuery(
            serviceDefinition("stu3", url, 443, magicAccessToken(), "/services/fhir/v0/stu3/"))
        .internalDataQuery(serviceDefinition("internal", url, 443, null, "/not-available/"))
        .cdwIds(labIds())
        .build();
  }

  private static TestIds labIds() {
    return TestIds.builder()
        .publicIds(true)
        .allergyIntolerance("I2-5XYSWFRZ637QKNR6IIRKYHA5RY000000")
        .condition("I2-FOBJ7YQOH3RIQ5UZ6TRM32ZSQA000000")
        .diagnosticReport("I2-3ACWF6E3HPG6GLOSVWR2CIQNPI000000")
        .diagnosticReports(diagnosticReports())
        .immunization("I2-55SQNNDBJUHYLVNXKTTYZSIVQE000000")
        .location("I2-2FPCKUIXVR7RJLLG34XVWGZERM000000")
        .locations(localAndLabLocations())
        .medication("I2-Q6VHYRTPQZ755P7JKKFUU5Q4TM000000")
        .medicationOrder("I2-J3UNHOOTERVSTBX4RMTN6MAMQ4000000")
        .medicationStatement("I2-AKEI5ITNUR5DGUNZXC33PYWXKU000000")
        .observation("I2-TSP35ALBRP4GSCBKRIWDO5CA54000000")
        .observations(observations())
        .organization("I2-AKOTGEFSVKFJOPUKHIVJAH5VQU000000")
        .organizations(localAndLabOrganizations())
        .patient("1011537977V693883")
        .practitioner("I2-DBBL6FFRELZQJ64PWDI7FIVXIU000000")
        .practitioners(localAndLabPractitioners())
        .procedure("I2-J2OUEVFHKESKUKIALZPTDTJNMQ000000")
        .procedures(localAndLabProcedures())
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
        .dstu2DataQuery(serviceDefinition("dstu2", url, 8090, null, "/dstu2/"))
        .stu3DataQuery(serviceDefinition("stu3", url, 8090, null, "/stu3/"))
        .internalDataQuery(serviceDefinition("internal", url, 8090, null, "/"))
        .cdwIds(localIds())
        .build();
  }

  private static TestIds.Locations localAndLabLocations() {
    return TestIds.Locations.builder()
        .name("TEM MH PSO TRS IND93EH 3")
        .addressCity("TEMPLE")
        .addressPostalCode("76504")
        .addressState("TEXAS")
        .addressStreet("1901 VETERANS MEMORIAL DRIVE")
        .build();
  }

  private static TestIds.Organizations localAndLabOrganizations() {
    return TestIds.Organizations.builder()
        .addressStreet("10 MONROE AVE, SUITE 6B PO BOX 4160")
        .addressCity("NEW AMSTERDAM")
        .addressState("OH")
        .addressPostalCode("44444-4160")
        .name("NEW AMSTERDAM CBOC")
        .npi("http://hl7.org/fhir/sid/us-npi|1205983228")
        .build();
  }

  private static TestIds.Practitioners localAndLabPractitioners() {
    return TestIds.Practitioners.builder()
        .family("LASTNAME")
        .given("FIRSTNAME A.")
        .npi("http://hl7.org/fhir/sid/us-npi|1932127842")
        .specialty("http://hl7.org/fhir/practitioner-specialty|207Q00000X")
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
        .condition("I2-NHQ2GKYCVNIOUULQCYTK2K6EQ4000000")
        .diagnosticReport("I2-NVJU4EWW3YBUEM2EFYP6VYA4JM000000")
        .diagnosticReports(diagnosticReports())
        .immunization("I2-SUIW57VEBLELRLBDYF3LKXB5ZA000000")
        .location("I2-K7WNFKZA3JCXL3CLT6D2HP7RRU000000")
        .locations(localAndLabLocations())
        .medication("I2-EMFL5CBY25CCZPXLHVMM4JEOX4000000")
        .medicationOrder("I2-LM6LHSWIRQPLNRO5XUKAQUXWI4000000")
        .medicationStatement("I2-CRBOB5CEO2YTFDNYTAGAUCREVA000000")
        .observation("I2-2RCKPYB63RBIONGQCHJKHWZCJY000000")
        .observations(observations())
        .organization("I2-WOKLYQ64CJR6Q5P26N2VPSP7NY000000")
        .organizations(localAndLabOrganizations())
        .pii(
            PersonallyIdentifiableInformation.builder()
                .gender("female")
                .birthdate("1998-01-26")
                .given("Carlita746")
                .name("Ms. Carlita746 Kautzer186")
                .family("Kautzer186")
                .build())
        .patient("43000199")
        .practitioner("I2-TVUBUQIWCJ6NIPURPDPGIYLLLU000000")
        .practitioners(localAndLabPractitioners())
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
    String url = "https://api.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .dstu2DataQuery(
            serviceDefinition("dstu2", url, 443, magicAccessToken(), "/services/fhir/v0/dstu2/"))
        .stu3DataQuery(
            serviceDefinition("stu3", url, 443, magicAccessToken(), "/services/fhir/v0/stu3/"))
        .internalDataQuery(serviceDefinition("internal", url, 443, null, "/not-available/"))
        .cdwIds(productionCdwIds())
        .build();
  }

  /*
   * NOTES:
   * - Organization: The following Organization is real (Orlando VAMC).
   * - Practitioner: The following is a real Practitioner working at the Orlando VAMC.
   *     If test breaks, they may no longer work at the Orlando VAMC and will need to be replaced.
   */
  private static TestIds productionCdwIds() {
    return TestIds.builder()
        .publicIds(true)
        .allergyIntolerance("I2-A6U4FCERBNSVAFYF6CMUOOHMBPJOJFVSJAWGW5TYE3EOC6TQ2OBQ0000")
        .condition("I2-H7TWOL6IPU27YRF3OKZIUJM5D27UCDVBMBWSONEYQ66OTFL4OVYQ0000")
        .diagnosticReport("I2-M2QUOOXL3O73NUZCB7HEOVQ2GAGQFOATAYXW5FMU3I57IYQDE6RQ0000")
        .diagnosticReports(diagnosticReports())
        .immunization("I2-LR6MEWBUXWJGD75WXF5BFXXTTLTYR3S3AHUTW55G25J4UOG3ZQIQ0000")
        .location("I2-WEIZUDRRQFULJACUVBXZO7EFOU000000")
        .locations(productionLocations())
        .medication("I2-H6VWTWQS5U454XKHOM6ZTUPCHA000000")
        .medicationOrder("I2-IB456XUS7OJUVJBC5ESLW3IZ2R6773XSYHA7V63BLTV6YSG4QJ6A0000")
        .medicationStatement("I2-EIQB74V2APLMGKQJPRRT7LIPABT43MYPA2TEUW36N6BTEAJC65RA0000")
        .observation("I2-QSUC3WVCAOC7PWYON5HMETFYBQWCULOIQWLKHG6OP3DXH7M7MUTQ0000")
        .observations(observations())
        .organization("I2-U2YS4YSMVOGA4TNVOJB3RXVIQRQR7OXTDMFNC24L4YSKJKXSTCZA0000")
        .organizations(productionOrganizations())
        .patient("1011537977V693883")
        .practitioner("I2-6NVSMKEGQKNB3KRDXBGE7NRIEY000000")
        .practitioners(productionPractitioners())
        .procedure("I2-DTO3TGZDH7VWRWL6CVHS4NTMTUBWUNA5D4U2L45RJJ3LG6LY6XXA0000")
        .procedures(productionCdwProcedures())
        .unknown("5555555555555")
        .build();
  }

  private static Procedures productionCdwProcedures() {
    return Procedures.builder().fromDate("ge2009").onDate("ge2009").toDate("le2014").build();
  }

  private static TestIds.Locations productionLocations() {
    return TestIds.Locations.builder()
        .name("TPA PCE OR AMB SURGERY      -X")
        .addressCity("TAMPA")
        .addressPostalCode("33612-4745")
        .addressState("FL")
        .addressStreet("13000 BRUCE B DOWNS BLVD")
        .build();
  }

  /*
   * These Organization search parameters are based on a real VA organization (Orlando VAMC)
   * if at any point the IT for Organization fails, it may be due to changes made to the
   * Organization.
   */
  private static TestIds.Organizations productionOrganizations() {
    return TestIds.Organizations.builder()
        .addressStreet("13800 VETERANS WAY")
        .addressCity("ORLANDO")
        .addressState("FL")
        .addressPostalCode("32827")
        .name("LAKE BALDWIN VAMC")
        .npi("http://hl7.org/fhir/sid/us-npi|1558796409")
        .build();
  }

  /*
   * These Practitioner search parameters were found on the VAs index for Practitioners. If the ITs
   * begin to fail, check that the Practitioner is still working at VA.
   */
  private static TestIds.Practitioners productionPractitioners() {
    return TestIds.Practitioners.builder()
        .family("ACOSTA")
        .given("SAID R")
        .npi("http://hl7.org/fhir/sid/us-npi|1013904481")
        // Valid ID will need to be added when test is available
        .specialty("http://hl7.org/fhir/practitioner-specialty|xxx")
        .build();
  }

  /** Return definitions for the qa environment. */
  private static SystemDefinition qa() {
    String url = "https://blue.qa.lighthouse.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .dstu2DataQuery(serviceDefinition("dstu2", url, 443, magicAccessToken(), "/dstu2/"))
        .stu3DataQuery(serviceDefinition("stu3", url, 443, magicAccessToken(), "/stu3/"))
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
    String url = "https://blue.staging.lighthouse.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .dstu2DataQuery(serviceDefinition("dstu2", url, 443, magicAccessToken(), "/dstu2/"))
        .stu3DataQuery(serviceDefinition("stu3", url, 443, magicAccessToken(), "/stu3/"))
        .internalDataQuery(serviceDefinition("internal", url, 443, null, "/data-query/"))
        .cdwIds(productionCdwIds())
        .build();
  }

  /** Return definitions for the lab environment. */
  private static SystemDefinition stagingLab() {
    String url = "https://blue.staging-lab.lighthouse.va.gov";
    return SystemDefinition.builder()
        .ids(serviceDefinition("ids", url, 443, null, "/not-available/"))
        .dstu2DataQuery(
            serviceDefinition(
                "dstu2", url, 443, magicAccessToken(), "/services/argonaut/v0/dstu2/"))
        .stu3DataQuery(
            serviceDefinition("stu3", url, 443, magicAccessToken(), "/services/argonaut/v0/stu3/"))
        .internalDataQuery(serviceDefinition("internal", url, 443, null, "/data-query/"))
        .cdwIds(labIds())
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
