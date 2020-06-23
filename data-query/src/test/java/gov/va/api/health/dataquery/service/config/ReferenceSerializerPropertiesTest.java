package gov.va.api.health.dataquery.service.config;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dstu2.api.elements.Reference;
import org.junit.jupiter.api.Test;

public class ReferenceSerializerPropertiesTest {
  @Test
  public void isEnabled() {
    ReferenceSerializerProperties testProperties =
        ReferenceSerializerProperties.builder()
            .location(true)
            .organization(false)
            .practitioner(true)
            .build();

    assertThat(testProperties.isEnabled(Reference.builder().build())).isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("").build())).isTrue();
    assertThat(testProperties.isEnabled((Reference) null)).isTrue();

    assertThat(testProperties.isEnabled(Reference.builder().reference("Location/1234").build()))
        .isTrue();
    assertThat(
            testProperties.isEnabled(
                Reference.builder().reference("http://localhost:90001/api/Location/1234").build()))
        .isTrue();

    assertThat(testProperties.isEnabled(Reference.builder().reference("/1234").build())).isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("location/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("/location/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("/Location/1234").build()))
        .isTrue();
    assertThat(
            testProperties.isEnabled(
                Reference.builder().reference("http://localhost:90001/api/location/1234").build()))
        .isTrue();

    assertThat(testProperties.isEnabled(Reference.builder().reference("Organization/1234").build()))
        .isFalse();
    assertThat(
            testProperties.isEnabled(
                Reference.builder()
                    .reference("http://localhost:90001/api/Organization/1234")
                    .build()))
        .isFalse();

    assertThat(testProperties.isEnabled(Reference.builder().reference("Organization").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("/Organization").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("organization/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("organization/1234").build()))
        .isTrue();
    assertThat(testProperties.isEnabled(Reference.builder().reference("organization/1234").build()))
        .isTrue();
    assertThat(
            testProperties.isEnabled(Reference.builder().reference("/organization/1234").build()))
        .isTrue();
    assertThat(
            testProperties.isEnabled(
                Reference.builder()
                    .reference("http://localhost:90001/api/organization/1234")
                    .build()))
        .isTrue();

    assertThat(
            testProperties.isEnabled(Reference.builder().reference("/practitioner/5678").build()))
        .isTrue();
  }
}
