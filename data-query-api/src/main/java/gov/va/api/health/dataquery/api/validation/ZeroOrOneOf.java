package gov.va.api.health.dataquery.api.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Constraint that indicates a class can have at most one of a collection of fields sets. The this
 * allows all fields in this group to be null, but disallows two or more to be set.
 *
 * <p>Consider the following example.
 *
 * <ul>
 *   <li>a and b may both be null
 *   <li>a or b may be set, but not both
 *   <li>x is not considered by this validation
 * </ul>
 *
 * <pre>
 * &#064;ZeroOrOneOf({"a","b"})
 * class Foo {
 *   String a;
 *   String b;
 *   String x;
 * }
 * </pre>
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ZeroOrOneOfValidator.class)
@Documented
public @interface ZeroOrOneOf {
  /** @return The fields */
  String[] fields();

  /** Assigns default set of constraints during validation */
  Class<?>[] groups() default {};

  /** Assigns message for when validation fails. */
  String message() default "Only one value in this group may be specified";

  /** Assigns default payload to constraints */
  Class<? extends Payload>[] payload() default {};
}
