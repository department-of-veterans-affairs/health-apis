package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.DiagnosticReport;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class DiagnosticReportIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.r4();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void basic() {
    verifier.verifyAll(
        // Reads
        test(200, DiagnosticReport.class, "DiagnosticReport/{id}", testIds.diagnosticReport()),
        test(404, OperationOutcome.class, "DiagnosticReport/{id}", testIds.unknown()),
        // Search By Patient
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}",
            testIds.patient()),
        // Search By Patient and Category (and Date)
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB",
            testIds.patient()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=http://terminology.hl7.org/CodeSystem/v2-0074|LAB&date={onDate}",
            testIds.patient(),
            testIds.diagnosticReports().onDate()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=http://terminology.hl7.org/CodeSystem/v2-0074|&date={fromDate}&date={toDate}",
            testIds.patient(),
            testIds.diagnosticReports().fromDate(),
            testIds.diagnosticReports().toDate()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYear}",
            testIds.patient(),
            testIds.diagnosticReports().dateYear()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonth}",
            testIds.patient(),
            testIds.diagnosticReports().dateYearMonth()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDay}",
            testIds.patient(),
            testIds.diagnosticReports().dateYearMonthDay()),
        test(
            400,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHour}",
            testIds.patient(),
            testIds.diagnosticReports().dateYearMonthDayHour()),
        test(
            400,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinute}",
            testIds.patient(),
            testIds.diagnosticReports().dateYearMonthDayHourMinute()),
        test(
            400,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinuteSecond}",
            testIds.patient(),
            testIds.diagnosticReports().dateYearMonthDayHourMinuteSecond()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinuteSecondTimezone}",
            testIds.patient(),
            testIds.diagnosticReports().dateYearMonthDayHourMinuteSecondTimezone()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinuteSecondZulu}",
            testIds.patient(),
            testIds.diagnosticReports().dateYearMonthDayHourMinuteSecondZulu()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateGreaterThan}",
            testIds.patient(),
            testIds.diagnosticReports().dateGreaterThan()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateNotEqual}",
            testIds.patient(),
            testIds.diagnosticReports().dateNotEqual()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateStartsWith}",
            testIds.patient(),
            testIds.diagnosticReports().dateStartsWith()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateNoPrefix}",
            testIds.patient(),
            testIds.diagnosticReports().dateNoPrefix()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateEqual}",
            testIds.patient(),
            testIds.diagnosticReports().dateEqual()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateLessOrEqual}",
            testIds.patient(),
            testIds.diagnosticReports().dateLessOrEqual()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateLessThan}",
            testIds.patient(),
            testIds.diagnosticReports().dateLessThan()),
        // Search By Patient and Code (and Date)
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code=",
            testIds.patient()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={badLoinc}",
            testIds.patient(),
            testIds.diagnosticReports().badLoinc()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code=panel&date={onDate}",
            testIds.patient(),
            testIds.diagnosticReports().onDate()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code=&date={fromDate}&date={toDate}",
            testIds.patient(),
            testIds.diagnosticReports().fromDate(),
            testIds.diagnosticReports().toDate()),
        // Search By Patient and Status
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&status=final",
            testIds.patient()));
  }

  @Test
  public void searchByIdentifier() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?_id={id}",
            testIds.diagnosticReport()),
        test(404, OperationOutcome.class, "DiagnosticReport?_id={id}", testIds.unknown()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?identifier={id}",
            testIds.diagnosticReport()),
        test(404, OperationOutcome.class, "DiagnosticReport?identifier={id}", testIds.unknown()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "DiagnosticReport?patient={patient}", testIds.unknown()));
  }
}
