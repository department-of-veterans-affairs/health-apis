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
 * Constraint that indicates a class must have one of a collection of fields sets. This requires
 * exactly one of the fields in the group to be set.
 *
 * <p>Consider the following example.
 *
 * <ul>
 *   <li>either a or b must be set, but not both
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
@Constraint(validatedBy = ExactlyOneOfValidator.class)
@Documented
public @interface ExactlyOneOf {
  /** Return The fields. */
  String[] fields();

  /** Assigns default set of constraints during validation. */
  Class<?>[] groups() default {};

  /** Assigns message for when validation fails. */
  String message() default "Exactly one value in this group must be specified";

  /** Assigns default payload to constraints. */
  Class<? extends Payload>[] payload() default {};
}
