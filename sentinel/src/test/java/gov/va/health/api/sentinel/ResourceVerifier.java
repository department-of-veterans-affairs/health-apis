package gov.va.health.api.sentinel;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.bundle.AbstractBundle;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/** This support class can be used to test standard resource queries, such as reads and searches. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceVerifier {
  private static final ResourceVerifier INSTANCE = new ResourceVerifier();

  private static final String API_PATH = Sentinel.get().system().argonaut().apiPath();

  @Getter(lazy = true)
  private final Sentinel sentinel = Sentinel.get();

  @Getter private final TestClient argonaut = sentinel().clients().argonaut();

  @Getter private final TestIds ids = IdRegistrar.of(sentinel().system()).registeredIds();

  private final Set<Class<?>> verifiedPageBoundsClasses =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  private final ExecutorService executorService = Executors.newFixedThreadPool(threadCount());

  public static ResourceVerifier get() {
    return INSTANCE;
  }

  public static <T> TestCase<T> test(
      int status, Class<T> response, String path, String... parameters) {
    return TestCase.<T>builder()
        .path(API_PATH + path)
        .parameters(parameters)
        .response(response)
        .status(status)
        .build();
  }

  private static int threadCount() {
    int threads = Runtime.getRuntime().availableProcessors();
    String maybeNumber = System.getProperty("sentinel.threads");
    if (isNotBlank(maybeNumber)) {
      try {
        threads = Integer.parseInt(maybeNumber);
      } catch (NumberFormatException e) {
        log.warn("Bad thread count {}, assuming {}", maybeNumber, threads);
      }
    }
    log.info("Using {} threads (Override with -Dsentinel.threads=<number>)", threads);
    return threads;
  }

  /**
   * If the response is a bundle, then the query is a search. We want to verify paging parameters
   * restrict page >= 1, _count >=1, and _count <= 20
   */
  private <T> void assertPagingParameterBounds(TestCase<T> tc) {
    if (!AbstractBundle.class.isAssignableFrom(tc.response())) {
      return;
    }

    if (verifiedPageBoundsClasses.contains(tc.response())) {
      log.info("Verify {} page bounds, skipping repeat {}.", tc.label(), tc.response.getName());
      return;
    }

    log.info("Verify {} page bounds", tc.label());
    verifiedPageBoundsClasses.add(tc.response());
    argonaut()
        .get(tc.path() + "&page=0", tc.parameters())
        .expect(400)
        .expectValid(OperationOutcome.class);
    argonaut()
        .get(tc.path() + "&_count=-1", tc.parameters())
        .expect(400)
        .expectValid(OperationOutcome.class);
    argonaut().get(tc.path() + "&_count=0", tc.parameters()).expect(200).expectValid(tc.response());
    argonaut()
        .get(tc.path() + "&_count=21", tc.parameters())
        .expect(200)
        .expectValid(tc.response());
  }

  private <T> T assertRequest(TestCase<T> tc) {
    log.info("Verify {} is {} ({})", tc.label(), tc.response().getSimpleName(), tc.status());
    return argonaut()
        .get(tc.path(), tc.parameters())
        .expect(tc.status())
        .expectValid(tc.response());
  }

  public <T> T verify(TestCase<T> tc) {
    assertPagingParameterBounds(tc);
    return assertRequest(tc);
  }

  @SneakyThrows
  public void verifyAll(TestCase<?>... testCases) {
    List<Future<?>> futures = new ArrayList<>(testCases.length);
    for (TestCase<?> tc : testCases) {
      futures.add(
          executorService.submit(
              () -> {
                try {
                  return verify(tc);
                } catch (Exception | AssertionError e) {
                  log.error(
                      "Failure: {} with parameters {}: {}",
                      tc.path(),
                      Arrays.toString(tc.parameters()),
                      e.getMessage());
                  throw e;
                }
              }));
    }

    for (Future<?> future : futures) {
      assertThat(future.get(5, TimeUnit.MINUTES)).isNotNull();
    }
  }

  @Value
  @Builder
  public static class TestCase<T> {
    int status;

    Class<T> response;

    String path;

    String[] parameters;

    String label() {
      return path + " with " + Arrays.toString(parameters);
    }
  }
}
