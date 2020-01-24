package gov.va.api.health.dataquery.service.config;

import gov.va.api.health.dataquery.service.controller.CountParameterResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This configures spring to support additional, custom HTTP media types for FHIR. We need to
 * support application/json, application/fhir+json, and application/json+fhir. Out of the box,
 * Spring will support application/*+json, but special configuration is needed for
 * application/json+fhir
 */
@Configuration
public class FhirMediaTypesConfig implements WebMvcConfigurer {
  private static final MediaType JSON_FHIR = MediaType.parseMediaType("application/json+fhir");

  private final int maxCount;

  private final int defaultCount;

  public FhirMediaTypesConfig(
      @Value("${count-parameter.max-count:20}") int maxCount,
      @Value("${count-parameter.default-count:15}") int defaultCount) {
    this.maxCount = maxCount;
    this.defaultCount = defaultCount;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(
        CountParameterResolver.builder().defaultCount(defaultCount).maxCount(maxCount).build());
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    /*
     * Augment the standard Jackson mapper to also support application/json+fhir.
     */
    Optional<MappingJackson2HttpMessageConverter> jackson =
        converters.stream()
            .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
            .map(c -> (MappingJackson2HttpMessageConverter) c)
            .findFirst();

    if (jackson.isPresent()) {
      List<MediaType> moreMediaTypes = new ArrayList<>();
      moreMediaTypes.addAll(jackson.get().getSupportedMediaTypes());
      moreMediaTypes.add(JSON_FHIR);
      jackson.get().setSupportedMediaTypes(moreMediaTypes);
    }
  }
}
