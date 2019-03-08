package gov.va.api.health.dataquery.api;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.elements.Extension;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * This class will verify fields with a field required by FHIR but not available for us is
 * represented by an ExactlyOneOf annotation that follows the name + _name convention where the
 * _name is an Extension object.
 */
@Slf4j
public class ExactlyOneOfExtensionVerifier<T> extends AbstractRelatedFieldVerifier<T> {
  /** The base of the related fields, e.g. status vs _status. */
  private String baseField;

  @Builder
  public ExactlyOneOfExtensionVerifier(T sample, String field) {
    super(sample, name -> name.equals(field) || name.equals("_" + field));
    baseField = field;
  }

  @Override
  public void verify() {
    log.info("Verifying {}", sample.getClass());
    String extensionField = "_" + baseField;
    assertThat(fields()).containsExactlyInAnyOrder(baseField, extensionField);

    /* Make sure the sample is valid before we mess it up. */
    assertProblems(0);

    /* Make sure we are valid if no fields are set. */
    unsetFields();
    assertProblems(1);

    setField(baseField);
    assertProblems(0);

    unsetFields();
    setField(extensionField);
    assertProblems(0);

    assertThat(field(extensionField).getType()).isEqualTo(Extension.class);
  }
}
