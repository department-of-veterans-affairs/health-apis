package gov.va.api.health.dataquery.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.junit.Test;

public class ArgoEthnicityExtensionValidationTest {
  private final SampleExtensions data = SampleExtensions.get();

  @Test
  public void patientWithMultipleOptionalEthnicityExtensionIsValid() {
    assertThat(violationsOf(data.patientWithMultipleOptionalEthnicityExtension())).isEmpty();
  }

  @Test
  public void patientWithNoRequiredEthnicityExtensionIsNotValid() {
    assertThat(violationsOf(data.patientWithNoRequiredEthnicityExtension())).isNotEmpty();
  }

  @Test
  public void patientWithNullEthnicityExtensionIsValid() {
    assertThat(violationsOf(data.patientWithNullEthnicityExtension())).isEmpty();
  }

  @Test
  public void patientWithSingleOptionalEthnicityExtensionIsValid() {
    assertThat(violationsOf(data.patientWithSingleOptionalEthnicityExtension())).isEmpty();
  }

  @Test
  public void patientWithSingleRequiredEthnicityExtensionIsValid() {
    assertThat(violationsOf(data.patientWithSingleRequiredEthnicityExtension())).isEmpty();
  }

  @Test
  public void patientWithTooManyOptionalEthnicityExtensionIsNotValid() {
    assertThat(violationsOf(data.patientWithTooManyOptionalEthnicityExtension())).isNotEmpty();
  }

  @Test
  public void patientWithTooManyRequiredEthnicityExtensionIsNotValid() {
    assertThat(violationsOf(data.patientWithTooManyRequiredEthnicityExtension())).isNotEmpty();
  }

  private <T> Set<ConstraintViolation<T>> violationsOf(T object) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator().validate(object);
  }
}
