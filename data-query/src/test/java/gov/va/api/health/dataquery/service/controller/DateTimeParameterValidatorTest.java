package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import javax.validation.ConstraintValidatorContext;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.mockito.Mockito;

public class DateTimeParameterValidatorTest {

  @Test(expected = IllegalArgumentException.class)
  public void isValidThrowsIllegalArguementExceptionWhenNotGivenAString() {
    var v = new DateTimeParameterValidator();
    var notUsed = Mockito.mock(ConstraintValidatorContext.class);
    v.isValid(Lists.emptyList(), notUsed);
  }

  @Test
  public void validates() {
    DateTimeParameterValidator v = new DateTimeParameterValidator();
    ConstraintValidatorContext notUsed = Mockito.mock(ConstraintValidatorContext.class);
    assertThat(v.isValid(null, notUsed)).isTrue();
    assertThat(v.isValid(new String[] {}, notUsed)).isTrue();
    assertThat(v.isValid(new String[] {"nope"}, notUsed)).isFalse();
    assertThat(v.isValid(new String[] {""}, notUsed)).isFalse();
    assertThat(v.isValid(new String[] {"20050121"}, notUsed)).isFalse();
    assertThat(v.isValid(new String[] {"2005-01-21", "nope"}, notUsed)).isFalse();
    assertThat(v.isValid(new String[] {"2005-01-21"}, notUsed)).isTrue();
    assertThat(v.isValid(new String[] {"gt2005-01-21"}, notUsed)).isTrue();
    assertThat(v.isValid(new String[] {"gt2005-01-21", "lt2006"}, notUsed)).isTrue();
    assertThat(v.isValid(new String[] {"2005-01-21T07:57:00.000Z"}, notUsed)).isTrue();
    assertThat(v.isValid(new String[] {"eq2005-01-21T07:57:00.000Z"}, notUsed)).isTrue();
    assertThat(
            v.isValid(
                new String[] {"eq2005-01-21T07:57:00.000Z", "eq2005-01-21T07:57:01.000Z"}, notUsed))
        .isTrue();
  }
}
