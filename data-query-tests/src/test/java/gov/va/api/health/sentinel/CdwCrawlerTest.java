package gov.va.api.health.sentinel;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.categories.Manual;
import gov.va.api.health.sentinel.crawler.ConcurrentResourceBalancingRequestQueue;
import gov.va.api.health.sentinel.crawler.Crawler;
import gov.va.api.health.sentinel.crawler.FileResultsCollector;
import gov.va.api.health.sentinel.crawler.RequestQueue;
import gov.va.api.health.sentinel.crawler.ResourceDiscovery;
import gov.va.api.health.sentinel.crawler.SummarizingResultCollector;
import gov.va.api.health.sentinel.crawler.UrlReplacementRequestQueue;
import java.io.File;
import java.time.Duration;
import java.time.format.DateTimeParseException;
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
    String patient = System.getProperty("patient-id", "1011537977V693883");
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
            .executor(Executors.newFixedThreadPool(threadCount()))
            .requestQueue(q)
            .results(results)
            .authenticationToken(accessTokenValue)
            .forceJargonaut(true)
            .timeLimit(timeLimit())
            .build();
    crawler.crawl();
    log.info("Results for patient : {} \n{}", patient, results.message());
    assertThat(results.failures()).withFailMessage("%d Failures", results.failures()).isEqualTo(0);
  }

  private RequestQueue requestQueue(SystemDefinition env) {
    String replaceUrl = System.getProperty("sentinel.argonaut.url.replace");
    if (isBlank(replaceUrl)) {
      log.info("Link replacement disabled (Override with -Dsentinel.argonaut.url.replace=<url>)");
      return new ConcurrentResourceBalancingRequestQueue();
    }
    log.info(
        "Link replacement {} (Override with -Dsentinel.argonaut.url.replace=<url>)", replaceUrl);
    return UrlReplacementRequestQueue.builder()
        .replaceUrl(replaceUrl)
        .withUrl(env.argonaut().urlWithApiPath())
        .requestQueue(new ConcurrentResourceBalancingRequestQueue())
        .build();
  }

  private int threadCount() {
    int threads = 8;
    String maybeNumber = System.getProperty("sentinel.crawler.threads");
    if (isNotBlank(maybeNumber)) {
      try {
        threads = Integer.parseInt(maybeNumber);
      } catch (NumberFormatException e) {
        log.warn("Bad thread count {}, assuming {}", maybeNumber, threads);
      }
    }
    log.info(
        "Crawling with {} threads (Override with -Dsentinel.crawler.threads=<number>)", threads);
    return threads;
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
