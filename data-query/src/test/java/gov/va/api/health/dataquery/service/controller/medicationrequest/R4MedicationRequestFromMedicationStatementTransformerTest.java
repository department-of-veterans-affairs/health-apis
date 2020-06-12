package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementSamples;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4MedicationRequestFromMedicationStatementTransformerTest {
  @Test
  public void noOptionalNote() {
    assertNull(R4MedicationRequestFromMedicationStatementTransformer.note(Optional.empty()));
  }

  @Test
  public void nullDosageInstructionTest() {
    assertNull(
        R4MedicationRequestFromMedicationStatementTransformer.dosageInstructionConverter(
            Optional.empty(), null));
  }

  @Test
  public void statusTests() {
    DatamartMedicationStatement.Status status = null;
    assertNull(R4MedicationRequestFromMedicationStatementTransformer.status(status));
    status = DatamartMedicationStatement.Status.active;
    assertEquals(
        R4MedicationRequestFromMedicationStatementTransformer.status(status),
        MedicationRequest.Status.active);
    status = DatamartMedicationStatement.Status.completed;
    assertEquals(
        R4MedicationRequestFromMedicationStatementTransformer.status(status),
        MedicationRequest.Status.completed);
  }

  @Test
  public void toFhir() {
    DatamartMedicationStatement dms =
        MedicationStatementSamples.Datamart.create().medicationStatement();
    assertEquals(
        R4MedicationRequestFromMedicationStatementTransformer.builder()
            .datamart(dms)
            .build()
            .toFhir(),
        MedicationRequestSamples.R4.create().medicationRequestFromMedicationStatement());
  }
}
