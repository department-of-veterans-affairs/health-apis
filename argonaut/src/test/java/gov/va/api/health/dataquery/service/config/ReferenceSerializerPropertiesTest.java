package gov.va.api.health.dataquery.service.config;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.elements.Reference;
import org.junit.Test;

public class ReferenceSerializerPropertiesTest {

  @Test
  public void isEnabled() {
    ReferenceSerializerProperties testProperties =
        ReferenceSerializerProperties.builder()
            .appointment(true)
            .encounter(false)
            .location(true)
            .organization(false)
            .practitioner(true)
            .medicationDispense(true)
            .build();

    assertThat(testProperties.isEnabled(Reference.builder().build())).isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("").build())).isTrue();
    assertThat(testProperties.isEnabled(null)).isTrue();

    assertThat(testProperties.isEnabled(Reference.builder().reference("Appointment/1234").build()))
        .isTrue();
    assertThat(
            testProperties.isEnabled(
                Reference.builder()
                    .reference("http://localhost:90001/api/Appointment/1234")
                    .build()))
        .isTrue();

    assertThat(testProperties.isEnabled(Reference.builder().reference("Appointment").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("/Appointment").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("appointment/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("appointment/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("/appointment/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("/Appointment/1234").build()))
        .isTrue();
    assertThat(
            testProperties.isEnabled(
                Reference.builder()
                    .reference("http://localhost:90001/api/appointment/1234")
                    .build()))
        .isTrue();

    assertThat(testProperties.isEnabled(Reference.builder().reference("Encounter/1234").build()))
        .isFalse();
    assertThat(
            testProperties.isEnabled(
                Reference.builder().reference("http://localhost:90001/api/Encounter/1234").build()))
        .isFalse();

    assertThat(testProperties.isEnabled(Reference.builder().reference("Encounter").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("/Encounter").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("encounter/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("encounter/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("encounter/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("/encounter/1234").build()))
        .isTrue();
    assertThat(
            testProperties.isEnabled(
                Reference.builder().reference("http://localhost:90001/api/encounter/1234").build()))
        .isTrue();

    assertThat(
            testProperties.isEnabled(
                Reference.builder().reference("/MedicationDispense/5678").build()))
        .isTrue();
  }
}
