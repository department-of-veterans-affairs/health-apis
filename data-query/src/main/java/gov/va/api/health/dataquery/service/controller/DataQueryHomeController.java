package gov.va.api.health.dataquery.service.controller;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SuppressWarnings("WeakerAccess")
@Controller
public class DataQueryHomeController {
  private static final YAMLMapper MAPPER = new YAMLMapper();

  private final Resource dstu2Openapi;

  @Autowired
  public DataQueryHomeController(@Value("classpath:/dstu2-openapi.json") Resource dstu2Openapi) {
    this.dstu2Openapi = dstu2Openapi;
  }

  /**
   * Provide access to the OpenAPI as JSON via RESTful interface. This is also used as the /
   * redirect.
   */
  @GetMapping(
      value = {"dstu2/", "dstu2/openapi.json", "dstu2-openapi.json"},
      produces = "application/json")
  @ResponseBody
  public Object dstu2OpenapiJson() throws IOException {
    return DataQueryHomeController.MAPPER.readValue(openapiContent(dstu2Openapi), Object.class);
  }

  /** The OpenAPI specific content. */
  @SuppressWarnings("WeakerAccess")
  public String openapiContent(Resource openapi) throws IOException {
    try (InputStream is = openapi.getInputStream()) {
      return StreamUtils.copyToString(is, Charset.defaultCharset());
    }
  }
}
