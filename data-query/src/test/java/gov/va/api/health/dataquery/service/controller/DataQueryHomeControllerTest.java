package gov.va.api.health.dataquery.service.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {DataQueryHomeController.class})
public class DataQueryHomeControllerTest {

  private final String basePath = "/dstu2";

  @Autowired private MockMvc mvc;

  @Test
  @SneakyThrows
  public void openapiJson() {
    mvc.perform(get(basePath + "/openapi.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi", equalTo("3.0.1")));
    mvc.perform(get(basePath + "/openapi.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi", equalTo("3.0.1")));
  }

  @Test
  @SneakyThrows
  public void openapiYaml() {
    String expected =
        StreamUtils.copyToString(
            getClass().getResourceAsStream("/openapi.yaml"), StandardCharsets.UTF_8);
    mvc.perform(get(basePath + "/openapi.yaml"))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo(expected)));
  }

  @Test
  @SneakyThrows
  public void openapiYamlFromIndex() {
    mvc.perform(get(basePath + "/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi", equalTo("3.0.1")));
  }
}
