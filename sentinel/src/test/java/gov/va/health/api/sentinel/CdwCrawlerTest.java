package gov.va.health.api.sentinel;

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
import java.io.File;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
@Category({NotInLocal.class, NotInLab.class, NotInProd.class})
public class CdwCrawlerTest {
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

  private void crawl(SystemDefinition env) {
    String patient = System.getProperty("patient-id", "1011537977V693883");
    log.info("Using patient {} (Override with -Dpatient-id=<id>)", patient);

    log.info(
        "\n\n                     SWIGGITY SWOOTY!\n"
            + SAPIDER
            + "\n          doot! doot! "
            + "Using patient {} (Override with -Dpatient-id=<value>)\n",
        patient);

    Supplier<String> accessTokenValue = () -> env.argonaut().accessToken().get().get();
    assertThat(accessTokenValue).isNotNull();
    log.info("Access token is specified");

    ResourceDiscovery discovery =
        ResourceDiscovery.builder().patientId(patient).url(env.argonaut().url() + "/api").build();
    SummarizingResultCollector results =
        SummarizingResultCollector.wrap(
            new FileResultsCollector(new File("target/cdw-crawl-" + patient)));
    RequestQueue q = new ConcurrentRequestQueue();
    discovery.queries().forEach(q::add);
    Crawler crawler =
        Crawler.builder()
            .executor(Executors.newFixedThreadPool(4))
            .requestQueue(q)
            .results(results)
            .authenticationToken(accessTokenValue)
            .forceJargonaut(true)
            .build();
    crawler.crawl();
    log.info("Results for patient : {} \n{}", patient, results.message());
    assertThat(results.failures()).withFailMessage("%d Failures", results.failures()).isEqualTo(0);
  }

  @Test
  public void crawlQa() {
    crawl(SystemDefinitions.get().qa());
  }

  @Test
  public void crawlProd() {
    crawl(SystemDefinitions.get().prod());
  }
}
