package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.Medication.Product;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Narrative;
import java.util.List;
import lombok.Builder;

@Builder
public class DatamartMedicationTransformer {

  private final DatamartMedication datamart;

  CodeableConcept code(DatamartMedication.RxNorm rxnorm) {
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .code(rxnorm.code)
                    .display(rxnorm.text)
                    .system("https://www.nlm.nih.gov/research/umls/rxnorm/")
                    .build()))
        .text(rxnorm.text)
        .build();
  }

  Product product(DatamartMedication.Product product) {
    return Medication.Product.builder()
        .id(product.id)
        .form(CodeableConcept.builder().text(product.formText).build())
        .build();
  }

  Narrative text(String text) {
    return Narrative.builder()
        .div("<div>" + text + "</div>")
        .status(Narrative.NarrativeStatus.additional)
        .build();
  }

  /** Convert the datamart structure to FHIR compliant structure. */
  public Medication toFhir() {
    return Medication.builder()
        .resourceType(Medication.class.getSimpleName())
        .id(datamart.cdwId())
        .product(product(datamart.product()))
        .text(text(datamart.rxnorm().text))
        .code(code(datamart.rxnorm()))
        .build();
  }
}
