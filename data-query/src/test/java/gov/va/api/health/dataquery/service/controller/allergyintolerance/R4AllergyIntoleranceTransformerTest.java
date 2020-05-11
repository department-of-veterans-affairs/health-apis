package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class R4AllergyIntoleranceTransformerTest {
  @Test
  public void clinicalStatusCode() {
    assertThat(R4AllergyIntoleranceTransformer.clinicalStatusCode(null)).isNull();

    assertThat(
            R4AllergyIntoleranceTransformer.clinicalStatusCode(
                DatamartAllergyIntolerance.Status.active))
        .isEqualTo("active");

    assertThat(
            R4AllergyIntoleranceTransformer.clinicalStatusCode(
                DatamartAllergyIntolerance.Status.inactive))
        .isEqualTo("inactive");

    assertThat(
            R4AllergyIntoleranceTransformer.clinicalStatusCode(
                DatamartAllergyIntolerance.Status.resolved))
        .isEqualTo("resolved");

    assertThat(
            R4AllergyIntoleranceTransformer.clinicalStatusCode(
                DatamartAllergyIntolerance.Status.confirmed))
        .isEqualTo("active");

    assertThat(
            R4AllergyIntoleranceTransformer.clinicalStatusCode(
                DatamartAllergyIntolerance.Status.unconfirmed))
        .isNull();

    assertThat(
            R4AllergyIntoleranceTransformer.clinicalStatusCode(
                DatamartAllergyIntolerance.Status.refuted))
        .isNull();

    assertThat(
            R4AllergyIntoleranceTransformer.clinicalStatusCode(
                DatamartAllergyIntolerance.Status.entered_in_error))
        .isNull();
  }

  @Test
  public void toFhir() {
    DatamartAllergyIntolerance dm =
        AllergyIntoleranceSamples.Datamart.create().allergyIntolerance();
    assertThat(R4AllergyIntoleranceTransformer.builder().datamart(dm).build().toFhir())
        .isEqualTo(AllergyIntoleranceSamples.R4.create().allergyIntolerance());
  }

  @Test
  public void verificationStatusCode() {
    assertThat(R4AllergyIntoleranceTransformer.verificationStatusCode(null)).isNull();

    assertThat(
            R4AllergyIntoleranceTransformer.verificationStatusCode(
                DatamartAllergyIntolerance.Status.unconfirmed))
        .isEqualTo("unconfirmed");

    assertThat(
            R4AllergyIntoleranceTransformer.verificationStatusCode(
                DatamartAllergyIntolerance.Status.confirmed))
        .isNull();

    assertThat(
            R4AllergyIntoleranceTransformer.verificationStatusCode(
                DatamartAllergyIntolerance.Status.refuted))
        .isEqualTo("refuted");

    assertThat(
            R4AllergyIntoleranceTransformer.verificationStatusCode(
                DatamartAllergyIntolerance.Status.entered_in_error))
        .isEqualTo("entered-in-error");

    assertThat(
            R4AllergyIntoleranceTransformer.verificationStatusCode(
                DatamartAllergyIntolerance.Status.active))
        .isNull();

    assertThat(
            R4AllergyIntoleranceTransformer.verificationStatusCode(
                DatamartAllergyIntolerance.Status.inactive))
        .isNull();

    assertThat(
            R4AllergyIntoleranceTransformer.verificationStatusCode(
                DatamartAllergyIntolerance.Status.resolved))
        .isNull();
  }
}
