package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.api.Fhir;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/** Verifies that the date time parameter matches a searchable date time. */
public class DateTimeParameterValidator implements ConstraintValidator<DateTimeParameter, Object> {

  private final Pattern pattern = Pattern.compile(Fhir.DATETIME_SEARCH);

  @Override
  public void initialize(DateTimeParameter unused) {
    /* Do nothing */
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    if (!(value instanceof String[])) {
      throw new IllegalArgumentException(
          "Cannot apply " + DateTimeParameter.class.getName() + " to " + value);
    }
    String[] params = (String[]) value;
    for (int i = 0; i < params.length; i++) {
      if (!pattern.matcher(params[i]).matches()) {
        return false;
      }
    }
    return true;
  }
}
