package gov.va.api.health.dataquery.service.controller.datamart;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.SneakyThrows;

public interface HasPayload<T> {
  ObjectMapper MAPPER = JacksonConfig.createMapper();

  @SneakyThrows
  default T deserialize() {
    return MAPPER.readValue(payload(), payloadType());
  }

  String payload();

  Class<T> payloadType();
}
