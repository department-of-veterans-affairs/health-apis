package gov.va.api.health.dataquery.service.config;

import gov.va.api.health.autoconfig.configuration.SecureRestTemplateConfig;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Supplier for RestTemplate that support JAXB.
 *
 * <p>Use the @{@link WithJaxb} qualifier.
 */
@SuppressWarnings("WeakerAccess")
@Configuration
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class JaxbRestTemplateConfig {
  private final SecureRestTemplateConfig secureConfig;

  /** Create a instance of the RestTemplate that works only with JAXB data entities. */
  @Bean
  @WithJaxb
  public RestTemplate jaxbRestTemplate(@Autowired RestTemplateBuilder restTemplateBuilder) {
    RestTemplate restTemplate = secureConfig.restTemplate(restTemplateBuilder);
    List<HttpMessageConverter<?>> converters = new ArrayList<>(restTemplate.getMessageConverters());
    converters.add(new Jaxb2RootElementHttpMessageConverter());
    restTemplate.setMessageConverters(converters);
    return restTemplate;
  }
}
