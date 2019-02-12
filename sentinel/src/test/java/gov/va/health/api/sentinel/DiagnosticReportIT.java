package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.health.api.sentinel.categories.LabArgo;
import gov.va.health.api.sentinel.categories.Local;
import gov.va.health.api.sentinel.categories.ProdArgo;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class DiagnosticReportIT {

  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category(Local.class)
  public void advanced() {
    verifier.verifyAll(
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?_id={id}",
            verifier.ids().diagnosticReport()),
        test(404, OperationOutcome.class, "DiagnosticReport?_id={id}", verifier.ids().unknown()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?identifier={id}",
            verifier.ids().diagnosticReport()),
        test(
            404,
            OperationOutcome.class,
            "DiagnosticReport?identifier={id}",
            verifier.ids().unknown()));
  }

  @Test
  @Category({Local.class, LabArgo.class, ProdArgo.class})
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB",
            verifier.ids().patient()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={loinc1}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().loinc1()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={loinc1},{badLoinc}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().loinc1(),
            verifier.ids().diagnosticReports().badLoinc()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={loinc1},{loinc2}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().loinc1(),
            verifier.ids().diagnosticReports().loinc2()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={onDate}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().onDate()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={fromDate}&date={toDate}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().fromDate(),
            verifier.ids().diagnosticReports().toDate()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYear}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYear()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonth}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonth()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDay}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDay()),
        test(
            400,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHour}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHour()),
        test(
            400,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinute}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHourMinute()),
        test(
            400,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinuteSecond}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHourMinuteSecond()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinuteSecondTimezone}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHourMinuteSecondTimezone()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinuteSecondZulu}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHourMinuteSecondZulu()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateGreaterThan}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateGreaterThan()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateNotEqual}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateNotEqual()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateStartsWith}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateStartsWith()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateNoPrefix}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateNoPrefix()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateEqual}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateEqual()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateLessOrEqual}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateLessOrEqual()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateLessThan}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateLessThan()),
        test(
            200,
            DiagnosticReport.class,
            "DiagnosticReport/{id}",
            verifier.ids().diagnosticReport()),
        test(404, OperationOutcome.class, "DiagnosticReport/{id}", verifier.ids().unknown()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  @Category({ProdArgo.class, LabArgo.class})
  public void searchNotMe() {
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}",
            verifier.ids().unknown()));
  }
}
