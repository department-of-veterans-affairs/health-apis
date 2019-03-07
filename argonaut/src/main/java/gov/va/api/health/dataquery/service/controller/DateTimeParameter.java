package gov.va.api.health.dataquery.service.controller;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/** Validate an array of strings to be valid date time parameters. */
@Documented
@Constraint(validatedBy = DateTimeParameterValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface DateTimeParameter {
  /** Required by constraint validation framework. */
  Class<?>[] groups() default {};

  /** The validation error message. */
  String message() default "Must be array of date/time search parameters";

  /** Required by constraint validation framework. */
  Class<? extends Payload>[] payload() default {};
}
