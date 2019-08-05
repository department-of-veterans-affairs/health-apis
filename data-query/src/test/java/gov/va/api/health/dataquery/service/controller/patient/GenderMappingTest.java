package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Patient;
import org.junit.Test;

public class GenderMappingTest {

  @Test
  public void genderMappingToCdwIsValid() {
    assertThat(GenderMapping.toCdw("MALE")).isEqualTo("M");
    assertThat(GenderMapping.toCdw("male")).isEqualTo("M");
    assertThat(GenderMapping.toCdw("FEMALE")).isEqualTo("F");
    assertThat(GenderMapping.toCdw("fEmAlE")).isEqualTo("F");
    assertThat(GenderMapping.toCdw("OTHER")).isEqualTo("*Missing*");
    assertThat(GenderMapping.toCdw("UNKNOWN")).isEqualTo("*Unknown at this time*");
    assertThat(GenderMapping.toCdw("")).isNull();
    assertThat(GenderMapping.toCdw("M")).isNull();
    assertThat(GenderMapping.toCdw("?!")).isNull();
  }

  @Test
  public void genderMappingToFhirIsValid() {
    assertThat(GenderMapping.toFhir("M")).isEqualTo(Patient.Gender.male);
    assertThat(GenderMapping.toFhir("m")).isEqualTo(Patient.Gender.male);
    assertThat(GenderMapping.toFhir("F")).isEqualTo(Patient.Gender.female);
    assertThat(GenderMapping.toFhir("*MISSING*")).isEqualTo(Patient.Gender.other);
    assertThat(GenderMapping.toFhir("*mIssIng*")).isEqualTo(Patient.Gender.other);
    assertThat(GenderMapping.toFhir("*UNKNOWN AT THIS TIME*")).isEqualTo(Patient.Gender.unknown);
    assertThat(GenderMapping.toFhir("-UNKNOWN AT THIS TIME-")).isNull();
    assertThat(GenderMapping.toFhir("")).isNull();
    assertThat(GenderMapping.toFhir("male")).isNull();
  }
}
