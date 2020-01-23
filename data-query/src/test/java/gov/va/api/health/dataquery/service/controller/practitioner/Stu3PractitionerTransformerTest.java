package gov.va.api.health.dataquery.service.controller.practitioner;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.stu3.api.resources.Practitioner;
import org.junit.Test;

public class Stu3PractitionerTransformerTest {
  @Test
  public void address() {
    assertThat(Stu3PractitionerTransformer.address(null)).isNull();
  }

  @Test
  public void empty() {
    assertThat(
            Stu3PractitionerTransformer.builder()
                .datamart(DatamartPractitioner.builder().build())
                .build()
                .toFhir())
        .isEqualTo(Practitioner.builder().resourceType("Practitioner").build());
  }

  @Test
  public void telecom() {
    assertThat(Stu3PractitionerTransformer.telecom(null)).isNull();
  }
}
