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
public class DatamartMedicationTransformer {

  private final DatamartMedication datamart;

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
    return CodeableConcept.builder()
        .text(datamart.localDrugName())
        .coding(
            List.of(
                Coding.builder()
                    .display(datamart.localDrugName())
                    .code(datamart.cdwId())
                    .system("urn:oid:2.16.840.1.113883.6.233")
                    .build()))
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
