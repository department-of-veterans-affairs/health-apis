package gov.va.api.health.dataquery.service.controller.practitioner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DatamartPractitionerTransformerTest {

  @Test
  public void practitioner() {
    assertThat(tx(DatamartPractitionerSamples.Datamart.create().practitioner()).toFhir())
        .isEqualTo(DatamartPractitionerSamples.Datamart.Fhir.create().practitioner());
  }

  DatamartPractitionerTransformer tx(DatamartPractitioner dm) {
    return DatamartPractitionerTransformer.builder().datamart(dm).build();
  }
}
