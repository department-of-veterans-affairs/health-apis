package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.uscorer4.api.resources.DiagnosticReport;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class DiagnosticReportIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void basic() {
    verifier.verifyAll(
        // Reads
        test(
            200,
            DiagnosticReport.class,
            "DiagnosticReport/{id}",
            verifier.ids().diagnosticReport()),
        test(404, OperationOutcome.class, "DiagnosticReport/{id}", verifier.ids().unknown()),
        // Search By Patient
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}",
            verifier.ids().patient()),
        // Search By Patient and Category (and Date)
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB",
            verifier.ids().patient()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=http://terminology.hl7.org/CodeSystem/v2-0074|LAB&date={onDate}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().onDate()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=http://terminology.hl7.org/CodeSystem/v2-0074|&date={fromDate}&date={toDate}",
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
        // Search By Patient and Code (and Date)
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code=",
            verifier.ids().patient()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={badLoinc}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().badLoinc()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code=panel&date={onDate}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().onDate()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code=&date={fromDate}&date={toDate}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().fromDate(),
            verifier.ids().diagnosticReports().toDate()),
        // Search By Patient and Status
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&status=final",
            verifier.ids().patient()));
  }

  @Test
  public void searchByIdentifier() {
    assumeEnvironmentIn(Environment.LOCAL);
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
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            403,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}",
            verifier.ids().unknown()));
  }
}
