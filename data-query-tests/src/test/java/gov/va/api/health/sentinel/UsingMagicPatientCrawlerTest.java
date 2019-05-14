package gov.va.api.health.sentinel;

import static gov.va.api.health.sentinel.SentinelProperties.magicAccessToken;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.categories.Manual;
import gov.va.api.health.sentinel.crawler.ConcurrentResourceBalancingRequestQueue;
import gov.va.api.health.sentinel.crawler.Crawler;
import gov.va.api.health.sentinel.crawler.CrawlerProperties;
import gov.va.api.health.sentinel.crawler.FileResultsCollector;
import gov.va.api.health.sentinel.crawler.ResourceDiscovery;
import gov.va.api.health.sentinel.crawler.SummarizingResultCollector;
import gov.va.api.health.sentinel.crawler.SummarizingWithIgnoreListResultCollector;
import gov.va.api.health.sentinel.crawler.UrlReplacementRequestQueue;
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

    SummarizingWithIgnoreListResultCollector results =
        SummarizingWithIgnoreListResultCollector.wrap(
            SummarizingResultCollector.wrap(
                new FileResultsCollector(new File("target/patient-crawl-" + patient))));
    results.useFilter(
        // "/argonaut/v0/Procedure/f8f848a3-35ae-5aec-93ad-e4d8731a1f11,Procedure/f8c70a3a-809f-5987-816c-78d7c966a194");
        "Procedure/f8f848a3-35ae-5aec-93ad-e4d8731a1f11");

    UrlReplacementRequestQueue rq =
        UrlReplacementRequestQueue.builder()
            .replaceUrl(SentinelProperties.urlReplace("argonaut"))
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
