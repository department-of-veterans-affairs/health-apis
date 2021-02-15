package gov.va.api.health.dataquery.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartSupport {

  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  public static final ObjectMapper mapper() {
    return MAPPER;
  }
}
