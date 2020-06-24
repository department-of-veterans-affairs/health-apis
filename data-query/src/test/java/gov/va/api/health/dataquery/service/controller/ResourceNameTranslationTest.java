package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ResourceNameTranslationTest {
  @Test
  public void conversion() {
    assertThat(ResourceNameTranslation.get().fhirToIdentityService("Single")).isEqualTo("SINGLE");
    assertThat(ResourceNameTranslation.get().fhirToIdentityService("DoubleWord"))
        .isEqualTo("DOUBLE_WORD");
    assertThat(ResourceNameTranslation.get().fhirToIdentityService("SoManyWords"))
        .isEqualTo("SO_MANY_WORDS");
    assertThat(ResourceNameTranslation.get().identityServiceToFhir("SINGLE")).isEqualTo("Single");
    assertThat(ResourceNameTranslation.get().identityServiceToFhir("DOUBLE_WORD"))
        .isEqualTo("DoubleWord");
    assertThat(ResourceNameTranslation.get().identityServiceToFhir("SO_MANY_WORDS"))
        .isEqualTo("SoManyWords");
  }
}
