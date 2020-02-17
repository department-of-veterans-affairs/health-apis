package gov.va.api.health.dataquery.service.controller.datamart;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import lombok.SneakyThrows;

public interface DatamartEntity {
  String cdwId();

  /** Deserialize datamart payload. */
  @SneakyThrows
  default <T> T deserializeDatamart(String payload, Class<T> clazz) {
    if (payload == null) {
      throw new ResourceExceptions.InvalidDatamartPayload(cdwId());
    }
    try {
      return JacksonConfig.createMapper()
          .readValue(payload.substring(0, payload.length() - 2), clazz);
    } catch (Exception e) {
      throw new ResourceExceptions.InvalidDatamartPayload(cdwId(), e);
    }
  }
}
