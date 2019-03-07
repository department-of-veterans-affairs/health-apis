package gov.va.api.health.dataquery.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This mapper provides additional configuration that treats Argonaut Data Query reference objects
 * special. It will fully qualify relative reference links.
 */
@Configuration
public class DataQueryJacksonMapper {
  /** Configure Jackson to magically deal with references. */
  private final MagicReferenceConfig magicReferences;

  /** Custom Data Query Jackson Mapper for serialization. */
  @Autowired
  public DataQueryJacksonMapper(MagicReferenceConfig magicReferences) {
    this.magicReferences = magicReferences;
  }

  /**
   * Return a ready to use mapper that will work with classes adhering to the conventions described
   * in the class-level documentation.
   */
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = JacksonConfig.createMapper();
    return magicReferences.configure(mapper);
  }
}
