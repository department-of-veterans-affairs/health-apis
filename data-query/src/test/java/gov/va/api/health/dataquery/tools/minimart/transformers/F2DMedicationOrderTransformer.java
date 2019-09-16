package gov.va.api.health.dataquery.tools.minimart.transformers;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder.DispenseRequest;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder.DosageInstruction;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder.Status;
import gov.va.api.health.dataquery.tools.minimart.FhirToDatamartUtils;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Duration;
import gov.va.api.health.dstu2.api.datatypes.SimpleQuantity;
import gov.va.api.health.dstu2.api.datatypes.Timing;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public class F2DMedicationOrderTransformer {

  FhirToDatamartUtils fauxIds;

  private String additionalInstructions(CodeableConcept additionalInstructions) {
    if (additionalInstructions == null) {
      return null;
    }
    return additionalInstructions.text();
  }

  private Optional<Instant> dateEnded(String dateEnded) {
    if (StringUtils.isBlank(dateEnded)) {
      return null;
    }
    return Optional.of(Instant.parse(dateEnded));
  }

  private Instant dateWritten(String dateWritten) {
    if (StringUtils.isBlank(dateWritten)) {
      return null;
    }
    return Instant.parse(dateWritten);
  }

  private Optional<DispenseRequest> dispenseRequest(
      MedicationOrder.DispenseRequest dispenseRequest) {
    if (dispenseRequest == null) {
      return null;
    }
    return Optional.of(
        DispenseRequest.builder()
            .numberOfRepeatsAllowed(Optional.ofNullable(dispenseRequest.numberOfRepeatsAllowed()))
            .quantity(Optional.ofNullable(dispenseRequestQuantity(dispenseRequest.quantity())))
            .unit(Optional.ofNullable(dispenseRequestUnit(dispenseRequest.quantity())))
            .expectedSupplyDuration(
                Optional.ofNullable(
                    dispenseRequestExpectedSupplyDuration(
                        dispenseRequest.expectedSupplyDuration())))
            .supplyDurationUnits(
                Optional.ofNullable(
                    dispenseRequestSupplyDurationUnits(dispenseRequest.expectedSupplyDuration())))
            .build());
  }

  private Integer dispenseRequestExpectedSupplyDuration(Duration expectedSupplyDuration) {
    if (expectedSupplyDuration == null) {
      return null;
    }
    if (expectedSupplyDuration.value() == null) {
      return null;
    }
    return expectedSupplyDuration.value().intValue();
  }

  private Double dispenseRequestQuantity(SimpleQuantity quantity) {
    if (quantity == null) {
      return null;
    }
    return quantity.value();
  }

  private String dispenseRequestSupplyDurationUnits(Duration expectedSupplyDuration) {
    if (expectedSupplyDuration == null) {
      return null;
    }
    return expectedSupplyDuration.unit();
  }

  private String dispenseRequestUnit(SimpleQuantity quantity) {
    if (quantity == null) {
      return null;
    }
    return quantity.unit();
  }

  private List<DosageInstruction> dosageInstruction(
      List<MedicationOrder.DosageInstruction> dosageInstructions) {
    if (dosageInstructions == null) {
      return null;
    }
    if (dosageInstructions.isEmpty()) {
      return null;
    }
    return dosageInstructions.stream()
        .map(
            dosageInstruction ->
                DosageInstruction.builder()
                    .dosageText(Optional.ofNullable(dosageInstruction.text()))
                    .timingText(Optional.ofNullable(timingText(dosageInstruction.timing())))
                    .additionalInstructions(
                        Optional.ofNullable(
                            additionalInstructions(dosageInstruction.additionalInstructions())))
                    .asNeeded(dosageInstruction.asNeededBoolean())
                    .routeText(Optional.ofNullable(routeText(dosageInstruction.route())))
                    .doseQuantityValue(
                        Optional.ofNullable(doseQuantityValue(dosageInstruction.doseQuantity())))
                    .doseQuantityUnit(
                        Optional.ofNullable(dosageQuantityUnit(dosageInstruction.doseQuantity())))
                    .build())
        .collect(Collectors.toList());
  }

  private String dosageQuantityUnit(SimpleQuantity doseQuantity) {
    if (doseQuantity == null) {
      return null;
    }
    return doseQuantity.unit();
  }

  private Double doseQuantityValue(SimpleQuantity doseQuantity) {
    if (doseQuantity == null) {
      return null;
    }
    return doseQuantity.value();
  }

  public DatamartMedicationOrder fhirToDatamart(MedicationOrder medicationOrder) {
    return DatamartMedicationOrder.builder()
        .cdwId(fauxIds.unmask("MedicationOrder", medicationOrder.id()))
        .patient(fauxIds.toDatamartReferenceWithCdwId(medicationOrder.patient()).get())
        .dateWritten(dateWritten(medicationOrder.dateWritten()))
        .status(status(medicationOrder.status()))
        .dateEnded(dateEnded(medicationOrder.dateEnded()))
        .prescriber(prescriber(medicationOrder.prescriber(), medicationOrder._prescriber()))
        .medication(
            fauxIds.toDatamartReferenceWithCdwId(medicationOrder.medicationReference()).get())
        .dosageInstruction(dosageInstruction(medicationOrder.dosageInstruction()))
        .dispenseRequest(dispenseRequest(medicationOrder.dispenseRequest()))
        .build();
  }

  private DatamartReference prescriber(Reference prescriber, Extension dar) {
    if (dar == null) {
      return fauxIds.toDatamartReferenceWithCdwId(prescriber).get();
    }
    return null;
  }

  private String routeText(CodeableConcept route) {
    if (route == null) {
      return null;
    }
    return route.text();
  }

  private Status status(MedicationOrder.Status status) {
    if (status == null) {
      return null;
    }
    return EnumSearcher.of(DatamartMedicationOrder.Status.class).find(status.toString());
  }

  private String timingText(Timing timing) {
    if (timing == null) {
      return null;
    }
    if (timing.code() == null) {
      return null;
    }
    return timing.code().text();
  }
}
