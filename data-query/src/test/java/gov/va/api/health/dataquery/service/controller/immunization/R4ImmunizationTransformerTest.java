package gov.va.api.health.dataquery.service.controller.immunization;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.uscorer4.api.resources.Immunization;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4ImmunizationTransformerTest {

  @Test
  void empty() {
    assertThat(
            R4ImmunizationTransformer.builder()
                .datamart(DatamartImmunization.builder().build())
                .build()
                .toFhir())
        .isEqualTo(Immunization.builder().resourceType("Immunization").build());
  }

  @Test
  void note() {
    assertThat(R4ImmunizationTransformer.note(Optional.empty())).isNull();
    assertThat(R4ImmunizationTransformer.note(ImmunizationSamples.Datamart.create().note()))
        .isEqualTo(ImmunizationSamples.R4.create().note());
  }

  @Test
  void performer() {
    assertThat(R4ImmunizationTransformer.performer(Optional.empty())).isNull();
    assertThat(
            R4ImmunizationTransformer.performer(ImmunizationSamples.Datamart.create().performer()))
        .isEqualTo(ImmunizationSamples.R4.create().performer());
  }

  @Test
  void reaction() {
    assertThat(R4ImmunizationTransformer.reaction(Optional.empty())).isNull();
    assertThat(
            R4ImmunizationTransformer.reaction(
                Optional.of(
                    DatamartReference.of().type(null).reference(null).display(null).build())))
        .isNull();
    assertThat(
            R4ImmunizationTransformer.reaction(
                Optional.of(ImmunizationSamples.Datamart.create().reaction())))
        .isEqualTo(ImmunizationSamples.R4.create().reactions());
  }

  @Test
  void status() {
    assertThat(R4ImmunizationTransformer.status(null)).isNull();
    assertThat(R4ImmunizationTransformer.status(DatamartImmunization.Status.completed))
        .isEqualTo(Immunization.Status.completed);
    assertThat(R4ImmunizationTransformer.status(DatamartImmunization.Status.entered_in_error))
        .isEqualTo(Immunization.Status.entered_in_error);
  }

  @Test
  void vaccineCode() {
    assertThat(R4ImmunizationTransformer.vaccineCode(null)).isEqualTo(null);
    assertThat(
            R4ImmunizationTransformer.vaccineCode(
                ImmunizationSamples.Datamart.create().vaccineCode()))
        .isEqualTo(ImmunizationSamples.R4.create().vaccineCode());
  }
}
