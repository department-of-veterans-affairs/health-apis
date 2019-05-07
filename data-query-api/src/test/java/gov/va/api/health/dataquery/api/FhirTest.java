package gov.va.api.health.dataquery.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.Test;

public class FhirTest {

  @Test
  public void base64RegexDoesNotMatchBadStrings() {
    // Make sure that = is only allowed as the last 1 or 2 characters of the string.
    assertThat(Pattern.matches(Fhir.BASE64, "SSBqdXN0IGF0ZSBhIHBlYW51dA=o"))
        .withFailMessage("Padding character in wrong location is invalid but was not rejected.")
        .isFalse();
    assertThat(Pattern.matches(Fhir.BASE64, ""))
        .withFailMessage("Empty string is invalid but was not rejected.")
        .isFalse();
    assertThat(Pattern.matches(Fhir.BASE64, "SSBqdXN0IGF0ZSBhIHBlYW51dAo"))
        .withFailMessage("Missing padding/improper size is invalid but was not rejected.")
        .isFalse();
  }

  @Test
  public void base64RegexMatchesGoodStrings() {
    // There are two separate cases in the regex that should be tested. < and > 4 characters.
    assertThat(Pattern.matches(Fhir.BASE64, "QUJD"))
        .withFailMessage("Failed short, valid BASE64 without padding.")
        .isTrue();
    assertThat(Pattern.matches(Fhir.BASE64, "QUI="))
        .withFailMessage("Failed short, valid BASE64 with one padding char.")
        .isTrue();
    assertThat(Pattern.matches(Fhir.BASE64, "QQ=="))
        .withFailMessage("Failed short, valid BASE64 with two padding chars.")
        .isTrue();
    assertThat(Pattern.matches(Fhir.BASE64, "SSBhdGUgbWFueSBwZWFudXRz"))
        .withFailMessage("Failed long, valid BASE64 without padding.")
        .isTrue();
    assertThat(Pattern.matches(Fhir.BASE64, "SSBqdXN0IGF0ZSBhIHBlYW51dAo="))
        .withFailMessage("Failed long, valid BASE64 with one padding char.")
        .isTrue();
    assertThat(Pattern.matches(Fhir.BASE64, "SSBhdGUgYSBmZXcgcGVhbnV0cw=="))
        .withFailMessage("Failed long, valid BASE64 with two padding chars.")
        .isTrue();
  }

  /**
   * Datetime is a union of xs:dateTime, xs:date, xs:gYearMonth, xs:gYear. See
   * http://hl7.org/fhir/DSTU2/datatypes.html#datetime
   */
  @Test
  public void parseDateTime() {
    assertThat(Fhir.parseDateTime(null)).isNull();
    assertThat(Fhir.parseDateTime(" ")).isNull();
    for (String datetime : // dateTime
        Arrays.asList(
            "2002-05-30T09:00:00",
            "2002-05-30T09:30:10.5",
            "2002-05-30T09:30:10Z",
            "2002-05-30T09:30:10-06:00", // date
            "2002-05-30T09:30:10+06:00",
            "2002-09-24",
            "2002-09-24Z",
            "2002-09-24-06:00", // gYearMonth
            "2002-09-24+06:00",
            "2001-10",
            "2001-10+02:00",
            "2001-10Z",
            "2001-10+00:00",
            "-2001-10", // gYear
            "-20000-04",
            "2001",
            "2001+02:00",
            "2001Z",
            "2001+00:00",
            "-2001",
            "-20000")) {
      Instant actual = Fhir.parseDateTime(datetime);
      assertThat(actual).withFailMessage(datetime).isNotNull();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseDateTimeThrowsExceptionWhenCannotBeParsed() {
    Fhir.parseDateTime("nope");
  }
}
