package gov.va.api.health.sentinel;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.categories.Manual;
import gov.va.api.health.sentinel.crawler.ConcurrentResourceBalancingRequestQueue;
import gov.va.api.health.sentinel.crawler.Crawler;
import gov.va.api.health.sentinel.crawler.CrawlerProperties;
import gov.va.api.health.sentinel.crawler.FileResultsCollector;
import gov.va.api.health.sentinel.crawler.RequestQueue;
import gov.va.api.health.sentinel.crawler.ResourceDiscovery;
import gov.va.api.health.sentinel.crawler.SummarizingResultCollector;
import gov.va.api.health.sentinel.crawler.UrlReplacementRequestQueue;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class CdwCrawlerTest {
  @Category(Manual.class)
  @Test
  public void crawl() {
    SystemDefinition env = SystemDefinitions.systemDefinition();
    String patient = DataQueryProperties.cdwTestPatient();
    log.info("Using patient {} (Override with -Dpatient-id=<id>)", patient);
    Swiggity.swooty(patient);

    Supplier<String> accessTokenValue = () -> env.argonaut().accessToken().get().get();
    assertThat(accessTokenValue).isNotNull();
    log.info("Access token is specified");

    ResourceDiscovery discovery =
        ResourceDiscovery.builder().patientId(patient).url(env.argonaut().urlWithApiPath()).build();
    SummarizingResultCollector results =
        SummarizingResultCollector.wrap(
            new FileResultsCollector(new File("target/cdw-crawl-" + patient)));

    RequestQueue q = requestQueue(env);
    discovery.queries().forEach(q::add);
    Crawler crawler =
        Crawler.builder()
            .executor(
                Executors.newFixedThreadPool(
                    SentinelProperties.threadCount("sentinel.crawler.threads", 8)))
            .requestQueue(q)
            .results(results)
            .authenticationToken(accessTokenValue)
            .forceJargonaut(true)
            .timeLimit(CrawlerProperties.timeLimit())
            .build();
    crawler.crawl();
    log.info("Results for patient : {} \n{}", patient, results.message());
    assertThat(results.failures()).withFailMessage("%d Failures", results.failures()).isEqualTo(0);
  }

  private RequestQueue requestQueue(SystemDefinition env) {
    if (isBlank(SentinelProperties.urlReplace("argonaut"))) {
      return new ConcurrentResourceBalancingRequestQueue();
    }
    return UrlReplacementRequestQueue.builder()
        .replaceUrl(SentinelProperties.urlReplace("argonaut"))
        .withUrl(env.argonaut().urlWithApiPath())
        .requestQueue(new ConcurrentResourceBalancingRequestQueue())
        .build();
  }
}
