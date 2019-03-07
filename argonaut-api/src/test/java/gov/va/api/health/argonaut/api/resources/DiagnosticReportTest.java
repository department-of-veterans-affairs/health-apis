package gov.va.api.health.argonaut.api.resources;

import static gov.va.api.health.argonaut.api.RoundTrip.assertRoundTrip;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.ZeroOrOneOfVerifier;
import gov.va.api.health.argonaut.api.bundle.AbstractBundle;
import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.samples.SampleDiagnosticReports;
import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.junit.Test;

public class DiagnosticReportTest {

  private final SampleDiagnosticReports data = SampleDiagnosticReports.get();

  @Test
  public void bundlerCanBuildDiagnosticReportBundles() {
    DiagnosticReport.Entry entry =
        DiagnosticReport.Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://diagnosticreports.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(BundleLink.LinkRelation.self)
                        .url(("http://diagnosticreport/1"))
                        .build()))
            .resource(data.diagnosticReport())
            .search(data.search())
            .request(data.request())
            .response(data.response())
            .build();

    DiagnosticReport.Bundle bundle =
        DiagnosticReport.Bundle.builder()
            .entry(Collections.singletonList(entry))
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(BundleLink.LinkRelation.self)
                        .url(("http://diagnosticreport.com/2"))
                        .build()))
            .type(AbstractBundle.BundleType.searchset)
            .build();

    assertRoundTrip(bundle);
  }

  @Test
  public void diagnosticReport() {
    assertRoundTrip(data.diagnosticReport());
  }

  @Test
  public void relatedGroups() {
    ZeroOrOneOfVerifier.builder()
        .sample(data.diagnosticReport())
        .fieldPrefix("effective")
        .build()
        .verify();
  }

  @Test
  public void validationFailsGivenBadCategory() {
    assertThat(violationsOf(data.diagnosticReport().category(data.codeableConcept()))).isNotEmpty();
  }

  @Test
  public void validationFailsGivenNoCategory() {
    assertThat(violationsOf(data.diagnosticReport().category(null))).isNotEmpty();
  }

  @Test
  public void validationPassesGivenGoodCategory() {
    assertThat(
            violationsOf(
                data.diagnosticReport()
                    .category()
                    .coding()
                    .get(0)
                    .code("http://hl7.org/fhir/ValueSet/diagnostic-service-sections")))
        .isEmpty();
  }

  private <T> Set<ConstraintViolation<T>> violationsOf(T object) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator().validate(object);
  }
}
