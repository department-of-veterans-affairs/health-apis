package gov.va.api.health.dataquery.service.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ResourceNameTranslationTest {
  @Test
  public void conversion() {
    assertThat(ResourceNameTranslation.get().fhirToIdentityService("Single"), is("SINGLE"));
    assertThat(
        ResourceNameTranslation.get().fhirToIdentityService("DoubleWord"), is("DOUBLE_WORD"));
    assertThat(
        ResourceNameTranslation.get().fhirToIdentityService("SoManyWords"), is("SO_MANY_WORDS"));

    assertThat(ResourceNameTranslation.get().identityServiceToFhir("SINGLE"), is("Single"));
    assertThat(
        ResourceNameTranslation.get().identityServiceToFhir("DOUBLE_WORD"), is("DoubleWord"));
    assertThat(
        ResourceNameTranslation.get().identityServiceToFhir("SO_MANY_WORDS"), is("SoManyWords"));
  }
}
