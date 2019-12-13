package gov.va.api.health.dataquery.tests;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.TestClient;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

/** This support class can be used to test standard resource queries, such as reads and searches. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractResourceVerifier {
  private static final Set<Class<?>> VERIFIED_PAGE_BOUNDS_CLASSES =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  static {
    log.info(
        "Datamart failures enabled: {} "
            + "(Override using -Ddatamart.failures.enabled=<true|false> "
            + "or environment variable DATAMART_FAILURES_ENABLED=<true|false>)",
        datamartFailuresEnabled());
  }

  @Getter
  private final TestIds ids = IdRegistrar.of(SystemDefinitions.systemDefinition()).registeredIds();

  /**
   * Datamart is not quite stable enough to prohibit builds from passing. Since this feature is
   * toggled off, we'll allow Datamart failures anywhere but locally.
   */
  private static boolean datamartFailuresEnabled() {
    if (Environment.get() == Environment.LOCAL) {
      return true;
    }
    if (isTrue(toBoolean(System.getProperty("datamart.failures.enabled")))) {
      return true;
    }
    if (isTrue(toBoolean(System.getenv("DATAMART_FAILURES_ENABLED")))) {
      return true;
    }
    return false;
  }

  protected abstract String apiPath();

  /**
   * If the response is a bundle, then the query is a search. We want to verify paging parameters
   * restrict page >= 1, _count >=1, and _count <= 20
   */
  @SneakyThrows
  protected final <T> void assertPagingParameterBounds(TestCase<T> tc) {
    if (!bundleClass().isAssignableFrom(tc.response())) {
      return;
    }
    if (VERIFIED_PAGE_BOUNDS_CLASSES.contains(tc.response())) {
      log.info("Verify {} page bounds, skipping repeat {}.", tc.label(), tc.response.getName());
      return;
    }
    log.info("Verify {} page bounds", tc.label());
    VERIFIED_PAGE_BOUNDS_CLASSES.add(tc.response());
    dataQuery()
        .get(tc.path() + "&page=0", tc.parameters())
        .expect(400)
        .expectValid(operationOutcomeClass());
    dataQuery()
        .get(tc.path() + "&_count=-1", tc.parameters())
        .expect(400)
        .expectValid(operationOutcomeClass());
    dataQuery()
        .get(tc.path() + "&_count=0", tc.parameters())
        .expect(200)
        .expectValid(tc.response());
    T bundle =
        dataQuery()
            .get(tc.path() + "&_count=21", tc.parameters())
            .expect(200)
            .expectValid(tc.response());
    Method bundleEntryMethod = bundleClass().getMethod("entry");
    ReflectionUtils.makeAccessible(bundleEntryMethod);
    Collection<?> entries = (Collection<?>) bundleEntryMethod.invoke(bundle);
    assertThat(entries.size()).isLessThan(21);
  }

  private <T> T assertRequest(TestCase<T> tc) {
    if (isDatamartAndCdwResource(tc)) {
      log.info(
          "Verify Datamart {} is {} ({})", tc.label(), tc.response().getSimpleName(), tc.status());
      try {
        Map<String, String> datamartHeader = ImmutableMap.of("Datamart", "true");
        dataQuery()
            .get(datamartHeader, tc.path(), tc.parameters())
            .expect(tc.status())
            .expectValid(tc.response());
      } catch (AssertionError | Exception e) {
        if (datamartFailuresEnabled()) {
          throw e;
        }
        log.error("Suppressing datamart failure: {}: {}", tc.label(), e.getMessage());
      }
    }
    log.info("Verify {} is {} ({})", tc.label(), tc.response().getSimpleName(), tc.status());
    return dataQuery()
        .get(tc.path(), tc.parameters())
        .expect(tc.status())
        .expectValid(tc.response());
  }

  protected abstract Class<?> bundleClass();

  protected abstract TestClient dataQuery();

  protected abstract Set<Class<?>> datamartAndCdwResources();

  private <T> boolean isDatamartAndCdwResource(TestCase<T> tc) {
    // If this is a bundle, we want the declaring resource type instead.
    Class<?> resource =
        bundleClass().isAssignableFrom(tc.response())
            ? tc.response().getDeclaringClass()
            : tc.response();
    return datamartAndCdwResources().contains(resource);
  }

  protected abstract Class<?> operationOutcomeClass();

  public final <T> TestCase<T> test(
      int status, Class<T> response, String path, String... parameters) {
    return TestCase.<T>builder()
        .path(apiPath() + path)
        .parameters(parameters)
        .response(response)
        .status(status)
        .build();
  }

  public final <T> T verify(TestCase<T> tc) {
    assertPagingParameterBounds(tc);
    return assertRequest(tc);
  }

  public final void verifyAll(TestCase<?>... testCases) {
    for (TestCase<?> tc : testCases) {
      try {
        verify(tc);
      } catch (Exception | AssertionError e) {
        log.error(
            "Failure: {} with parameters {}: {}",
            tc.path(),
            Arrays.toString(tc.parameters()),
            e.getMessage());
        throw e;
      }
    }
  }

  @Value
  @Builder
  public static final class TestCase<T> {
    int status;

    Class<T> response;

    String path;

    String[] parameters;

    String label() {
      return path + " with " + Arrays.toString(parameters);
    }
  }
}
