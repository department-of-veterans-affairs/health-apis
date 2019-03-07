package gov.va.api.health.dataquery.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import org.junit.Test;

public class FhirTest {

  /**
   * Datetime is a union of xs:dateTime, xs:date, xs:gYearMonth, xs:gYear. See
   * http://hl7.org/fhir/DSTU2/datatypes.html#datetime
   */
  @Test
  public void parseDateTime() {
    assertThat(Fhir.parseDateTime(null)).isNull();
    assertThat(Fhir.parseDateTime(" ")).isNull();
    for (String datetime :
        Arrays.asList(
            // dateTime
            "2002-05-30T09:00:00",
            "2002-05-30T09:30:10.5",
            "2002-05-30T09:30:10Z",
            "2002-05-30T09:30:10-06:00",
            "2002-05-30T09:30:10+06:00",
            // date
            "2002-09-24",
            "2002-09-24Z",
            "2002-09-24-06:00",
            "2002-09-24+06:00",
            // gYearMonth
            "2001-10",
            "2001-10+02:00",
            "2001-10Z",
            "2001-10+00:00",
            "-2001-10",
            "-20000-04",
            // gYear
            "2001",
            "2001+02:00",
            "2001Z",
            "2001+00:00",
            "-2001",
            "-20000"
            //
            )) {
      Instant actual = Fhir.parseDateTime(datetime);
      assertThat(actual).withFailMessage(datetime).isNotNull();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseDateTimeThrowsExceptionWhenCannotBeParsed() {
    Fhir.parseDateTime("nope");
  }
}
