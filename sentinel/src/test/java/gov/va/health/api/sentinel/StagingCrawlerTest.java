package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.SystemDefinitions.magicAccessToken;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.health.api.sentinel.categories.Manual;
import gov.va.health.api.sentinel.crawler.ConcurrentResourceBalancingRequestQueue;
import gov.va.health.api.sentinel.crawler.Crawler;
import gov.va.health.api.sentinel.crawler.FileResultsCollector;
import gov.va.health.api.sentinel.crawler.ResourceDiscovery;
import gov.va.health.api.sentinel.crawler.SummarizingResultCollector;
import gov.va.health.api.sentinel.crawler.UrlReplacementRequestQueue;
import java.io.File;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class StagingCrawlerTest {
  @Category(Manual.class)
  @Test
  public void crawlStaging() {
    Supplier<String> accessTokenValue = () -> magicAccessToken().get().get();
    assertThat(accessTokenValue.get()).isNotNull();
    log.info("Access token is specified");

    String patient = System.getProperty("patient-id", "1011537977V693883");
    log.info("Using patient {} (Override with -Dpatient-id=<id>)", patient);
    Swiggity.swooty(patient);

    ResourceDiscovery discovery =
        ResourceDiscovery.builder()
            .patientId(patient)
            .url("https://staging-argonaut.lighthouse.va.gov/api/")
            .build();
    SummarizingResultCollector results =
        SummarizingResultCollector.wrap(
            new FileResultsCollector(new File("target/staging-crawl-" + patient)));

    UrlReplacementRequestQueue rq =
        UrlReplacementRequestQueue.builder()
            .replaceUrl("https://dev-api.va.gov/services/argonaut/v0/")
            .withUrl("https://staging-argonaut.lighthouse.va.gov/api/")
            .requestQueue(new ConcurrentResourceBalancingRequestQueue())
            .build();

    discovery.queries().forEach(rq::add);
    Crawler crawler =
        Crawler.builder()
            .executor(Executors.newFixedThreadPool(4))
            .requestQueue(rq)
            .results(results)
            .authenticationToken(accessTokenValue)
            .forceJargonaut(true)
            .timeLimit(timeLimit())
            .build();
    crawler.crawl();
    log.info("Results for patient : {} \n{}", patient, results.message());
    assertThat(results.failures()).withFailMessage("%d Failures", results.failures()).isEqualTo(0);
  }

  private Duration timeLimit() {
    String maybeDuration = System.getProperty("sentinel.crawler.timelimit");
    if (isNotBlank(maybeDuration)) {
      try {
        final Duration timeLimit = Duration.parse(maybeDuration);
        log.info(
            "Crawling with time limit {} (Override with -Dsentinel.crawler.timelimit=<PnYnMnDTnHnMnS>)",
            timeLimit);
        return timeLimit;
      } catch (DateTimeParseException e) {
        log.warn("Bad time limit {}, proceeding with no limit.", maybeDuration);
      }
    }
    log.info(
        "Crawling with no time limit (Override with -Dsentinel.crawler.timelimit=<PnYnMnDTnHnMnS>)");
    return null;
  }
}
