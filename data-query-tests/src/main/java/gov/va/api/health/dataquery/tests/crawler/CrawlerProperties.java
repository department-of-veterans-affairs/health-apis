package gov.va.api.health.dataquery.tests.crawler;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.base.Splitter;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CrawlerProperties {

  /**
   * Get crawler ignores from a system property. Ignores are not factored into the crawlers result
   * if they fail.
   */
  public String optionCrawlerIgnores() {
    String ignores = System.getProperty("crawler.ignores");
    if (isBlank(ignores)) {
      log.info(
          "No requests ignored. (Override with -Dcrawler.ignores=<ignores> "
              + "-- Place ignores in a comma separated list)");
    } else {
      log.info(
          "Ignoring the following requests: {} "
              + "(Override with -Dcrawler.ignores=<ignores> "
              + "-- Place ignores in a comma separated list)",
          ignores);
    }
    return ignores;
  }

  /** Read crawler thread limit from the property crawler.threads */
  public int threads() {
    String property = "crawler.threads";
    String maybeInteger = System.getProperty(property);
    if (isNotBlank(maybeInteger)) {
      try {
        final int threadLimit = Integer.parseInt(maybeInteger);
        log.info(
            "Crawling with thread limit {} (Override with -D{}=<Number>)", threadLimit, property);
        return threadLimit;
      } catch (NumberFormatException e) {
        log.warn("Bad thread limit {}, proceeding with 10.", maybeInteger);
      }
    }
    log.info("Crawling with default thread limit of 10 (Override with -D{}=<Number>)", property);
    return 10;
  }

  /** Read crawler time limit from the property crawler.timelimit/ */
  public Duration timeLimit() {
    String property = "crawler.timelimit";
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
  public String allowQueryUrlPattern() {
    String pattern = System.getProperty("crawler.allow-query-url-pattern");
    if (isBlank(pattern)) {
      log.info("URL filtering disabled (Override with -Dcrawler.allow-query-url-pattern=<regex>)");
      pattern = ".*";
    } else {
      log.info(
          "Only allowing URLs matching {}"
              + " (Override with --Dcrawler.allow-query-url-pattern=<regex>)",
          pattern);
    }
    return pattern;
  }

  /** Read url replacement from system property. */
  public List<String> seedQueries() {
    String replace = System.getProperty("crawler.seed");
    if (isBlank(replace)) {
      log.info("Additional seed URLs disabled (Override with -Dcrawler.seed=<url>)");
      return List.of();
    }
    log.info("Additional seed URLs {} (Override with -Dcrawler.url.replace=<url>)", replace);
    return Splitter.on(",").splitToList(replace);
  }

  /** Read url replacement from system property. */
  public String urlReplace() {
    String replace = System.getProperty("crawler.url.replace");
    if (isBlank(replace)) {
      log.info("URL replacement disabled (Override with -Dcrawler.url.replace=<url>)");
    } else {
      log.info("URL replacement {} (Override with -Dcrawler.url.replace=<url>)", replace);
    }
    return replace;
  }

  /** Read the metadata URL from system property. */
  public String baseUrlOrElse(@NonNull String defaultUrl) {
    String url = System.getProperty("crawler.base-url");
    if (isBlank(url)) {
      log.info(
          "Base URL not specified, assuming {} (Override with -Dcrawler.base-url=<url>)",
          defaultUrl);
      return defaultUrl;
    }
    log.info("Base URL {} (Override with -Dcrawler.base-url=<url>)", url);
    return url;
  }
}
