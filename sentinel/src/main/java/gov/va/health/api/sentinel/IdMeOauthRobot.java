package gov.va.health.api.sentinel;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class IdMeOauthRobot {

  @Getter @NonNull private final Configuration config;

  private String code;

  private TokenExchange token;

  private void checkForBadCredentials(WebDriver driver) {
    Optional<WebElement> oops =
        findOptionalElement(
            driver,
            By.cssSelector(
                "#new_user > div.form-container.shared_structure > p.alert.alert-error"));
    if (oops.isPresent()) {
      log.error("Failed to log in: " + oops.get().getText());
      throw new IllegalStateException(
          "Failed to log in " + config.user().id() + ": " + oops.get().getText());
    }
  }

  @SneakyThrows
  private void checkForConsentForm(WebDriver driver) {
    String url = driver.getCurrentUrl();
    if (!startsWith(url, config.authorization().redirectUrl())) {
      waitForPageLoad(driver);
      /* There are two different consent forms ... */
      if (findOptionalElement(driver, By.className("consent-title")).isPresent()) {
        log.info("Granting consent to access data");
        driver.findElement(By.className("button-primary")).click();
        waitForUrlToChange(driver, url);
      } else if (findOptionalElement(driver, By.id("sr_page_title")).isPresent()) {
        log.info("Granting consent to access data");
        driver.findElement(By.className("btn-primary")).click();
        waitForUrlToChange(driver, url);
      }
      waitForPageLoad(driver);
      /* We sometimes see the server error here. */
      Optional<WebElement> errorMessage = findOptionalElement(driver, By.id("error-code"));
      if (errorMessage.isPresent()) {
        throw new IllegalStateException("Failed grant access: " + errorMessage.get().getText());
      }
    }
  }

  private void checkForMatchingError(WebDriver driver) {
    Optional<WebElement> matchingError =
        findOptionalElement(driver, By.className("usa-alert-error"));
    if (matchingError.isPresent()) {
      WebElement problem = driver.findElement(By.className("usa-alert-heading"));
      throw new IllegalStateException("Matching error: " + problem.getText());
    }
  }

  private void clickThroughFakeTwoFactorAuthentication(WebDriver driver) {
    // Continue passed authentication code send form
    log.info("Clicking through two factor authorization sham");
    String url = driver.getCurrentUrl();
    driver.findElement(By.className("btn-primary")).click();
    url = waitForUrlToChange(driver, url);
    // Continue passed entering the authentication code
    driver.findElement(By.className("btn-primary")).click();
    waitForUrlToChange(driver, url);
  }

  /** Return the authorization code, logging in if necessary. */
  @SneakyThrows
  public String code() {
    if (code != null) {
      return code;
    }

    WebDriver driver = createWebDriver();
    try {

      String url = driver.getCurrentUrl();
      enterCredentials(driver);
      checkForBadCredentials(driver);
      url = waitForUrlToChange(driver, url);
      clickThroughFakeTwoFactorAuthentication(driver);
      /*
       * There are possibly two consent forms.
       */
      url = waitForUrlToChange(driver, url);
      checkForConsentForm(driver);
      checkForConsentForm(driver);
      checkForMatchingError(driver);

      String code = extractCodeFromRedirectUrl(driver);
      log.info("Code: {}", code);
      return code;
    } catch (Exception e) {
      log.error("Failed to acquire access code: {}", e.getMessage());
      throw e;
    } finally {
      driver.close();
      driver.quit();
    }
  }

  private WebDriver createWebDriver() {
    ChromeOptions chromeOptions = new ChromeOptions();
    chromeOptions.setHeadless(config.headless());
    chromeOptions.addArguments(
        "--whitelisted-ips", "--disable-extensions", "--no-sandbox", "--disable-logging");
    if (isNotBlank(config.chromeDriver())) {
      System.setProperty("webdriver.chrome.driver", config.chromeDriver());
    }

    WebDriver driver = new ChromeDriver(chromeOptions);
    driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    return driver;
  }

  private void enterCredentials(WebDriver driver) {
    log.info("Loading {}", config.authorization().asUrl());
    driver.get(config.authorization().asUrl());
    log.info("Using Id.me");
    driver.findElement(By.className("idme-signin")).click();
    log.info("Entering credentials");
    WebElement userEmail = driver.findElement(By.id("user_email"));
    userEmail.sendKeys(config.user().id());
    WebElement userPassword = driver.findElement(By.id("user_password"));
    userPassword.sendKeys(config.user().password());
    driver.findElement(By.className("btn-primary")).click();
  }

  private String extractCodeFromRedirectUrl(WebDriver driver) {
    new WebDriverWait(driver, 1, 100)
        .until(ExpectedConditions.urlContains(config.authorization().redirectUrl()));
    String url = driver.getCurrentUrl();
    log.info("Redirected {}", url);

    return Arrays.stream(url.split("\\?")[1].split("&"))
        .filter(p -> p.startsWith("code="))
        .findFirst()
        .orElseThrow(
            () -> new RuntimeException("Cannot find code in url " + driver.getCurrentUrl()))
        .split("=")[1];
  }

  private Optional<WebElement> findOptionalElement(WebDriver driver, By by) {
    try {
      return Optional.ofNullable(driver.findElement(by));
    } catch (NoSuchElementException e) {
      return Optional.empty();
    }
  }

  /** Return the token exchange, logging in if necessary. */
  public TokenExchange token() {
    if (token != null) {
      return token;
    }
    log.info("Exchanging authorization code for token");
    TokenExchange token =
        RestAssured.given()
            .contentType(ContentType.URLENC.withCharset("UTF-8"))
            .formParam("client_id", config.authorization().clientId())
            .formParam("client_secret", config.authorization().clientSecret())
            .formParam("grant_type", "authorization_code")
            .formParam("redirect_uri", config.authorization().redirectUrl())
            .formParam("code", code())
            .log()
            .ifValidationFails()
            .post(config.tokenUrl())
            .then()
            .extract()
            .as(TokenExchange.class);
    log.info("{}", token);
    if (token.isError()) {
      throw new IllegalStateException(
          "Failed to exchange code for token: " + token.error() + "\n" + token.errorDescription());
    }
    return token;
  }

  /** Waits for the current page to completely load. */
  private void waitForPageLoad(WebDriver driver) {
    WebDriverWait wait = new WebDriverWait(driver, 30);
    wait.until(
        (ExpectedCondition<Boolean>)
            d ->
                ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState")
                    .equals("complete"));
  }

  private String waitForUrlToChange(WebDriver driver, String url) {
    new WebDriverWait(driver, 1, 100)
        .until(ExpectedConditions.not(ExpectedConditions.urlToBe(url)));
    return driver.getCurrentUrl();
  }

  @Value
  @Builder
  public static class Configuration {

    @NonNull Authorization authorization;
    @NonNull String tokenUrl;
    @NonNull UserCredentials user;
    @Default boolean headless = true;
    String chromeDriver;

    @Value
    @Builder
    public static class Authorization {
      @NonNull String authorizeUrl;
      @NonNull String redirectUrl;
      @NonNull String clientId;
      @NonNull String clientSecret;
      @NonNull String state;
      @NonNull String aud;
      @Singular Set<String> scopes;

      @SneakyThrows
      String asUrl() {
        return authorizeUrl
            + "?client_id="
            + clientId
            + "&response_type=code"
            + "&redirect_uri="
            + URLEncoder.encode(redirectUrl, "UTF-8")
            + "&state="
            + state
            + "&aud="
            + aud
            + "&scope="
            + URLEncoder.encode(scopes.stream().collect(Collectors.joining(" ")), "UTF-8");
      }
    }

    @Value
    @Builder
    public static class UserCredentials {
      @NonNull String id;
      @NonNull String password;
    }
  }

  @Value
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @AllArgsConstructor
  public static class TokenExchange {

    @JsonProperty("error")
    String error;

    @JsonProperty("error_description")
    String errorDescription;

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("token_type")
    String tokenType;

    @JsonProperty("expires_at")
    long expiresAt;

    @JsonProperty("scope")
    String scope;

    @JsonProperty("id_token")
    String idToken;

    @JsonProperty("patient")
    String patient;

    @JsonProperty("state")
    String state;

    boolean isError() {
      return isNotBlank(error) || isNotBlank(errorDescription);
    }
  }
}
