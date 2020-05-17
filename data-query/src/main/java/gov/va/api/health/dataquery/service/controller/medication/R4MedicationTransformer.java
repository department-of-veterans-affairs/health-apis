package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Narrative;
import gov.va.api.health.uscorer4.api.resources.Medication;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public class R4MedicationTransformer {
  @NotNull final DatamartMedication datamart;

  static CodeableConcept code(Optional<DatamartMedication.RxNorm> maybeRxnorm, String localDrugName) {
    if (maybeRxnorm == null || !maybeRxnorm.isPresent()) {

      // If rxnorm is null then return a coding with the local drug name if it exists
      return CodeableConcept.builder().text(localDrugName).build();
    }

    DatamartMedication.RxNorm rxnorm = maybeRxnorm.get();

    return CodeableConcept.builder()
        .text(rxnorm.text())
        .coding(List.of(Coding.builder().code(rxnorm.code()).display(rxnorm.text()).build()))
        .build();
  }

  static CodeableConcept form(Optional<DatamartMedication.Product> maybeProduct) {
    if (maybeProduct == null || !maybeProduct.isPresent()) {
      return null;
    }

    DatamartMedication.Product product = maybeProduct.get();

    return CodeableConcept.builder()
        .text(product.formText())
        .coding(List.of(Coding.builder().code(product.id()).display(product.formText()).build()))
        .build();
  }

  Narrative bestText() {
    String text =
            datamart.rxnorm().isPresent() ? datamart.rxnorm().get().text() : datamart.localDrugName();
    return Narrative.builder()
            .div("<div>" + text + "</div>")
            .status(Narrative.NarrativeStatus.additional)
            .build();
  }

  Medication toFhir() {
    return Medication.builder()
        .resourceType("Medication")
        .id(datamart.cdwId())
        .text(bestText())
        .code(code(datamart.rxnorm(), datamart.localDrugName()))
        .form(form(datamart.product()))
        .build();
  }
}
