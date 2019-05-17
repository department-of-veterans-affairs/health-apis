package gov.va.api.health.dataquery.tests.crawler;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class CrawlerProperties {

  /**
   * Get crawler ignores from a system property. Ignores are not factored into the crawlers result
   * if they fail.
   */
  public static String optionCrawlerIgnores() {
    String ignores = System.getProperty("sentinel.crawler.ignores");
    if (isBlank(ignores)) {
      log.info(
          "No requests ignored. (Override with -Dsentinel.crawler.ignores=<ignores> "
              + "-- Place ignores in a comma separated list)");
    } else {
      log.info(
          "Ignoring the following requests: {} "
              + "(Override with -Dsentinel.crawler.ignores=<ignores> "
              + "-- Place ignores in a comma separated list)",
          ignores);
    }
    return ignores;
  }

  /** Read crawler time limit from the property sentinel.crawler.timelimit/ */
  public static Duration timeLimit() {
    String property = "sentinel.crawler.timelimit";
    String maybeDuration = System.getProperty(property);
    if (isNotBlank(maybeDuration)) {
      try {
        final Duration timeLimit = Duration.parse(maybeDuration);
        log.info(
            "Crawling with time limit {} (Override with -D{}=<PnYnMnDTnHnMnS>)",
            timeLimit,
            property);
        return timeLimit;
      } catch (DateTimeParseException e) {
        log.warn("Bad time limit {}, proceeding with no limit.", maybeDuration);
      }
    }
    log.info("Crawling with no time limit (Override with -D{}=<PnYnMnDTnHnMnS>)", property);
    return null;
  }

  /** Read url replacement from system property. */
  public String urlReplace() {
    String replace = System.getProperty("sentinel.crawler.url.replace");
    if (isBlank(replace)) {
      log.info("URL replacement disabled (Override with -Dsentinel.crawler.url.replace=<url>)");
    } else {
      log.info("URL replacement {} (Override with -Dsentinel.crawler.url.replace=<url>)", replace);
    }
    return replace;
  }
}
