package gov.va.api.health.dataquery.service.controller.datamart;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.SneakyThrows;

public interface DatamartEntity {
  @SneakyThrows
  static <T> T deserializeDatamart(String payload, Class<T> clazz) {
    return JacksonConfig.createMapper().readValue(payload, clazz);
  }

  String cdwId();
}
