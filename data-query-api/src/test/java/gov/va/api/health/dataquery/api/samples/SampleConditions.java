package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.resources.Condition;
import gov.va.api.health.dataquery.api.resources.Condition.ClinicalStatusCode;
import gov.va.api.health.dataquery.api.resources.Condition.Evidence;
import gov.va.api.health.dataquery.api.resources.Condition.Stage;
import gov.va.api.health.dataquery.api.resources.Condition.VerificationStatusCode;
import java.util.Arrays;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public class SampleConditions {

  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public Condition condition() {
    return Condition.builder()
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
        .patient(reference())
        .encounter(reference())
        .asserter(reference())
        .dateRecorded("2000-10-01")
        .code(codeableConcept())
        .category(codeableConcept())
        .clinicalStatus(ClinicalStatusCode.active)
        .verificationStatus(VerificationStatusCode.provisional)
        .severity(codeableConcept())
        .onsetAge(age())
        .abatementBoolean(true)
        .stage(stage())
        .evidence(singletonList(evidence()))
        .bodySite(singletonList(codeableConcept()))
        .notes("HelloText")
        .build();
  }

  public Evidence evidence() {
    return Evidence.builder()
        .id("8888")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .code(codeableConcept())
        .detail(singletonList(reference()))
        .build();
  }

  public Stage stage() {
    return Stage.builder()
        .id("8888")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .summary(codeableConcept())
        .assessment(singletonList(reference()))
        .build();
  }
}
