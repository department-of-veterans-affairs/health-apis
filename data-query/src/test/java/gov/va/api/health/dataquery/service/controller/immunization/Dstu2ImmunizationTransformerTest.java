package gov.va.api.health.dataquery.service.controller.immunization;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.Status;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationSamples.Dstu2;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.DataAbsentReason.Reason;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class Dstu2ImmunizationTransformerTest {
  @Test
  public void empty() {
    assertThat(tx(DatamartImmunization.builder().build()).toFhir())
        .isEqualTo(
            Immunization.builder()
                .resourceType("Immunization")
                .wasNotGiven(false)
                ._reported(DataAbsentReason.of(Reason.unsupported))
                .build());
  }

  @Test
  public void immunization() {
    assertThat(json(tx(Datamart.create().immunization()).toFhir()))
        .isEqualTo(json(Dstu2.create().immunization()));
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void note() {
    Dstu2ImmunizationTransformer tx = tx(ImmunizationSamples.Datamart.create().immunization());
    assertThat(tx.note(Optional.empty())).isNull();
    assertThat(tx.note(Optional.of("hello"))).isEqualTo(Dstu2.create().note("hello"));
  }

  @Test
  public void reaction() {
    Dstu2ImmunizationTransformer tx = tx(ImmunizationSamples.Datamart.create().immunization());
    assertThat(tx.reaction(Optional.empty())).isNull();
    assertThat(
            tx.reaction(
                Optional.of(
                    DatamartReference.of().type(null).reference(null).display(null).build())))
        .isNull();
    assertThat(tx.reaction(Optional.of(Datamart.create().reaction())))
        .isEqualTo(Dstu2.create().reactions());
  }

  @Test
  public void status() {
    Dstu2ImmunizationTransformer tx = tx(ImmunizationSamples.Datamart.create().immunization());
    assertThat(tx.status(null)).isNull();
    assertThat(tx.status(DatamartImmunization.Status.completed))
        .isEqualTo(Immunization.Status.completed);
    assertThat(tx.status(DatamartImmunization.Status.entered_in_error))
        .isEqualTo(Immunization.Status.entered_in_error);
    assertThat(tx.status(Status.data_absent_reason_unsupported)).isNull();
  }

  @Test
  public void statusExtension() {
    Dstu2ImmunizationTransformer tx = tx(ImmunizationSamples.Datamart.create().immunization());
    assertThat(tx.statusExtension(DatamartImmunization.Status.completed)).isNull();
    assertThat(tx.statusExtension(DatamartImmunization.Status.entered_in_error)).isNull();
    assertThat(tx.statusExtension(Status.data_absent_reason_unsupported))
        .isEqualTo(DataAbsentReason.of(Reason.unsupported));
  }

  Dstu2ImmunizationTransformer tx(DatamartImmunization dm) {
    return Dstu2ImmunizationTransformer.builder().datamart(dm).build();
  }

  @Test
  public void vaccineCode() {
    Dstu2ImmunizationTransformer tx = tx(ImmunizationSamples.Datamart.create().immunization());
    assertThat(tx.vaccineCode(Datamart.create().vaccineCode()))
        .isEqualTo(Dstu2.create().vaccineCode());
  }
}
