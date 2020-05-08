package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Patient;
import org.junit.Test;

public class Dstu2GenderMappingTest {

  @Test
  public void genderMappingToCdwIsValid() {
    assertThat(Dstu2GenderMapping.toCdw("MALE")).isEqualTo("M");
    assertThat(Dstu2GenderMapping.toCdw("male")).isEqualTo("M");
    assertThat(Dstu2GenderMapping.toCdw("FEMALE")).isEqualTo("F");
    assertThat(Dstu2GenderMapping.toCdw("fEmAlE")).isEqualTo("F");
    assertThat(Dstu2GenderMapping.toCdw("OTHER")).isEqualTo("*Missing*");
    assertThat(Dstu2GenderMapping.toCdw("UNKNOWN")).isEqualTo("*Unknown at this time*");
    assertThat(Dstu2GenderMapping.toCdw("")).isNull();
    assertThat(Dstu2GenderMapping.toCdw("M")).isNull();
    assertThat(Dstu2GenderMapping.toCdw("?!")).isNull();
  }

  @Test
  public void genderMappingToFhirIsValid() {
    assertThat(Dstu2GenderMapping.toFhir("M")).isEqualTo(Patient.Gender.male);
    assertThat(Dstu2GenderMapping.toFhir("m")).isEqualTo(Patient.Gender.male);
    assertThat(Dstu2GenderMapping.toFhir("F")).isEqualTo(Patient.Gender.female);
    assertThat(Dstu2GenderMapping.toFhir("*MISSING*")).isEqualTo(Patient.Gender.other);
    assertThat(Dstu2GenderMapping.toFhir("*mIssIng*")).isEqualTo(Patient.Gender.other);
    assertThat(Dstu2GenderMapping.toFhir("*UNKNOWN AT THIS TIME*"))
        .isEqualTo(Patient.Gender.unknown);
    assertThat(Dstu2GenderMapping.toFhir("-UNKNOWN AT THIS TIME-")).isNull();
    assertThat(Dstu2GenderMapping.toFhir("")).isNull();
    assertThat(Dstu2GenderMapping.toFhir("male")).isNull();
  }
}
