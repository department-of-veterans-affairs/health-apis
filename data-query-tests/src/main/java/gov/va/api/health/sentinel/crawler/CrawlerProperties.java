package gov.va.api.health.sentinel.crawler;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class CrawlerProperties {
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
}
