package gov.va.api.health.mranderson.config;

import gov.va.api.health.dataquery.idsmapping.DataQueryIdsCodebookSupplier;
import gov.va.api.health.ids.client.EncryptingIdEncoder.Codebook;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdsConfiguration {

  @Bean
  @ConditionalOnMissingBean
  Codebook codebook() {
    return new DataQueryIdsCodebookSupplier().get();
  }
}
