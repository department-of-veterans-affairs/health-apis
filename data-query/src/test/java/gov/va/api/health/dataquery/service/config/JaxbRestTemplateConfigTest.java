package gov.va.api.health.dataquery.service.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.SecureRestTemplateConfig;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class JaxbRestTemplateConfigTest {
  @Test
  public void jaxbSupportIsAdded() {
    RestTemplate rt = mock(RestTemplate.class);
    List<HttpMessageConverter<?>> converters = new ArrayList<>();
    when(rt.getMessageConverters()).thenReturn(converters);

    SecureRestTemplateConfig secureConfig = mock(SecureRestTemplateConfig.class);
    RestTemplateBuilder rtb = mock(RestTemplateBuilder.class);
    when(secureConfig.restTemplate(rtb)).thenReturn(rt);

    JaxbRestTemplateConfig config = new JaxbRestTemplateConfig(secureConfig);
    RestTemplate actual = config.jaxbRestTemplate(rtb);

    assertThat(actual).isSameAs(rt);
    assertThat(converters.size()).isEqualTo(1);
    assertThat(converters.get(0)).isInstanceOf(Jaxb2RootElementHttpMessageConverter.class);
  }
}
