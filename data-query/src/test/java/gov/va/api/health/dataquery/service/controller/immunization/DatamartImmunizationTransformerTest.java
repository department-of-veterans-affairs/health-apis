package gov.va.api.health.dataquery.service.controller.immunization;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.Status;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunizationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunizationSamples.Fhir;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.DataAbsentReason.Reason;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartImmunizationTransformerTest {
  @Test
  public void immunization() {
    assertThat(json(tx(Datamart.create().immunization()).toFhir()))
        .isEqualTo(json(Fhir.create().immunization()));
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void note() {
    DatamartImmunizationTransformer tx =
        tx(DatamartImmunizationSamples.Datamart.create().immunization());
    assertThat(tx.note(Optional.empty())).isNull();
    assertThat(tx.note(Optional.of("hello"))).isEqualTo(Fhir.create().note("hello"));
  }

  @Test
  public void reaction() {
    DatamartImmunizationTransformer tx =
        tx(DatamartImmunizationSamples.Datamart.create().immunization());

    assertThat(tx.reaction(Optional.empty())).isNull();
    assertThat(tx.reaction(Optional.of(Datamart.create().reaction())))
        .isEqualTo(Fhir.create().reactions());
  }

  @Test
  public void status() {
    DatamartImmunizationTransformer tx =
        tx(DatamartImmunizationSamples.Datamart.create().immunization());
    assertThat(tx.status(null)).isNull();
    assertThat(tx.status(DatamartImmunization.Status.completed))
        .isEqualTo(Immunization.Status.completed);
    assertThat(tx.status(DatamartImmunization.Status.entered_in_error))
        .isEqualTo(Immunization.Status.entered_in_error);
    assertThat(tx.status(Status.data_absent_reason_unsupported)).isNull();
  }

  @Test
  public void statusExtension() {
    DatamartImmunizationTransformer tx =
        tx(DatamartImmunizationSamples.Datamart.create().immunization());
    assertThat(tx.statusExtension(DatamartImmunization.Status.completed)).isNull();
    assertThat(tx.statusExtension(DatamartImmunization.Status.entered_in_error)).isNull();
    assertThat(tx.statusExtension(Status.data_absent_reason_unsupported))
        .isEqualTo(DataAbsentReason.of(Reason.unsupported));
  }

  DatamartImmunizationTransformer tx(DatamartImmunization dm) {
    return DatamartImmunizationTransformer.builder().datamart(dm).build();
  }

  @Test
  public void vaccineCode() {
    DatamartImmunizationTransformer tx =
        tx(DatamartImmunizationSamples.Datamart.create().immunization());
    assertThat(tx.vaccineCode(Datamart.create().vaccineCode()))
        .isEqualTo(Fhir.create().vaccineCode());
  }
}
