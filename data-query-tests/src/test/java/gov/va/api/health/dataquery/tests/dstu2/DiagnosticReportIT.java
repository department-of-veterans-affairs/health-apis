package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.DataQueryResourceVerifier;
import gov.va.api.health.dataquery.tests.TestIds;
import gov.va.api.health.dstu2.api.resources.DiagnosticReport;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class DiagnosticReportIT {
  @Delegate ResourceVerifier verifier = DataQueryResourceVerifier.dstu2();

  TestIds testIds = DataQueryResourceVerifier.ids();

  @Test
  public void advanced() {
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
  public void basic() {
    verifier.verifyAll(
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB",
            testIds.patient()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={loinc1}",
            testIds.patient(),
            testIds.diagnosticReports().loinc1()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={loinc1},{badLoinc}",
            testIds.patient(),
            testIds.diagnosticReports().loinc1(),
            testIds.diagnosticReports().badLoinc()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={loinc1},{loinc2}",
            testIds.patient(),
            testIds.diagnosticReports().loinc1(),
            testIds.diagnosticReports().loinc2()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={onDate}",
            testIds.patient(),
            testIds.diagnosticReports().onDate()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={fromDate}&date={toDate}",
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
        test(200, DiagnosticReport.class, "DiagnosticReport/{id}", testIds.diagnosticReport()),
        test(404, OperationOutcome.class, "DiagnosticReport/{id}", testIds.unknown()),
        test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}",
            testIds.patient()));
  }

  @Test
  public void searchNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        test(403, OperationOutcome.class, "DiagnosticReport?patient={patient}", testIds.unknown()));
  }
}
