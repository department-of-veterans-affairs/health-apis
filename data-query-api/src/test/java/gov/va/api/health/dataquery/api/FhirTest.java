package gov.va.api.health.dataquery.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.Test;

public class FhirTest {

  @Test
  public void base64RegexMatches() {
    assertThat(Pattern.matches(Fhir.BASE64, "SSBhdGUgbWFueSBwZWFudXRz"))
        .withFailMessage("I ate many peanuts; failed valid BASE64 without padding.")
        .isTrue();
    assertThat(Pattern.matches(Fhir.BASE64, "SSBqdXN0IGF0ZSBhIHBlYW51dAo="))
        .withFailMessage("I ate a peanut; failed valid BASE64 with one = padding.")
        .isTrue();
    assertThat(Pattern.matches(Fhir.BASE64, "SSBhdGUgYSBmZXcgcGVhbnV0cw=="))
        .withFailMessage("I ate a few peanuts; failed valid BASE64 with two = padding.")
        .isTrue();
    assertThat(Pattern.matches(Fhir.BASE64, ""))
        .withFailMessage("Failed valid BASE64 empty string.")
        .isTrue();
    assertThat(
            Pattern.matches(
                Fhir.BASE64, "SSBqdXN0IGF0ZSBhIHBlYW51dAo=SSBqdXN0IGF0ZSBhIHBlYW51dAo="))
        .withFailMessage("Invalid BASE64. = in middle of string was not rejected.")
        .isFalse();
    assertThat(Pattern.matches(Fhir.BASE64, "SSBqdXN0IGF0ZSBhIHBlYW51dAo"))
        .withFailMessage("Invalid BASE64. String missing padding was not rejected.")
        .isFalse();
  }

  /**
   * Datetime is a union of xs:dateTime, xs:date, xs:gYearMonth, xs:gYear. See
   * http://hl7.org/fhir/DSTU2/datatypes.html#datetime
   */
  @Test
  public void parseDateTime() {
    assertThat(Fhir.parseDateTime(null)).isNull();
    assertThat(Fhir.parseDateTime(" ")).isNull();
    for (String datetime :
        Arrays.asList( // dateTime
            "2002-05-30T09:00:00",
            "2002-05-30T09:30:10.5",
            "2002-05-30T09:30:10Z",
            "2002-05-30T09:30:10-06:00",
            "2002-05-30T09:30:10+06:00", // date
            "2002-09-24",
            "2002-09-24Z",
            "2002-09-24-06:00",
            "2002-09-24+06:00", // gYearMonth
            "2001-10",
            "2001-10+02:00",
            "2001-10Z",
            "2001-10+00:00",
            "-2001-10",
            "-20000-04", // gYear
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
