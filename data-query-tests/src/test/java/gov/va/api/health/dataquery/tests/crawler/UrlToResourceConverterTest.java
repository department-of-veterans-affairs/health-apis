package gov.va.api.health.dataquery.tests.crawler;

import static gov.va.api.health.dataquery.tests.EnvironmentAssumptions.assumeLocal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.tests.crawler.UrlToResourceConverter.DoNotUnderstandUrl;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.resources.Resource;
import gov.va.api.health.fhir.api.FhirVersion;
import gov.va.api.health.stu3.api.resources.Location;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UrlToResourceConverterTest {
  UrlToResourceConverter converter =
      UrlToResourceConverter.builder()
          .bundleClass(AbstractBundle.class)
          .resourcePackages(List.of(Patient.class.getPackage(), Resource.class.getPackage()))
          .build();

  @Test
  public void conversions() {
    assertThat(converter.apply("https://argonaut.com/api/Patient/123")).isEqualTo(Patient.class);
    assertThat(converter.apply("https://argonaut.com/api/Patient?_id=123"))
        .isEqualTo(Patient.Bundle.class);
    assertThat(converter.apply("http://something.com/what/ever/Medication/123"))
        .isEqualTo(Medication.class);
    assertThat(converter.apply("http://something.com/what/ever/Medication/123?oh=boy"))
        .isEqualTo(Medication.class);
    assertThat(converter.apply("http://something.com/what/ever/MedicationStatement?patient=123"))
        .isEqualTo(MedicationStatement.Bundle.class);

    assertThat(
            converter.apply(
                "http://something.com/what/ever/MedicationStatement?what=ever&patient=123&who=cares"))
        .isEqualTo(MedicationStatement.Bundle.class);
  }

  @Test
  public void forFhirVersion() {
    assertThat(
            UrlToResourceConverter.forFhirVersion(FhirVersion.DSTU2)
                .apply("https://argonaut.com/api/Patient/123"))
        .isEqualTo(Patient.class);
    assertThat(
            UrlToResourceConverter.forFhirVersion(FhirVersion.STU3)
                .apply("https://argonaut.com/api/Location/123"))
        .isEqualTo(Location.class);
    assertThat(
            UrlToResourceConverter.forFhirVersion(FhirVersion.R4)
                .apply("https://argonaut.com/api/Patient/123"))
        .isEqualTo(gov.va.api.health.uscorer4.api.resources.Patient.class);
  }

  @Test
  public void noSuchClass() {
    assertThrows(
        DoNotUnderstandUrl.class,
        () -> converter.apply("http://something.com/api/WhatIsThisClass/123"));
  }

  @Test
  public void notParseable() {
    assertThrows(
        DoNotUnderstandUrl.class, () -> converter.apply("http://what-is-this-nonsense.com"));
  }

  @Test
  public void relativeUrlsNotSupported() {
    assertThrows(DoNotUnderstandUrl.class, () -> converter.apply("/api/Patient/123"));
  }
}
