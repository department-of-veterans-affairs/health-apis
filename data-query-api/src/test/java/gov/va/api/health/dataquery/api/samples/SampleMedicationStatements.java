package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.resources.MedicationStatement;
import gov.va.api.health.dataquery.api.resources.MedicationStatement.Dosage;
import gov.va.api.health.dataquery.api.resources.MedicationStatement.Status;
import java.util.Arrays;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

/**
 * This class provides data structures that are populated with dummy values, suitable for testing
 * serialization.
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public class SampleMedicationStatements {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  private Dosage dosage() {
    return Dosage.builder()
        .id("2222")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .text("Hello Text")
        .timing(timing())
        .asNeededBoolean(true)
        .siteCodeableConcept(codeableConcept())
        .route(codeableConcept())
        .method(codeableConcept())
        .doseQuantity(simpleQuantity())
        .rateRange(range())
        .maxDosePerPeriod(ratio())
        .build();
  }

  private List<Dosage> dosageList() {
    return singletonList(dosage());
  }

  public MedicationStatement medicationStatement() {
    return MedicationStatement.builder()
        .resourceType("MedicationStatement")
        .id("ms1")
        .meta(meta())
        .implicitRules("rules")
        .language("murican!")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(Arrays.asList(extension(), extension()))
        .modifierExtension(
            Arrays.asList(extension(), extensionWithQuantity(), extensionWithRatio()))
        .identifier(singletonList(identifier()))
        .patient(reference())
        .informationSource(reference())
        .dateAsserted("2005-01-21T07:57:00.000Z")
        .status(Status.active)
        .wasNotTaken(true)
        .reasonNotTaken(codeableConceptList())
        .reasonForUseReference(reference())
        .effectiveDateTime("2005-01-21T07:57:00.001Z")
        .note("neato")
        .supportingInformation(referenceList())
        .medicationReference(reference())
        .dosage(dosageList())
        .build();
  }

  public MedicationStatement medicationStatementWithAlternateValues() {
    return medicationStatement()
        .reasonForUseReference(null)
        .reasonForUseCodeableConcept(codeableConcept())
        .medicationReference(null)
        .medicationCodeableConcept(codeableConcept())
        .effectiveDateTime(null)
        .effectivePeriod(period());
  }
}
