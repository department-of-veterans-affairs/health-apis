package gov.va.health.api.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.health.api.sentinel.categories.Manual;
import gov.va.health.api.sentinel.crawler.ConcurrentRequestQueue;
import gov.va.health.api.sentinel.crawler.Crawler;
import gov.va.health.api.sentinel.crawler.FileResultsCollector;
import gov.va.health.api.sentinel.crawler.RequestQueue;
import gov.va.health.api.sentinel.crawler.ResourceDiscovery;
import gov.va.health.api.sentinel.crawler.SummarizingResultCollector;
import java.io.File;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class LabCrawlerTest {

  private LabRobots robots = LabRobots.fromSystemProperties();

  private void crawl(IdMeOauthRobot robot) {
    Swiggity.swooty(robot.config().user().id());
    assertThat(robot.token().accessToken()).isNotBlank();
    ResourceDiscovery discovery =
        ResourceDiscovery.builder()
            .patientId(robot.token().patient())
            .url("https://dev-api.va.gov/services/argonaut/v0")
            .build();
    SummarizingResultCollector results =
        SummarizingResultCollector.wrap(
            new FileResultsCollector(new File("target/lab-crawl-" + robot.token().patient())));
    RequestQueue q = new ConcurrentRequestQueue();
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
    assertThat(results.failures()).withFailMessage("%d Failures", results.failures()).isEqualTo(0);
  }

  @Category(Manual.class)
  @Test
  public void crawlUser1() {
    crawl(robots.user1());
  }

  @Category(Manual.class)
  @Test
  public void crawlUser2() {
    crawl(robots.user2());
  }

  @Category(Manual.class)
  @Test
  public void crawlUser3() {
    crawl(robots.user3());
  }

  @Category(Manual.class)
  @Test
  public void crawlUser4() {
    crawl(robots.user4());
  }

  @Category(Manual.class)
  @Test
  public void crawlUser5() {
    crawl(robots.user5());
  }
}
