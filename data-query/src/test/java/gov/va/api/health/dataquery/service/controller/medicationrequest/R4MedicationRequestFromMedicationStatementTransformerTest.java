package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementSamples;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import java.util.Optional;
import org.junit.Test;

public class R4MedicationRequestFromMedicationStatementTransformerTest {

  @Test
  public void nullDosageInstructionTest() {
    assertThat(
            R4MedicationRequestFromMedicationStatementTransformer.dosageInstructionConverter(
                Optional.empty(), null))
        .isNull();
  }

  @Test
  public void statusTests() {

    DatamartMedicationStatement.Status status = null;

    assertThat(R4MedicationRequestFromMedicationStatementTransformer.status(status)).isNull();

    status = DatamartMedicationStatement.Status.active;
    assertThat(R4MedicationRequestFromMedicationStatementTransformer.status(status))
        .isEqualTo(MedicationRequest.Status.active);
  }

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
