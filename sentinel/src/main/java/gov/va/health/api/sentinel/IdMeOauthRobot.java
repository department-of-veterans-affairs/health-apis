package gov.va.health.api.sentinel;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Set;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class IdMeOauthRobot {

  @Getter @NonNull private final Configuration config;

  @Getter(lazy = true)
  private final String code = authorize();

  @Getter(lazy = true)
  private final TokenExchange token = exchangeForCodeForToken();

  private String authorize() {
    ChromeOptions chromeOptions = new ChromeOptions();
    chromeOptions.setHeadless(config.headless());
    chromeOptions.addArguments("--whitelisted-ips", "--disable-extensions", "--no-sandbox");
    if (isNotBlank(config.chromeDriver())) {
      System.setProperty("webdriver.chrome.driver", config.chromeDriver());
    }
    WebDriver driver = new ChromeDriver(chromeOptions);

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
    // Continue passed authentication code send form
    log.info("Clicking through two factor authorization sham");
    driver.findElement(By.className("btn-primary")).click();
    // Continue passed entering the authentication code
    driver.findElement(By.className("btn-primary")).click();

    String url = driver.getCurrentUrl();

    log.info("Redirected {}", url);

    driver.close();
    driver.quit();

    String code =
        Arrays.stream(url.split("\\?")[1].split("&"))
            .filter(p -> p.startsWith("code="))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cannot find code in url " + url))
            .split("=")[1];

    log.info("Code: {}", code);
    return code;
  }

  private TokenExchange exchangeForCodeForToken() {
    log.info("Exchanging authorization code for token");
    TokenExchange tokenExchange =
        RestAssured.given()
            .contentType(ContentType.URLENC.withCharset("UTF-8"))
            .formParam("client_id", config.authorization().clientId())
            .formParam("client_secret", config.authorization().clientSecret())
            .formParam("grant_type", "authorization_code")
            .formParam("redirect_uri", config.authorization().redirectUrl())
            .formParam("code", code())
            .log()
            .all()
            .post(config.tokenUrl())
            .then()
            .extract()
            .as(TokenExchange.class);
    log.info("{}", tokenExchange);
    if (tokenExchange.isError()) {
      throw new IllegalStateException(
          "Failed to exchange code for token: "
              + tokenExchange.error()
              + "\n"
              + tokenExchange.error_description());
    }
    return tokenExchange;
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
      @NonNull String icn;
    }
  }

  @Value
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @AllArgsConstructor
  public static class TokenExchange {

    @JsonProperty("error")
    String error;

    @JsonProperty("error_description")
    String error_description;

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
      return isNotBlank(error) || isNotBlank(error_description);
    }
  }
}
