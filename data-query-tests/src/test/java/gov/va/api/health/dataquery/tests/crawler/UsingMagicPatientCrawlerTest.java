package gov.va.api.health.dataquery.tests.crawler;

import static gov.va.api.health.sentinel.SentinelProperties.magicAccessToken;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.DataQueryProperties;
import gov.va.api.health.dataquery.tests.Swiggity;
import gov.va.api.health.dataquery.tests.SystemDefinition;
import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.sentinel.SentinelProperties;
import gov.va.api.health.sentinel.categories.Manual;
import java.io.File;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class UsingMagicPatientCrawlerTest {

  @Category(Manual.class)
  @Test
  public void crawl() {
    assertThat(magicAccessToken()).isNotNull();
    log.info("Access token is specified");

    String patient = DataQueryProperties.cdwTestPatient();
    log.info("Using patient {} (Override with -Dpatient-id=<id>)", patient);
    Swiggity.swooty(patient);

    SystemDefinition env = SystemDefinitions.systemDefinition();

    ResourceDiscovery discovery =
        ResourceDiscovery.builder()
            .patientId(patient)
            .url(env.dataQuery().urlWithApiPath())
            .build();

    IgnoreFilterResultCollector results =
        IgnoreFilterResultCollector.wrap(
            SummarizingResultCollector.wrap(
                new FileResultsCollector(new File("target/patient-crawl-" + patient))));
    results.useFilter(CrawlerProperties.optionCrawlerIgnores());

    UrlReplacementRequestQueue rq =
        UrlReplacementRequestQueue.builder()
            .replaceUrl(CrawlerProperties.urlReplace())
            .withUrl(env.dataQuery().urlWithApiPath())
            .requestQueue(new ConcurrentResourceBalancingRequestQueue())
            .build();

    discovery.queries().forEach(rq::add);
    Crawler crawler =
        Crawler.builder()
            .executor(
                Executors.newFixedThreadPool(
                    SentinelProperties.threadCount("sentinel.crawler.threads", 8)))
            .requestQueue(rq)
            .results(results)
            .authenticationToken(() -> magicAccessToken())
            .forceJargonaut(Boolean.parseBoolean(System.getProperty("jargonaut", "true")))
            .timeLimit(CrawlerProperties.timeLimit())
            .build();
    crawler.crawl();
    log.info("Results for patient : {}", patient);
    assertThat(results.failures()).withFailMessage("%d Failures", results.failures()).isEqualTo(0);
  }
}
