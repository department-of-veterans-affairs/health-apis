package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dstu2.api.resources.Patient.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class Dstu2PatientTransformerTest {
  @Test
  void empty() {
    assertThat(
            Dstu2PatientTransformer.builder()
                .datamart(DatamartPatient.builder().build())
                .build()
                .toFhir())
        .isEqualTo(builder().resourceType("Patient").gender(Gender.unknown).build());
  }

  @Test
  void gender() {
    assertThat(genderOf("M")).isEqualTo(Gender.male);
    assertThat(genderOf("F")).isEqualTo(Gender.female);
    assertThat(genderOf("unknown")).isEqualTo(Gender.unknown);
    assertThat(genderOf("female")).isEqualTo(Gender.female);
    assertThat(genderOf(null)).isEqualTo(Gender.unknown);
    assertThat(genderOf("shanktopus")).isEqualTo(Gender.other);
  }

  private Gender genderOf(String birthGender) {
    return Dstu2PatientTransformer.builder()
        .datamart(DatamartPatient.builder().gender(birthGender).build())
        .build()
        .toFhir()
        .gender();
  }
}
