package gov.va.api.health.dataquery.service.controller;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SuppressWarnings("WeakerAccess")
@Controller
public class DataQueryHomeController {
  private static final YAMLMapper MAPPER = new YAMLMapper();

  @Autowired
  public DataQueryHomeController() {}

  /** The OpenAPI specific content in yaml form. */
  @SuppressWarnings("WeakerAccess")
  @Bean
  public String openapiContent() throws IOException {
    return "";
  }

  /**
   * Provide access to the OpenAPI as JSON via RESTful interface. This is also used as the /
   * redirect.
   */
  @GetMapping(
    value = {"dstu2/", "dstu2/openapi.json"},
    produces = "application/json"
  )
  @ResponseBody
  public Object openapiJson() throws IOException {
    return DataQueryHomeController.MAPPER.readValue(openapiContent(), Object.class);
  }

  /** Provide access to the OpenAPI yaml via RESTful interface. */
  @GetMapping(
    value = {"/dstu2/openapi.yaml"},
    produces = "application/vnd.oai.openapi"
  )
  @ResponseBody
  public String openapiYaml() throws IOException {
    return openapiContent();
  }
}
