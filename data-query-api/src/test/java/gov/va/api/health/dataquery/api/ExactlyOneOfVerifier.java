package gov.va.api.health.dataquery.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExactlyOneOfVerifier<T> extends AbstractRelatedFieldVerifier<T> {

  private String fieldPrefix;

  @Builder
  public ExactlyOneOfVerifier(T sample, String fieldPrefix, @Singular Collection<?> omissions) {
    super(sample, name -> name.startsWith(fieldPrefix) && !omissions.contains(name));
    this.fieldPrefix = fieldPrefix;
  }

  @Override
  public void verify() {
    log.info("Verifying {}", sample.getClass());
    assertProblems(0);

    /* Must have at least 1 field set. */
    unsetFields();
    assertProblems(1);

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
