package gov.va.api.health.dataquery.service.controller.procedure;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure.Status;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureSamples.Dstu2;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class Dstu2ProcedureTransformerTest {
  @Test
  public void empty() {
    assertThat(tx(DatamartProcedure.builder().build()).toFhir())
        .isEqualTo(Procedure.builder().resourceType("Procedure").notPerformed(false).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void procedure() {
    assertThat(json(tx(Datamart.create().procedure()).toFhir()))
        .isEqualTo(json(Dstu2.create().procedure()));
  }

  @Test
  public void reasonNotPerformed() {
    Dstu2ProcedureTransformer tx = tx(Datamart.create().procedure());
    assertThat(tx.reasonNotPerformed(Optional.empty())).isNull();
    assertThat(tx.reasonNotPerformed(Optional.of("cuz.")))
        .isEqualTo(List.of(CodeableConcept.builder().text("cuz.").build()));
  }

  @Test
  public void status() {
    Dstu2ProcedureTransformer tx = tx(Datamart.create().procedure());
    assertThat(tx.status(Status.in_progress)).isEqualTo(Procedure.Status.in_progress);
    assertThat(tx.status(Status.aborted)).isEqualTo(Procedure.Status.aborted);
    assertThat(tx.status(Status.completed)).isEqualTo(Procedure.Status.completed);
    assertThat(tx.status(Status.cancelled)).isEqualTo(Procedure.Status.entered_in_error);
  }

  Dstu2ProcedureTransformer tx(DatamartProcedure dm) {
    return Dstu2ProcedureTransformer.builder().datamart(dm).build();
  }
}
