package gov.va.api.health.dataquery.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gov.va.api.health.argonaut.api.resources.DiagnosticReport;
import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.TestClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/** This support class can be used to test standard resource queries, such as reads and searches. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceVerifier {

  private static final ResourceVerifier INSTANCE = new ResourceVerifier();

  private static final String API_PATH = SystemDefinitions.systemDefinition().dataQuery().apiPath();

  @Getter private final TestClient dataQuery = TestClients.dataQuery();

  @Getter
  private final TestIds ids = IdRegistrar.of(SystemDefinitions.systemDefinition()).registeredIds();

  private final Set<Class<?>> verifiedPageBoundsClasses =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  private ImmutableList<Class<?>> DATAMART_RESOURCES =
      ImmutableList.of(Patient.class, DiagnosticReport.class);

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
    dataQuery()
        .get(tc.path() + "&page=0", tc.parameters())
        .expect(400)
        .expectValid(OperationOutcome.class);
    dataQuery()
        .get(tc.path() + "&_count=-1", tc.parameters())
        .expect(400)
        .expectValid(OperationOutcome.class);
    dataQuery()
        .get(tc.path() + "&_count=0", tc.parameters())
        .expect(200)
        .expectValid(tc.response());
    AbstractBundle bundle =
        (AbstractBundle<?>)
            dataQuery()
                .get(tc.path() + "&_count=21", tc.parameters())
                .expect(200)
                .expectValid(tc.response());
    assertThat(bundle.entry().size()).isLessThan(21);
  }

  private <T> T assertRequest(TestCase<T> tc) {
    if (isDatamart(tc)) {
      log.info(
          "Verify Datamart {} is {} ({})", tc.label(), tc.response().getSimpleName(), tc.status());
      dataQuery()
          .get(datamartHeader(), tc.path(), tc.parameters())
          .expect(tc.status())
          .expectValid(tc.response());
    }
    log.info("Verify {} is {} ({})", tc.label(), tc.response().getSimpleName(), tc.status());
    return dataQuery()
        .get(tc.path(), tc.parameters())
        .expect(tc.status())
        .expectValid(tc.response());
  }

  private ImmutableMap<String, String> datamartHeader() {
    return ImmutableMap.of("Datamart", "true");
  }

  private <T> boolean isDatamart(TestCase<T> tc) {
    /*
     * If this is a bundle, we want the declaring resource type instead.
     */
    Class<?> resource =
        AbstractBundle.class.isAssignableFrom(tc.response())
            ? tc.response().getDeclaringClass()
            : tc.response();
    return DATAMART_RESOURCES.contains(resource);
  }

  public <T> T verify(TestCase<T> tc) {
    assertPagingParameterBounds(tc);
    return assertRequest(tc);
  }

  public void verifyAll(TestCase<?>... testCases) {
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
