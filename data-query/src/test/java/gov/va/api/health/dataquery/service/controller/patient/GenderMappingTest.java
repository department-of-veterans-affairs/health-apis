package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.resources.Patient;
import org.junit.jupiter.api.Test;

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
  public void genderMappingToDstu2FhirIsValid() {
    assertThat(GenderMapping.toDstu2Fhir("M")).isEqualTo(Patient.Gender.male);
    assertThat(GenderMapping.toDstu2Fhir("m")).isEqualTo(Patient.Gender.male);
    assertThat(GenderMapping.toDstu2Fhir("F")).isEqualTo(Patient.Gender.female);
    assertThat(GenderMapping.toDstu2Fhir("*MISSING*")).isEqualTo(Patient.Gender.other);
    assertThat(GenderMapping.toDstu2Fhir("*mIssIng*")).isEqualTo(Patient.Gender.other);
    assertThat(GenderMapping.toDstu2Fhir("*UNKNOWN AT THIS TIME*"))
        .isEqualTo(Patient.Gender.unknown);
    assertThat(GenderMapping.toDstu2Fhir("-UNKNOWN AT THIS TIME-")).isNull();
    assertThat(GenderMapping.toDstu2Fhir("")).isNull();
    assertThat(GenderMapping.toDstu2Fhir("male")).isNull();
  }

  @Test
  public void genderMappingToR4FhirIsValid() {
    assertThat(GenderMapping.toR4Fhir("M"))
        .isEqualTo(gov.va.api.health.uscorer4.api.resources.Patient.Gender.male);
    assertThat(GenderMapping.toR4Fhir("m"))
        .isEqualTo(gov.va.api.health.uscorer4.api.resources.Patient.Gender.male);
    assertThat(GenderMapping.toR4Fhir("F"))
        .isEqualTo(gov.va.api.health.uscorer4.api.resources.Patient.Gender.female);
    assertThat(GenderMapping.toR4Fhir("*MISSING*"))
        .isEqualTo(gov.va.api.health.uscorer4.api.resources.Patient.Gender.other);
    assertThat(GenderMapping.toR4Fhir("*mIssIng*"))
        .isEqualTo(gov.va.api.health.uscorer4.api.resources.Patient.Gender.other);
    assertThat(GenderMapping.toR4Fhir("*UNKNOWN AT THIS TIME*"))
        .isEqualTo(gov.va.api.health.uscorer4.api.resources.Patient.Gender.unknown);
    assertThat(GenderMapping.toR4Fhir("-UNKNOWN AT THIS TIME-")).isNull();
    assertThat(GenderMapping.toR4Fhir("")).isNull();
    assertThat(GenderMapping.toR4Fhir("male")).isNull();
  }
}
