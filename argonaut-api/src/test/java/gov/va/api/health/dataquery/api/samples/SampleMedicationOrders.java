package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.DispenseRequest;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.DosageInstruction;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.Status;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.Substitution;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public class SampleMedicationOrders {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public DispenseRequest dispenseRequest() {
    return DispenseRequest.builder()
        .id("2222")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .medicationCodeableConcept(codeableConcept())
        .validityPeriod(period())
        .numberOfRepeatsAllowed(10)
        .quantity(simpleQuantity())
        .expectedSupplyDuration(duration())
        .build();
  }

  public DosageInstruction dosageInstruction() {
    return DosageInstruction.builder()
        .id("2222")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .text("Hello Text")
        .additionalInstructions(codeableConcept())
        .timing(timing())
        .asNeededBoolean(true)
        .siteCodeableConcept(codeableConcept())
        .route(codeableConcept())
        .method(codeableConcept())
        .doseQuantity(simpleQuantity())
        .rateRange(range())
        .maxDosePerDay(ratio())
        .build();
  }

  public MedicationOrder medicationOrder() {
    return MedicationOrder.builder()
        .resourceType("MedicationOrder")
        .id("2222")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello Language")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .identifier(singletonList(identifier()))
        .dateWritten("2000-10-01")
        .status(Status.active)
        .dateEnded("2000-10-01")
        .reasonEnded(codeableConcept())
        .patient(reference())
        .prescriber(reference())
        .reasonCodeableConcept(codeableConcept())
        .note("Hello Note")
        .medicationReference(reference())
        .dosageInstruction(singletonList(dosageInstruction()))
        .dispenseRequest(dispenseRequest())
        .substitution(substitution())
        .priorPrescription(reference())
        .build();
  }

  public Substitution substitution() {
    return Substitution.builder()
        .id("2222")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .type(codeableConcept())
        .reason(codeableConcept())
        .build();
  }
}
