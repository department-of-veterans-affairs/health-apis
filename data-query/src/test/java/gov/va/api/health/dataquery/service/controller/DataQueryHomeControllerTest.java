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
    // Full Url
    mvc.perform(get(basePath + "/openapi.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi", equalTo("3.0.1")))
        .andExpect(jsonPath("$.info.title", equalTo("Argonaut Data Query")));
    mvc.perform(get("/dstu2-openapi.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi", equalTo("3.0.1")))
        .andExpect(jsonPath("$.info.title", equalTo("Argonaut Data Query")));
    // From index i.e. dstu2/
    mvc.perform(get(basePath + "/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi", equalTo("3.0.1")))
        .andExpect(jsonPath("$.info.title", equalTo("Argonaut Data Query")));
  }
}
