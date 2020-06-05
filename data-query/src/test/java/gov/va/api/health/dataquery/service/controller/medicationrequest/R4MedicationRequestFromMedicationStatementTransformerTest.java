package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementSamples;
import org.junit.Test;

public class R4MedicationRequestFromMedicationStatementTransformerTest {

  @Test
  public void toFhir() {
    DatamartMedicationStatement dms =
        MedicationStatementSamples.Datamart.create().medicationStatement();

    assertThat(
            R4MedicationRequestFromMedicationStatementTransformer.builder()
                .datamart(dms)
                .build()
                .toFhir())
        .isEqualTo(MedicationRequestSamples.R4.create().medicationRequestFromMedicationStatement());
  }
}
