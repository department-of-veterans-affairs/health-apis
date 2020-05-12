package gov.va.api.health.dataquery.service.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {DataQueryHomeController.class})
public class DataQueryHomeControllerTest {

  @Autowired private MockMvc mvc;

  @Test
  @SneakyThrows
  public void dstu2OpenapiJson() {
    final String basePath = "/dstu2";
    testOpenapiPath(basePath + "/openapi.json", "Argonaut Data Query");
    testOpenapiPath("/dstu2-openapi.json", "Argonaut Data Query");
    testOpenapiPath(basePath + "/", "Argonaut Data Query");
  }

  @Test
  public void r4OpenapiJson() {
    final String basePath = "/r4";
    final String expectedInfoTitle = "US Core R4";
    testOpenapiPath(basePath + "/openapi.json", expectedInfoTitle);
    testOpenapiPath("/r4-openapi.json", expectedInfoTitle);
    testOpenapiPath(basePath + "/", expectedInfoTitle);
  }

  @SneakyThrows
  private void testOpenapiPath(String path, String expectedInfoTitle) {
    mvc.perform(get(path))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi", equalTo("3.0.1")))
        .andExpect(jsonPath("$.info.title", equalTo(expectedInfoTitle)));
  }
}
