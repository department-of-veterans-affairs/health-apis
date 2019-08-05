package gov.va.api.health.dataquery.tests.crawler;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.Swiggity;
import gov.va.api.health.dataquery.tests.SystemDefinition;
import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.sentinel.LabBot;
import gov.va.api.health.sentinel.categories.Manual;
import gov.va.api.health.sentinel.selenium.IdMeOauthRobot;
import gov.va.api.health.sentinel.selenium.IdMeOauthRobot.Configuration.UserCredentials;
import gov.va.api.health.sentinel.selenium.IdMeOauthRobot.OAuthCredentialsMode;
import java.io.File;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class UsingOAuthCrawlerTest {
  private final LabBot bot = LabBot.builder().configFile("config/lab.properties").build();

  private int crawl(String patient, OAuthCredentialsMode credentialsMode) {
    SystemDefinition env = SystemDefinitions.systemDefinition();
    UserCredentials user =
        UserCredentials.builder()
            .id(patient)
            .password(System.getProperty("lab.user-password"))
            .build();
    IdMeOauthRobot robot = bot.makeLabBot(user, env.dataQuery().urlWithApiPath(), credentialsMode);
    Swiggity.swooty(patient);
    assertThat(robot.token().accessToken()).isNotBlank();

    ResourceDiscovery discovery =
        ResourceDiscovery.builder()
            .patientId(robot.token().patient())
            .url(env.dataQuery().urlWithApiPath())
            .build();
    SummarizingResultCollector results =
        SummarizingResultCollector.wrap(
            new FileResultsCollector(new File("target/lab-crawl-" + robot.token().patient())));
    RequestQueue q = requestQueue(env);
    discovery.queries().forEach(q::add);
    Crawler crawler =
        Crawler.builder()
            .executor(Executors.newFixedThreadPool(10))
            .requestQueue(q)
            .results(results)
            .authenticationToken(() -> robot.token().accessToken())
            .forceJargonaut(true)
            .timeLimit(CrawlerProperties.timeLimit())
            .build();
    crawler.crawl();
    log.info("Results for {} ({})", robot.config().user().id(), robot.token().patient());
    return results.failures();
  }

  @Category(Manual.class)
  @Test
  public void crawlPatients() {
    int counter = 0;
    int failureCount = 0;
    String[] patients = System.getProperty("patient-id", "vasdvp+IDME_01@gmail.com").split(",");
    for (String patient : patients) {
      final OAuthCredentialsMode credentialsMode =
          counter % 2 == 0 ? OAuthCredentialsMode.HEADER : OAuthCredentialsMode.REQUEST_BODY;
      failureCount += crawl(patient.trim(), credentialsMode);
      counter++;
    }
    assertThat(failureCount).withFailMessage("%d Failures", failureCount).isEqualTo(0);
  }

  private RequestQueue requestQueue(SystemDefinition env) {
    if (isBlank(CrawlerProperties.urlReplace())) {
      return new ConcurrentResourceBalancingRequestQueue();
    }
    return UrlReplacementRequestQueue.builder()
        .replaceUrl(CrawlerProperties.urlReplace())
        .withUrl(env.dataQuery().urlWithApiPath())
        .requestQueue(new ConcurrentResourceBalancingRequestQueue())
        .build();
  }
}
