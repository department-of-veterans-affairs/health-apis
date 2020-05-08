package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.uscorer4.api.resources.Patient;
import org.junit.Test;

public class R4GenderMappingTest {

  @Test
  public void genderMappingToCdwIsValid() {
    assertThat(R4GenderMapping.toCdw("MALE")).isEqualTo("M");
    assertThat(R4GenderMapping.toCdw("male")).isEqualTo("M");
    assertThat(R4GenderMapping.toCdw("FEMALE")).isEqualTo("F");
    assertThat(R4GenderMapping.toCdw("fEmAlE")).isEqualTo("F");
    assertThat(R4GenderMapping.toCdw("OTHER")).isEqualTo("*Missing*");
    assertThat(R4GenderMapping.toCdw("UNKNOWN")).isEqualTo("*Unknown at this time*");
    assertThat(R4GenderMapping.toCdw("")).isEqualTo("*Unknown at this time*");
    assertThat(R4GenderMapping.toCdw("M")).isEqualTo("*Unknown at this time*");
    assertThat(R4GenderMapping.toCdw("?!")).isEqualTo("*Unknown at this time*");
  }

  @Test
  public void genderMappingToFhirIsValid() {
    assertThat(R4GenderMapping.toFhir("M")).isEqualTo(Patient.Gender.male);
    assertThat(R4GenderMapping.toFhir("m")).isEqualTo(Patient.Gender.male);
    assertThat(R4GenderMapping.toFhir("F")).isEqualTo(Patient.Gender.female);
    assertThat(R4GenderMapping.toFhir("*MISSING*")).isEqualTo(Patient.Gender.other);
    assertThat(R4GenderMapping.toFhir("*mIssIng*")).isEqualTo(Patient.Gender.other);
    assertThat(R4GenderMapping.toFhir("*UNKNOWN AT THIS TIME*")).isEqualTo(Patient.Gender.unknown);
    assertThat(R4GenderMapping.toFhir("-UNKNOWN AT THIS TIME-")).isEqualTo(Patient.Gender.unknown);
    assertThat(R4GenderMapping.toFhir("")).isEqualTo(Patient.Gender.unknown);
    assertThat(R4GenderMapping.toFhir("male")).isEqualTo(Patient.Gender.unknown);
  }
}
