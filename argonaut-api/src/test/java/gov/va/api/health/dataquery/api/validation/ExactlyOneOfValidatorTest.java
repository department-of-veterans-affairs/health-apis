package gov.va.api.health.dataquery.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

public class ExactlyOneOfValidatorTest {

  @Test
  public void exactlyOneOfDoesNotFindProblemsForProperlyCreatedObjects() {
    assertThat(violationsOf(new SingleGroup(1, null))).isEmpty();
    assertThat(violationsOf(new SingleGroup(null, 1))).isEmpty();
  }

  @Test
  public void exactlyOneOfFindsProblemsWhenConstraintsAreViolated() {
    assertThat(violationsOf(new SingleGroup(1, 1))).isNotEmpty();
    assertThat(violationsOf(new SingleGroup(null, null))).isNotEmpty();
  }

  @Test(expected = ValidationException.class)
  public void exactlyOneWithUnknownFieldThrowsException() {
    violationsOf(new PoorlyDefinedGroup(1, null));
  }

  @Test
  public void relatedFieldsDoesNotFindProblemsForProperlyCreatedObjects() {
    assertThat(violationsOf(new MultipleGroups("a", null, "b", null))).isEmpty();
    assertThat(violationsOf(new MultipleGroups(null, "a", null, "b"))).isEmpty();
  }

  @Test
  public void relatedFieldsFindsProblemsWhenConstraintsAreViolated() {
    assertThat(violationsOf(new MultipleGroups("a", "a", "b", null))).isNotEmpty();
    assertThat(violationsOf(new MultipleGroups("a", "a", "b", "b"))).isNotEmpty();
    assertThat(violationsOf(new MultipleGroups("a", null, "b", "b"))).isNotEmpty();
    assertThat(violationsOf(new MultipleGroups(null, null, null, null))).isNotEmpty();
    assertThat(violationsOf(new MultipleGroups(null, "a", null, null))).isNotEmpty();
    assertThat(violationsOf(new MultipleGroups(null, null, "b", null))).isNotEmpty();
  }

  <T> Set<ConstraintViolation<T>> violationsOf(T object) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator().validate(object);
  }

  @ExactlyOneOfs({
    @ExactlyOneOf(fields = {"a1", "a2"}),
    @ExactlyOneOf(fields = {"b1", "b2"}),
  })
  @Data
  @AllArgsConstructor
  private class MultipleGroups {
    String a1;
    String a2;
    String b1;
    String b2;
  }

  @ExactlyOneOf(fields = {"a1", "a2"})
  @Data
  @AllArgsConstructor
  private class SingleGroup {
    Integer a1;
    Integer a2;
  }

  @ExactlyOneOf(fields = {"x1", "x2"})
  @Data
  @AllArgsConstructor
  private class PoorlyDefinedGroup {
    Integer a1;
    Integer a2;
  }
}
