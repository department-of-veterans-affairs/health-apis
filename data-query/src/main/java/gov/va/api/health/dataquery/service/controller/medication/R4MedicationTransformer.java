package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Narrative;
import gov.va.api.health.uscorer4.api.resources.Medication;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public class R4MedicationTransformer {
  @NotNull final DatamartMedication datamart;

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

  /**
   * Per KBS guidelines we want to return the following for code.
   *
   * <p>1. If rxnorm information is available, return it as the code.
   *
   * <p>2. If rxnorm is not available, but product is, then this is a "local" drug. We will use the
   * local drug name as the text and the code.coding.display value. The product.id is the VA
   * specific "VUID" medication ID. We want to use this value as the code.coding.code along with the
   * VA specific system.
   *
   * <p>3. If neither rxnorm or product is available, we'll create a code with no coding, using just
   * the local drug name as text.
   */
  CodeableConcept bestCode() {
    if (datamart.rxnorm() != null && datamart.rxnorm().isPresent()) {
      DatamartMedication.RxNorm rxNorm = datamart.rxnorm().get();
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .code(rxNorm.code())
                      .display(rxNorm.text())
                      .system("https://www.nlm.nih.gov/research/umls/rxnorm")
                      .build()))
          .text(rxNorm.text())
          .build();
    }
    if (datamart.product() != null && datamart.product().isPresent()) {
      DatamartMedication.Product product = datamart.product().get();
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .code(product.id())
                      .display(datamart.localDrugName())
                      .system("urn:oid:2.16.840.1.113883.6.233")
                      .build()))
          .text(datamart.localDrugName())
          .build();
    }
    return CodeableConcept.builder().text(datamart.localDrugName()).build();
  }

  Narrative bestText() {
    String text =
        datamart.rxnorm().isPresent() ? datamart.rxnorm().get().text() : datamart.localDrugName();
    return Narrative.builder()
        .div("<div>" + text + "</div>")
        .status(Narrative.NarrativeStatus.additional)
        .build();
  }

  List<Identifier> identifierFromProduct() {
    if (datamart.product() != null && datamart.product().isPresent()) {
      return List.of(Identifier.builder().id(datamart.product().get().id()).build());
    } else {
      return null;
    }
  }

  Medication toFhir() {
    return Medication.builder()
        .resourceType("Medication")
        .id(datamart.cdwId())
        .text(bestText())
        .code(bestCode())
        .identifier(identifierFromProduct())
        .form(form(datamart.product()))
        .build();
  }
}
