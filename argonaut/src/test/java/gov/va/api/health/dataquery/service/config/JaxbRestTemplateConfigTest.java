package gov.va.api.health.dataquery.service.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.SecureRestTemplateConfig;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class JaxbRestTemplateConfigTest {

  @Test
  public void jaxbSupportIsAdded() {
    RestTemplate rt = mock(RestTemplate.class);
    RestTemplateBuilder rtb = mock(RestTemplateBuilder.class);
    SecureRestTemplateConfig secureConfig = mock(SecureRestTemplateConfig.class);

    JaxbRestTemplateConfig config = new JaxbRestTemplateConfig(secureConfig);

    when(secureConfig.restTemplate(rtb)).thenReturn(rt);
    RestTemplate actual = config.jaxbRestTemplate(rtb);

    assertThat(actual).isSameAs(rt);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<HttpMessageConverter<?>>> captor = ArgumentCaptor.forClass(List.class);
    verify(rt).setMessageConverters(captor.capture());
    assertThat(captor.getValue().size()).isEqualTo(1);
    assertThat(captor.getValue().get(0)).isInstanceOf(Jaxb2RootElementHttpMessageConverter.class);
  }
}
