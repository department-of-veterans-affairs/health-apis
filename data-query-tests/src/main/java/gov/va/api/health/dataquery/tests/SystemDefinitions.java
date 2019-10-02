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
        .allergyIntolerance("I2-MPTQDG3NVPK5BGA3KODHCYZHTM000000")
        .condition("I2-OGOGVG7UKPFCHLAPY3HP74VI4Q000000")
        .diagnosticReport("I2-NVJU4EWW3YBUEM2EFYP6VYA4JM000000")
        .diagnosticReports(diagnosticReports())
        .immunization("I2-ZHDWUIIUHJ5Q3KNK5AX33G67H4000000")
        .medication("I2-2IDZ7BGJF46A4QTPZGF5RRNN7I000000")
        .medicationOrder("I2-M7PXGD377A3MHDSMT4E7LHRPQY000000")
        .medicationStatement("I2-K4TY5EHC2CLNI264FNJK74XUHA000000")
        .observation("I2-IC7T4AQEH7DM6S2M63GUXQBPEA000000")
        .observations(observations())
        .patient("1011537977V693883")
        .procedure("I2-LYTD7U57TFHXJLTFRXDTMGI6MA000000")
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
    /* IDS encoded with key: data-query */
    return TestIds.builder()
        .publicIds(true)
        .allergyIntolerance("I2-77Z26UJVFNOFH34V6NZFYZ2CQCSCTIWLE7XF4HX6ZWLGHFQTHKOQ0000")
        .appointment("I2-YFGSH7XZIDAPOYL24GECYKVG5MXYAY6D5AXK24LLFDIFP6GD4LGQ0000")
        .condition("I2-BTUFUJHXLJ4GX5NMJOGVTPQRAA000000")
        .diagnosticReport("I2-AHVBJEQ5AYSKIMWPBP76H7GKGW2NAJZCWYDRHESB2OSOWL4C57CA0000")
        .encounter("I2-U4SUMY5UCFBIDMJBTD6AQB37XHD5G56UK4TESCKITDDOQ5YU7XJA0000")
        .diagnosticReports(diagnosticReports())
        .immunization("I2-CDDUTSPQSJCONJYNX3YSVZL354000000")
        .location("I2-XPB3YP63EXGWLVZ3Z3TE76MQBE000000")
        .medication("I2-S5FGE6M764FZFLSFENQPCLCYME000000")
        .medicationDispense("I2-22CRMSOEQUG7QIPH4DLC4V5FFH2C5OZRTCGW2QDOCFD5SESUFKUQ0000")
        .medicationOrder("I2-D5VD7CW7FYAZL3ZFHOXMMZ23CWKRQY5YF73FXSWN2MEKT5NYFJYA0000")
        .medicationStatement("I2-MRB3MCOINJJ3TABBMC3ARA3S57IWFDJGAW4AXZ2KKMHLZY2Y6MAQ0000")
        .observation("I2-GEXUF7DF2QYP5ID5OAM3TDFIODV7NJCIZVGTE6E6O7KUMPZIQZDA0000")
        .observations(observations())
        .organization("I2-CU32H73D562BVWKWLSRRHBY4WFWYJ7447EZD5HWII6CKS6YQZCTA0000")
        .pii(
            PersonallyIdentifiableInformation.builder()
                .gender("male")
                .birthdate("1948-06-28")
                .given("Conrad619")
                .name("Olson653, Conrad619")
                .family("Olson653")
                .build())
        .patient("111222333V000999")
        .practitioner("I2-WDRQEV46TZAZCKF236GEVA7JQ4000000")
        .procedure("I2-VAJTCWXXHJ554L7YKRDEMMQHAMXDF2WHMXXUNDBGLGS4DXNTYFTQ0000")
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
        .allergyIntolerance("I2-DU3ADCHWAB7S6FYVTZVJXYURW6OJF2PMJFC5MLLMNNWUGHAENZTQ0000")
        .appointment("I2-ZATU4OPBUMQ4CXL73YERYX4VLU7VSSSA2JTHONXC3TKAKQQCTUQQ0000")
        .condition("I2-UD6WKM44KCM2TFOTFOYNQMSWBOUIVYFINXVXNVZSJ3GEF7FJJVQQ0000")
        .diagnosticReport("I2-BU7TYY2OWNQAHSBJO3UXCRVZF5GGTXWC4PKJHDFAUSGIMSPYKHDA0000")
        .diagnosticReports(diagnosticReports())
        .encounter("I2-PNOERLHVV7TNYKLNFOB5CD44UJKTAGL3P7PRPPU4LPXTYT7JPOYQ0000")
        .immunization("I2-LLK72TY34QKKXYZCFA5BJDSNJIPRJTBTI3SRTK52SXYBE7TJ3CBQ0000")
        .location("I2-XIFVEANVQEMF542L5OTVDX3MF4000000")
        .medication("I2-GCCUUPCAKW7WXMLWRFA7K6ELHQ000000")
        .medicationDispense("I2-DWUIVKO22MMKSDBV4XN257NMG45NEUELQBREYHUHZWTKSWTMLUKA0000")
        .medicationOrder("I2-7MYZ4PRRJQLACCVL6QFZCMWJQETDG4CKFA75PD4JQR6GKPRMXY4A0000")
        .medicationStatement("I2-MRB3MCOINJJ3TABBMC3ARA3S57IWFDJGAW4AXZ2KKMHLZY2Y6MAQ0000")
        .observation("I2-BXHS3JZGZI5H6IEVFY7P3IH7YMCGL5A66XLECKA56P5J5TRLPWKQ0000")
        .observations(observations())
        .organization("I2-4WOEB6M4TEWPZFVZA4RVMYF3LU000000")
        .patient("1011537977V693883")
        .practitioner("I2-B33NRHR736NXOVE276BTWAOMNE000000")
        .procedure("I2-CAMPMNPTZLQXJMQBRAE6REKBZXGA2QZTPA6GB7BEJ5XKJ7WKSUOQ0000")
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
