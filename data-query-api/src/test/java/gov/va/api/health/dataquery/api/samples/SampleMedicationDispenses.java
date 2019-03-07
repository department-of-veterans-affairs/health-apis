package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.resources.MedicationDispense;
import gov.va.api.health.dataquery.api.resources.MedicationDispense.DosageInstruction;
import gov.va.api.health.dataquery.api.resources.MedicationDispense.Status;
import gov.va.api.health.dataquery.api.resources.MedicationDispense.Substitution;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@NoArgsConstructor(staticName = "get")
public class SampleMedicationDispenses {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public DosageInstruction dosageInstruction() {
    return DosageInstruction.builder()
        .id("123")
        .modifierExtension(singletonList(extension()))
        .extension(singletonList(extension()))
        .text("Take orally, morning and night")
        .additionalInstructions(codeableConcept())
        .timing(timing())
        .asNeededBoolean(false)
        .siteCodeableConcept(codeableConcept())
        .route(codeableConcept())
        .method(codeableConcept())
        .doseQuantity(simpleQuantity())
        .rateRatio(ratio())
        .maxDosePerPeriod(ratio())
        .build();
  }

  public MedicationDispense medicationDispense() {
    return MedicationDispense.builder()
        .resourceType("MedicationDispense")
        .id("789")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("English")
        .contained(simpleResourceList())
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .identifier(identifier())
        .text(narrative())
        .status(Status.completed)
        .patient(reference())
        .dispenser(reference())
        .authorizingPrescription(singletonList(reference()))
        .type(codeableConcept())
        .quantity(simpleQuantity())
        .daysSupply(simpleQuantity())
        .medicationReference(reference())
        .whenPrepared("2018-01-02T04:00:00Z")
        .whenHandedOver("2018-01-04T04:00:00Z")
        .destination(reference())
        .receiver(singletonList(reference()))
        .note("Do not feed to dogs")
        .dosageInstruction(singletonList(dosageInstruction()))
        .substitution(substitution())
        .build();
  }

  public Substitution substitution() {
    return Substitution.builder()
        .id("456")
        .modifierExtension(singletonList(extension()))
        .extension(singletonList(extension()))
        .type(codeableConcept())
        .reason(singletonList(codeableConcept()))
        .responsibleParty(singletonList(reference()))
        .build();
  }
}
