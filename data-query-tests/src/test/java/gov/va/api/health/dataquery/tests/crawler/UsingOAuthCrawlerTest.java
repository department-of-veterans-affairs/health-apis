package gov.va.api.health.dataquery.tests.crawler;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.DataQueryScopes;
import gov.va.api.health.dataquery.tests.SystemDefinition;
import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.sentinel.LabBot;
import gov.va.api.health.sentinel.LabBot.LabBotUserResult;
import gov.va.api.health.sentinel.categories.Manual;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class UsingOAuthCrawlerTest {

  private RequestQueue baseRequestQueue(SystemDefinition env) {
    if (isBlank(CrawlerProperties.urlReplace())) {
      return new ConcurrentResourceBalancingRequestQueue();
    }
    return UrlReplacementRequestQueue.builder()
        .replaceUrl(CrawlerProperties.urlReplace())
        .withUrl(env.dataQuery().urlWithApiPath())
        .requestQueue(new ConcurrentResourceBalancingRequestQueue())
        .build();
  }

  private int crawl(LabBot robot) {
    SystemDefinition env = SystemDefinitions.systemDefinition();

    LabBotUserResult userResult = robot.tokens().get(0);

    Swiggity.swooty(userResult.user().id());

    ResourceDiscovery discovery =
        ResourceDiscovery.builder()
            .patientId(userResult.tokenExchange().patient())
            .url(env.dataQuery().urlWithApiPath())
            .build();
    SummarizingResultCollector results =
        SummarizingResultCollector.wrap(
            new FileResultsCollector(
                new File("target/lab-crawl-" + userResult.tokenExchange().patient())));
    RequestQueue q = filtered(baseRequestQueue(env));
    discovery.queries().forEach(q::add);
    Crawler crawler =
        Crawler.builder()
            .executor(Executors.newFixedThreadPool(CrawlerProperties.threads()))
            .requestQueue(q)
            .results(results)
            .authenticationToken(() -> userResult.tokenExchange().accessToken())
            .timeLimit(CrawlerProperties.timeLimit())
            .build();
    crawler.crawl();
    log.info("Results for {} ({})", userResult.user().id(), userResult.tokenExchange().patient());
    return results.failures();
  }

  @Category(Manual.class)
  @Test
  public void crawlPatients() {
    int failureCount = 0;
    String[] patients = System.getProperty("patient-id", "vasdvp+IDME_01@gmail.com").split(",");
    for (String patient : patients) {
      LabBot bot =
          LabBot.builder()
              .userIds(List.of(patient))
              .configFile("config/lab.properties")
              .scopes(DataQueryScopes.labResources())
              .build();
      failureCount += crawl(bot);
    }
    assertThat(failureCount).withFailMessage("%d Failures", failureCount).isEqualTo(0);
  }

  private RequestQueue filtered(RequestQueue requestQueue) {
    return FilteringRequestQueue.builder()
        .allowQueryUrlPattern(CrawlerProperties.allowQueryUrlPattern())
        .requestQueue(requestQueue)
        .build();
  }
}
