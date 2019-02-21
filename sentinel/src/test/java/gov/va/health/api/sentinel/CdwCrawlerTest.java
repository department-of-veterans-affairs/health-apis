package gov.va.health.api.sentinel;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.assertj.core.api.Assertions.assertThat;

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
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class CdwCrawlerTest {

  @Category(Manual.class)
  @Test
  public void crawl() {
    SystemDefinition env = Sentinel.get().system();
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
            .build();
    crawler.crawl();
    log.info("Results for patient : {} \n{}", patient, results.message());
    assertThat(results.failures()).withFailMessage("%d Failures", results.failures()).isEqualTo(0);
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
}
