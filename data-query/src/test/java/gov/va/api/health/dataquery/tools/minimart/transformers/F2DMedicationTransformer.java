package gov.va.api.health.dataquery.tools.minimart.transformers;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.tools.minimart.FhirToDatamartUtils;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import java.util.Optional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class F2DMedicationTransformer {

  FhirToDatamartUtils fauxIds;

  private Optional<DatamartMedication.Product> datamartProduct(Medication.Product product) {
    if (product == null) {
      return null;
    }
    return Optional.of(
        DatamartMedication.Product.builder()
            .id(product.id())
            .formText(product.form() != null ? product.form().text() : null)
            .build());
  }

  public DatamartMedication fhirToDatamart(Medication fhir) {
    return DatamartMedication.builder()
        .cdwId(fauxIds.unmask("Medication", fhir.id()))
        .rxnorm(rxNorm(fhir.code()))
        .product(datamartProduct(fhir.product()))
        .build();
  }

  private Optional<DatamartMedication.RxNorm> rxNorm(CodeableConcept cc) {
    if (cc == null) {
      return null;
    }
    if (cc.coding() == null || cc.coding().isEmpty()) {
      return cc.text() == null
          ? null
          : Optional.of(DatamartMedication.RxNorm.builder().text(cc.text()).build());
    }
    return Optional.of(
        DatamartMedication.RxNorm.builder()
            .code(cc.coding().get(0).code())
            .text(cc.coding().get(0).display())
            .build());
  }
}
