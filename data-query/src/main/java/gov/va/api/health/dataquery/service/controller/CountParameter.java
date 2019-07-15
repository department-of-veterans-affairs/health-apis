package gov.va.api.health.dataquery.service.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be applied to `_count` request parameters. Combined with the {@link
 * CountParameterResolver}, it will manage safe count defaults.
 *
 * <p>THIS PARAMETER REPLACES &#064;RequestParam("_count")
 *
 * <p>Usage:
 *
 * <pre>
 *  &#064;GetMapping(params = {"patient"})
 *   public Thing.Bundle searchByPatient(
 *       &#064;RequestParam("patient") String patient,
 *       &#064;RequestParam(value = "page", defaultValue = "1") &#064;Min(1) int page,
 *       &#064;CountParameter &#064;Min(0) int count) {
 *    ...
 *  }
 * </pre>
 *
 * <p>See http://hl7.org/fhir/DSTU2/search.html#count
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CountParameter {}
