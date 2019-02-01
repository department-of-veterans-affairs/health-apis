package gov.va.health.api.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.health.api.sentinel.categories.NotInProd;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@Category(NotInProd.class)
@Slf4j
public class SwaggerAvailabilityTest {

  DevApiPortal apiPortalPage = new DevApiPortal();

  public void checkAvailability(String url) {
    log.info("Checking {} for API Documentation", url);
    apiPortalPage.initializeDriver(url, 10);
    assertThat(apiPortalPage.getElement(By.className("swagger-ui"))).isNotNull();
    WebElement headerGroup = apiPortalPage.getElement(By.tagName("hgroup"));
    assertThat(headerGroup).isNotNull();
    WebElement link = apiPortalPage.getChild(headerGroup, By.partialLinkText("openapi.json"));
    assertThat(link).isNotNull();
    RestAssured.baseURI = link.getText();
    log.info("Testing that the discovered link {} returns 200", RestAssured.baseURI);
    assertThat(RestAssured.given().get().getStatusCode()).isEqualTo(200);
    log.info("API Documentation is available at {}", url);
    apiPortalPage.quit();
  }

  @Test
  public void checkDevDevAvailability() {
    checkAvailability("https://dev-developer.va.gov/explore/health/docs/argonaut");
  }

  @Test
  public void checkDevAvailability() {
    checkAvailability("https://developer.va.gov/explore/health/docs/argonaut");
  }
}
