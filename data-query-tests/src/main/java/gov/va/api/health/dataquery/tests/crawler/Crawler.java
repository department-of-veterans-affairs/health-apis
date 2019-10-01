package gov.va.api.health.dataquery.tests.crawler;

import static java.util.stream.Collectors.joining;

import com.google.common.base.Stopwatch;
import gov.va.api.health.dataquery.tests.crawler.Result.Outcome;
import gov.va.api.health.dataquery.tests.crawler.Result.ResultBuilder;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/** The Crawler will recursively request resources from a Data Query server. I */
@SuppressWarnings("WeakerAccess")
@Builder
@Slf4j
public class Crawler {
  private final RequestQueue requestQueue;

  private final ResultCollector results;

  private final Supplier<String> authenticationToken;

  private final ExecutorService executor;

  private final Duration timeLimit;

  private static long notDoneCount(Collection<Future<?>> futures) {
    return futures.stream().filter(f -> !f.isDone()).count();
  }

  private void addCustomSeedUrls() {
    CrawlerProperties.seedQueries().forEach(requestQueue::add);
  }

  private void addLinksFromBundle(Object payload) {
    if (!(payload instanceof AbstractBundle<?>)) {
      return;
    }
    AbstractBundle<?> bundle = (AbstractBundle<?>) payload;
    Optional<BundleLink> next =
        bundle.link().stream().filter(l -> l.relation() == LinkRelation.next).findFirst();
    next.ifPresent(bundleLink -> requestQueue.add(bundleLink.url()));
    bundle.entry().stream().map(AbstractEntry::fullUrl).forEach(requestQueue::add);
  }

  private String asAdditionalInfo(ConstraintViolation<?> v) {
    return v.getPropertyPath() + ": " + v.getMessage() + ", got: " + v.getInvalidValue();
  }

  private String asAdditionalInfo(Exception e) {
    return "Exception: "
        + e.getClass().getName()
        + "\nMessage: "
        + e.getMessage()
        + "\n----\n"
        + ExceptionUtils.getStackTrace(e);
  }

  /** Crawler iterates through queue performing all queries. */
  @SuppressWarnings("WeakerAccess")
  @SneakyThrows
  public void crawl() {
    addCustomSeedUrls();
    Stopwatch watch = Stopwatch.createStarted();
    results.init();
    if (!requestQueue.hasNext()) {
      log.info("Request queue is empty, aborting");
      return;
    }
    Stack<Future<?>> futures = new Stack<>();
    ScheduledExecutorService monitor = monitorPendingRequests(futures);
    while (hasPendingRequests(futures) && !timeLimitReached(watch)) {
      if (!requestQueue.hasNext()) {
        continue;
      }
      String url = requestQueue.next();
      futures.push(
          executor.submit(
              () -> {
                ResultBuilder resultBuilder = Result.builder().timestamp(Instant.now()).query(url);
                try {
                  process(url, resultBuilder);
                } catch (Exception e) {
                  log.error("Failed to process {}", url, e);
                  resultBuilder.outcome(Outcome.REQUEST_FAILED).additionalInfo(asAdditionalInfo(e));
                }
                results.add(resultBuilder.build());
              }));
    }
    if (timeLimitReached(watch)) {
      log.info(
          "Time limit {} reached. Ignoring {} pending requests.", timeLimit, notDoneCount(futures));
      for (final Future<?> future : futures) {
        future.cancel(false);
      }
    }
    monitor.shutdownNow();
    results.done();
  }

  private boolean hasPendingRequests(Collection<Future<?>> futures) {
    if (futures.isEmpty()) {
      /*
       * If there are no futures in the list, then we have not yet processed any requests... this is
       * the first time through the while loop.
       */
      return true;
    }
    return futures.stream().anyMatch(f -> !f.isDone());
  }

  private ScheduledExecutorService monitorPendingRequests(Collection<Future<?>> futures) {
    ScheduledExecutorService monitor = Executors.newScheduledThreadPool(1);
    monitor.scheduleAtFixedRate(
        () -> log.info("{} pending requests", notDoneCount(futures)), 5, 5, TimeUnit.SECONDS);
    return monitor;
  }

  @SneakyThrows
  private void process(String url, ResultBuilder resultBuilder) {
    Class<?> type = new UrlToResourceConverter().apply(url);
    String datamart = System.getProperty("datamart");
    log.info("Requesting {} as {} (Datamart={})", url, type.getName(), datamart);
    RequestSpecification specification =
        RestAssured.given()
            .header("Authorization", "Bearer " + authenticationToken.get())
            .accept("application/fhir+json");
    if (StringUtils.isNotBlank(datamart)) {
      specification.header("Datamart", datamart);
    }
    Instant start = Instant.now();
    Response response = specification.relaxedHTTPSValidation().get(url).andReturn();
    resultBuilder
        .duration(Duration.between(start, Instant.now()))
        .httpStatus(response.getStatusCode())
        .body(response.getBody().asString());
    if (response.getStatusCode() == 200) {
      ReferenceInterceptor interceptor = new ReferenceInterceptor();
      Object payload = interceptor.mapper().readValue(response.asByteArray(), type);
      interceptor.references().forEach(requestQueue::add);
      Set<ConstraintViolation<Object>> violations =
          Validation.buildDefaultValidatorFactory().getValidator().validate(payload);
      if (violations.isEmpty()) {
        resultBuilder.outcome(Outcome.OK);
      } else {
        resultBuilder
            .outcome(Outcome.INVALID_PAYLOAD)
            .additionalInfo(violations.stream().map(this::asAdditionalInfo).collect(joining("\n")));
      }
      addLinksFromBundle(payload);
    } else {
      resultBuilder.outcome(Outcome.INVALID_STATUS);
    }
    slowDownIfApproachingRequestLimit(response);
  }

  private void slowDownIfApproachingRequestLimit(Response response) {
    String rateLimitHeader = response.getHeader("X-RateLimit-Remaining-minute");
    if (StringUtils.isBlank(rateLimitHeader)) {
      return;
    }
    int remainingRequests;
    try {
      remainingRequests = Integer.parseInt(rateLimitHeader);
    } catch (NumberFormatException e) {
      log.warn("Cannot parse rate limit header {}, assuming full steam ahead!", rateLimitHeader);
      return;
    }
    long sleepSecs = 0;
    if (remainingRequests < 30) {
      sleepSecs = 15;
    } else if (remainingRequests < 45) {
      sleepSecs = 5;
    }
    if (sleepSecs > 0) {
      log.info(
          "{} remaining requests per minute, throttling requests {} seconds",
          remainingRequests,
          sleepSecs);
      try {
        TimeUnit.SECONDS.sleep(sleepSecs);
      } catch (InterruptedException e) {
        log.warn("Got abruptly woken up by the rude neighbor!", e);
      }
    }
  }

  private boolean timeLimitReached(Stopwatch watch) {
    return timeLimit != null && watch.elapsed(TimeUnit.SECONDS) > timeLimit.getSeconds();
  }
}
