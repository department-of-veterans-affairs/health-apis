package gov.va.api.health.dataquery.service.controller.procedure;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure.Status;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.uscorer4.api.resources.Procedure;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4ProcedureTransformerTest {

  @Test
  void empty() {
    assertThat(
            R4ProcedureTransformer.builder()
                .datamart(DatamartProcedure.builder().build())
                .build()
                .toFhir())
        .isEqualTo(Procedure.builder().resourceType("Procedure").build());
  }

  @Test
  void status() {
    assertThat(R4ProcedureTransformer.status(null)).isEqualTo(null);
    assertThat(R4ProcedureTransformer.status(Status.in_progress))
        .isEqualTo(Procedure.Status.in_progress);
    assertThat(R4ProcedureTransformer.status(Status.aborted)).isEqualTo(Procedure.Status.stopped);
    assertThat(R4ProcedureTransformer.status(Status.cancelled))
        .isEqualTo(Procedure.Status.not_done);
    assertThat(R4ProcedureTransformer.status(Status.completed))
        .isEqualTo(Procedure.Status.completed);
  }

  @Test
  void statusReason() {
    assertThat(R4ProcedureTransformer.statusReason(Optional.empty())).isEqualTo(null);
    assertThat(R4ProcedureTransformer.statusReason(Optional.of("reasons")))
        .isEqualTo(CodeableConcept.builder().text("reasons").build());
  }

  @Test
  void toFhir() {
    DatamartProcedure dm = ProcedureSamples.Datamart.create().procedure();
    assertThat(R4ProcedureTransformer.builder().datamart(dm).build().toFhir())
        .isEqualTo(ProcedureSamples.R4.create().procedure());
  }
}
