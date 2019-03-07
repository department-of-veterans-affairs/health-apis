package gov.va.api.health.argonaut.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.argonaut.api.datatypes.CodeableConcept;
import gov.va.api.health.argonaut.api.datatypes.Coding;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import java.util.Arrays;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public class SampleDiagnosticReports {

  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  private CodeableConcept category() {
    return CodeableConcept.builder()
        .coding(
            singletonList(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                    .code("LAB")
                    .display("Laboratory")
                    .build()))
        .text("dat category")
        .build();
  }

  private CodeableConcept codeCodeableConcept() {
    return CodeableConcept.builder().coding(codeCodingList()).text("panel").build();
  }

  private List<Coding> codeCodingList() {
    return singletonList(
        Coding.builder().system("http://HelloSystem.com").code("Hello Code").build());
  }

  public DiagnosticReport diagnosticReport() {
    return DiagnosticReport.builder()
        .id("1234")
        .resourceType("Diagnostic Report")
        .meta(meta())
        .implicitRules("https://HelloRules.com")
        .language("Hello Language")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(Arrays.asList(extension(), extension()))
        .modifierExtension(
            Arrays.asList(extension(), extensionWithQuantity(), extensionWithRatio()))
        .identifier(singletonList(identifier()))
        .status(DiagnosticReport.Code._final)
        .category(category())
        .code(codeCodeableConcept())
        .subject(reference())
        .encounter(reference())
        .effectiveDateTime("2013-06-21T20:05:12Z")
        .issued("2013-06-21T19:03:16Z")
        .performer(reference())
        .conclusion("The end.")
        .codedDiagnosis(Arrays.asList(codeableConcept(), codeableConcept()))
        .presentedForm(Arrays.asList(attachment(), attachment()))
        .build();
  }
}
