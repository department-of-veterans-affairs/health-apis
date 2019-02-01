package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.SystemDefinitions.magicAccessToken;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.health.api.sentinel.categories.NotInLab;
import gov.va.health.api.sentinel.categories.NotInLocal;
import gov.va.health.api.sentinel.categories.NotInProd;
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

@Category({NotInLocal.class, NotInLab.class, NotInProd.class})
@Slf4j
public class StagingCrawlerTest {
  private static final String SAPIDER =
      "\n"
          + "                   /\\\n"
          + "                  /  \\\n"
          + "                 |  _ \\                  _\n"
          + "                 | / \\ \\                / \\\n"
          + "                 |/   \\ \\              /   \\\n"
          + "                 /     \\ |        /\\  /     \\\n"
          + "                /|      \\| ~  ~  /  \\/       \\\n"
          + "        _______/_|_______\\(o)(o)/___/\\_____   \\\n"
          + "       /      /  |       (______)     \\    \\   \\_\n"
          + "      /      /   |                     \\    \\\n"
          + "     /      /    |                      \\    \\\n"
          + "    /      /     |                       \\    \\\n"
          + "   /     _/      |                        \\    \\\n"
          + "  /             _|                         \\    \\_\n"
          + "_/                                          \\\n"
          + "                                             \\\n"
          + "                                              \\_"
          + "\n";

  @Test
  public void crawlStaging() {

    Supplier<String> accessTokenValue = () -> magicAccessToken().get().get();
    assertThat(accessTokenValue.get()).isNotNull();
    log.info("Access token is specified");

    String patient = System.getProperty("patient-id", "1011537977V693883");
    log.info("Using patient {} (Override with -Dpatient-id=<id>)", patient);

    log.info(
        "\n\n                     SWIGGITY SWOOTY!\n"
            + SAPIDER
            + "\n          doot! doot! "
            + "Using patient {} (Override with -Dpatient-id=<value>)\n",
        patient);

    ResourceDiscovery discovery =
        ResourceDiscovery.builder()
            .patientId(patient)
            .url("https://staging-argonaut.lighthouse.va.gov/api/")
            .build();
    SummarizingResultCollector results =
        SummarizingResultCollector.wrap(
            new FileResultsCollector(new File("target/staging-crawl-" + patient)));

    RequestQueue q = new ConcurrentRequestQueue();
    UrlReplacementRequestQueue rq =
        UrlReplacementRequestQueue.builder()
            .forceUrl("https://staging-argonaut.lighthouse.va.gov/api/")
            .baseUrl("https://dev-api.va.gov/services/argonaut/v0/")
            .requestQueue(q)
            .build();

    discovery.queries().forEach(rq::add);
    Crawler crawler =
        Crawler.builder()
            .executor(Executors.newFixedThreadPool(4))
            .requestQueue(rq)
            .results(results)
            .authenticationToken(accessTokenValue)
            .forceJargonaut(true)
            .build();
    crawler.crawl();
    log.info("Results for patient : {} \n{}", patient, results.message());
    assertThat(results.failures()).withFailMessage("%d Failures", results.failures()).isEqualTo(0);
  }
}
