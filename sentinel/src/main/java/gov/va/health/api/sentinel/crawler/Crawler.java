package gov.va.health.api.sentinel.crawler;

import static java.util.stream.Collectors.joining;

import com.google.common.base.Stopwatch;
import gov.va.api.health.argonaut.api.bundle.AbstractBundle;
import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.bundle.BundleLink.LinkRelation;
import gov.va.health.api.sentinel.crawler.Result.Outcome;
import gov.va.health.api.sentinel.crawler.Result.ResultBuilder;
import io.restassured.RestAssured;
import io.restassured.response.Response;
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

/** The Crawler will recursive request resources from an Argonaut server. I */
@Builder
@Slf4j
public class Crawler {
  private final RequestQueue requestQueue;
  private final ResultCollector results;
  private final Supplier<String> authenticationToken;
  private final ExecutorService executor;
  private final boolean forceJargonaut;

  private static long notDoneCount(Collection<Future<?>> futures) {
    return futures.stream().filter(f -> !f.isDone()).count();
  }

  private void addLinksFromBundle(Object payload) {
    if (!(payload instanceof AbstractBundle<?>)) {
      return;
    }
    AbstractBundle<?> bundle = (AbstractBundle<?>) payload;
    Optional<BundleLink> next =
        bundle.link().stream().filter(l -> l.relation() == LinkRelation.next).findFirst();
    if (next.isPresent()) {
      requestQueue.add(next.get().url());
    }
    bundle.entry().stream().map(entry -> entry.fullUrl()).forEach(requestQueue::add);
  }

  private String asAdditionalInfo(ConstraintViolation<?> v) {
    return v.getPropertyPath() + ": " + v.getMessage() + ", got: " + v.getInvalidValue();
  }

  private String asAdditionalInfo(Exception e) {
    StringBuilder info = new StringBuilder();
    info.append("Exception: ")
        .append(e.getClass().getName())
        .append("\nMessage: ")
        .append(e.getMessage())
        .append("\n----\n")
        .append(ExceptionUtils.getStackTrace(e));
    return info.toString();
  }

  /** Crawler iterates through queue performing all queries. */
  @SneakyThrows
  public void crawl() {
    Stopwatch watch = Stopwatch.createStarted();
    results.init();
    Stack<Future<?>> futures = new Stack<>();
    ScheduledExecutorService monitor = monitorPendingRequests(futures);
    // PETERTODO configurable time limit
    Duration timeLimit = Duration.parse("PT5S");
    // PT1H10M10S

    while (hasPendingRequests(futures)) {
      if (watch.elapsed(TimeUnit.SECONDS) > timeLimit.getSeconds()) {
        log.info(
            "Time limit {} reached. Finishing {} pending requests.",
            timeLimit,
            notDoneCount(futures));
        for (final Future<?> future : futures) {
          future.get(1, TimeUnit.HOURS);
        }
        break;
      }

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
        () -> {
          log.info("{} pending requests", notDoneCount(futures));
        },
        5,
        5,
        TimeUnit.SECONDS);
    return monitor;
  }

  @SneakyThrows
  private void process(String url, ResultBuilder resultBuilder) {
    Class<?> type = new UrlToResourceConverter().apply(url);
    log.info("Requesting {} as {}", url, type.getName());
    Response response =
        RestAssured.given()
            .header("Authorization", "Bearer " + authenticationToken.get())
            .contentType("application/fhir+json")
            .header("jargonaut", forceJargonaut)
            .relaxedHTTPSValidation()
            .get(url)
            .andReturn();
    resultBuilder.httpStatus(response.getStatusCode()).body(response.getBody().asString());

    if (response.getStatusCode() == 200) {
      ReferenceInterceptor interceptor = new ReferenceInterceptor();
      Object payload = interceptor.mapper().readValue(response.asByteArray(), type);
      interceptor.references().forEach(u -> requestQueue.add(u));

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
}
