package gov.va.api.health.dataquery.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

/**
 * This class will verify fields with a given prefix are properly configured in the same ZeroOrOneOf
 * group. This class will find related fields with the same prefix and systematically test different
 * combinations to ensure they are validated as expected.
 */
@Slf4j
public class ZeroOrOneOfVerifier<T> extends AbstractRelatedFieldVerifier<T> {
  /** The prefix of the related fields. */
  private String fieldPrefix;

  @Builder
  public ZeroOrOneOfVerifier(T sample, String fieldPrefix, @Singular Collection<?> omissions) {
    super(sample, name -> name.startsWith(fieldPrefix) && !omissions.contains(name));
    this.fieldPrefix = fieldPrefix;
  }

  @Override
  public void verify() {
    log.info("Verifying {}", sample.getClass());
    /* Make sure the sample is valid before we mess it up. */
    assertProblems(0);

    /* Make sure we are valid if no fields are set. */
    unsetFields();
    assertProblems(0);

    /* Make sure setting any two fields is not ok. */
    log.info("{} fields in group {}: {}", sample.getClass().getSimpleName(), fieldPrefix, fields());
    assertThat(fields().size())
        .withFailMessage("Not enough fields in group: " + fieldPrefix)
        .isGreaterThan(1);
    String anchor = fields().get(0);
    for (int i = 1; i < fields().size(); i++) {
      unsetFields();
      setField(anchor);
      setField(fields().get(i));
      assertProblems(1);
    }
  }
}
