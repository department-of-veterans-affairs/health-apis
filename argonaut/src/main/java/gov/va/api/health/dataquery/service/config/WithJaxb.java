package gov.va.api.health.dataquery.service.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.beans.factory.annotation.Qualifier;

/** Qualifier for RestTemplate that support JAXB data types. */
@Qualifier
@Target({
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.TYPE,
  ElementType.PARAMETER,
  ElementType.CONSTRUCTOR
})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithJaxb {}
