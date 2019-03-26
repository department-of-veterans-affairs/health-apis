package gov.va.api.health.dataquery.service.config;

import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportController;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.Test;

public class DerpTest {
  @Test
  public void asdf() {
    // date=eq1970-01-01
    System.out.println(DiagnosticReportController.querySnippet(new String[] {"eq1970"})); // -01-01

    // and 1970-01-01T00:00:00Z =< dr.startedDtg and dr.endedDtg <= 1970-01-01T23:59:59.999Z

    // eq	the range of the search value fully contains the range of the target value
  }

  @Test
  public void derp() {
    System.out.println(LocalDate.parse("1970-01-01").atStartOfDay().toInstant(ZoneOffset.UTC));
    System.out.println(
        LocalDate.parse("1970-01-01").atStartOfDay().toInstant(ZoneOffset.UTC).minusMillis(1));
    System.out.println(
        LocalDate.parse("1970-01-02").atStartOfDay().toInstant(ZoneOffset.UTC).minusMillis(1));
  }
}
