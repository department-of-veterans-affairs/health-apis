package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.Medication.Product;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Narrative;
import java.util.List;
import java.util.Optional;
import lombok.Builder;

@Builder
public class Dstu2MedicationTransformer {

  private final DatamartMedication datamart;

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
    if (datamart.rxnorm().isPresent()) {
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .code(datamart.rxnorm().get().code())
                      .display(datamart.rxnorm().get().text())
                      .system("https://www.nlm.nih.gov/research/umls/rxnorm")
                      .build()))
          .text(datamart.rxnorm().get().text())
          .build();
    }
    if (datamart.product().isPresent()) {
      return CodeableConcept.builder()
          .text(datamart.localDrugName())
          .coding(
              List.of(
                  Coding.builder()
                      .display(datamart.localDrugName())
                      .code(datamart.product().get().id())
                      .system("urn:oid:2.16.840.1.113883.6.233")
                      .build()))
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

  Product product(Optional<DatamartMedication.Product> product) {
    if (!product.isPresent()) {
      return null;
    }
    return Medication.Product.builder()
        .id(product.get().id())
        .form(CodeableConcept.builder().text(product.get().formText()).build())
        .build();
  }

  /** Convert the datamart structure to FHIR compliant structure. */
  public Medication toFhir() {
    return Medication.builder()
        .resourceType(Medication.class.getSimpleName())
        .id(datamart.cdwId())
        .product(product(datamart.product()))
        .text(bestText())
        .code(bestCode())
        .build();
  }
}
