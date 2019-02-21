package gov.va.health.api.sentinel;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.health.api.sentinel.IdMeOauthRobot.Configuration.UserCredentials;
import gov.va.health.api.sentinel.LabRobots.SmartOnFhirUrls;
import gov.va.health.api.sentinel.categories.Manual;
import gov.va.health.api.sentinel.crawler.ConcurrentRequestQueue;
import gov.va.health.api.sentinel.crawler.Crawler;
import gov.va.health.api.sentinel.crawler.FileResultsCollector;
import gov.va.health.api.sentinel.crawler.RequestQueue;
import gov.va.health.api.sentinel.crawler.ResourceDiscovery;
import gov.va.health.api.sentinel.crawler.SummarizingResultCollector;
import gov.va.health.api.sentinel.crawler.UrlReplacementRequestQueue;
import java.io.File;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class LabCrawlerTest {

  private final LabRobots robots = LabRobots.fromSystemProperties();

  private int crawl(String patient) {
    SystemDefinition env = Sentinel.get().system();
    UserCredentials user =
        UserCredentials.builder()
            .id(patient)
            .password(System.getProperty("lab.user-password"))
            .build();
    IdMeOauthRobot robot =
        robots.makeRobot(user, new SmartOnFhirUrls(env.argonaut().urlWithApiPath()));
    Swiggity.swooty(patient);
    assertThat(robot.token().accessToken()).isNotBlank();

    ResourceDiscovery discovery =
        ResourceDiscovery.builder()
            .patientId(robot.token().patient())
            .url(env.argonaut().urlWithApiPath())
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
            .build();
    crawler.crawl();
    log.info(
        "Results for {} ({})\n{}",
        robot.config().user().id(),
        robot.token().patient(),
        results.message());
    return results.failures();
  }

  @Category(Manual.class)
  @Test
  public void crawlPatients() {
    int failureCount = 0;
    String patientIds = System.getProperty("patient-id", "vasdvp+IDME_01@gmail.com");
    String[] patients = patientIds.split(",");
    for (String patient : patients) {
      failureCount += crawl(patient.trim());
    }
    assertThat(failureCount).withFailMessage("%d Failures", failureCount).isEqualTo(0);
  }

  private RequestQueue requestQueue(SystemDefinition env) {
    String replaceUrl = System.getProperty("sentinel.argonaut.url.replace");
    if (isBlank(replaceUrl)) {
      log.info("Link replacement disabled (Override with -Dsentinel.argonaut.url.replace=<url>)");
      return new ConcurrentRequestQueue();
    }
    log.info(
        "Link replacement {} (Override with -Dsentinel.argonaut.url.replace=<url>)", replaceUrl);
    return UrlReplacementRequestQueue.builder()
        .replaceUrl(replaceUrl)
        .withUrl(env.argonaut().urlWithApiPath())
        .requestQueue(new ConcurrentRequestQueue())
        .build();
  }
}
