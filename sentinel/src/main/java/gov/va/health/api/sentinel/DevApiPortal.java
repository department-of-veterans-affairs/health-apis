package gov.va.health.api.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Slf4j
public class DevApiPortal {
  private ChromeDriver driver;

  public WebElement getChild(WebElement element, By by) {
    return element.findElement(by);
  }

  public WebElement getElement(By by) {
    return driver.findElement(by);
  }

  /** Set up the chrome driver with the proper properties. */
  public void initializeDriver(String url, int timeOutInSeconds) {
    Config config = new Config(new File("config/lab.properties"));
    ChromeOptions chromeOptions = new ChromeOptions();
    chromeOptions.addArguments("--whitelisted-ips", "--disable-extensions", "--no-sandbox");
    chromeOptions.setHeadless(config.headless());
    if (StringUtils.isNotBlank(config.driver())) {
      System.setProperty("webdriver.chrome.driver", config.driver());
    }
    driver = new ChromeDriver(chromeOptions);
    driver.manage().timeouts().implicitlyWait(timeOutInSeconds, TimeUnit.SECONDS);
    driver.get(url);
  }

  public void quit() {
    driver.quit();
  }

  public static class Config {
    private Properties properties;

    @SneakyThrows
    Config(File file) {
      if (file.exists()) {
        log.info("Loading lab properties from: {}", file);
        properties = new Properties(System.getProperties());
        try (FileInputStream in = new FileInputStream(file)) {
          properties.load(in);
        }
      } else {
        log.info("Lab properties not found: {}, using System properties", file);
        properties = System.getProperties();
      }
    }

    String driver() {
      return valueOf("webdriver.chrome.driver");
    }

    boolean headless() {
      return BooleanUtils.toBoolean(valueOf("webdriver.chrome.headless"));
    }

    private String valueOf(String name) {
      String value = properties.getProperty(name, "");
      assertThat(value).withFailMessage("System property %s must be specified.", name).isNotBlank();
      return value;
    }
  }
}
