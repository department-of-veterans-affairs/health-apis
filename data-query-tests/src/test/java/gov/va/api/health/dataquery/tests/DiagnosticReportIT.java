package gov.va.api.health.dataquery.tests;

import gov.va.api.health.dataquery.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class DiagnosticReportIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?_id={id}",
            verifier.ids().diagnosticReport()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "DiagnosticReport?_id={id}", verifier.ids().unknown()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?identifier={id}",
            verifier.ids().diagnosticReport()),
        ResourceVerifier.test(
            404,
            OperationOutcome.class,
            "DiagnosticReport?identifier={id}",
            verifier.ids().unknown()));
  }

  @Test
  @Category({
    Local.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void basic() {
    verifier.verifyAll(
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB",
            verifier.ids().patient()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={loinc1}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().loinc1()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={loinc1},{badLoinc}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().loinc1(),
            verifier.ids().diagnosticReports().badLoinc()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&code={loinc1},{loinc2}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().loinc1(),
            verifier.ids().diagnosticReports().loinc2()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={onDate}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().onDate()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={fromDate}&date={toDate}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().fromDate(),
            verifier.ids().diagnosticReports().toDate()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYear}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYear()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonth}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonth()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDay}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDay()),
        ResourceVerifier.test(
            400,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHour}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHour()),
        ResourceVerifier.test(
            400,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinute}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHourMinute()),
        ResourceVerifier.test(
            400,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinuteSecond}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHourMinuteSecond()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinuteSecondTimezone}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHourMinuteSecondTimezone()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateYearMonthDayHourMinuteSecondZulu}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateYearMonthDayHourMinuteSecondZulu()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateGreaterThan}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateGreaterThan()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateNotEqual}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateNotEqual()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateStartsWith}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateStartsWith()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateNoPrefix}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateNoPrefix()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateEqual}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateEqual()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateLessOrEqual}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateLessOrEqual()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}&category=LAB&date={dateLessThan}",
            verifier.ids().patient(),
            verifier.ids().diagnosticReports().dateLessThan()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.class,
            "DiagnosticReport/{id}",
            verifier.ids().diagnosticReport()),
        ResourceVerifier.test(
            404, OperationOutcome.class, "DiagnosticReport/{id}", verifier.ids().unknown()),
        ResourceVerifier.test(
            200,
            DiagnosticReport.Bundle.class,
            "DiagnosticReport?patient={patient}",
            verifier.ids().patient()));
  }

  @Test
  @Category({
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void searchNotMe() {
    verifier.verifyAll(
        ResourceVerifier.test(
            403,
            OperationOutcome.class,
            "DiagnosticReport?patient={patient}",
            verifier.ids().unknown()));
  }
}
