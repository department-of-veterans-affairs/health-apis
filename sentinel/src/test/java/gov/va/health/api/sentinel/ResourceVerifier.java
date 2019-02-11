package gov.va.health.api.sentinel;

import gov.va.api.health.argonaut.api.bundle.AbstractBundle;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import java.util.Arrays;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/** This support class can be used to test standard resource queries, such as reads and searches. */
@Slf4j
public class ResourceVerifier {

  private static final ResourceVerifier INSTANCE = new ResourceVerifier();

  private static final String apiPath = Sentinel.get().system().argonaut().apiPath();

  @Getter(lazy = true)
  private final Sentinel sentinel = Sentinel.get();

  @Getter private final TestClient argonaut = sentinel().clients().argonaut();

  @Getter private final TestIds ids = IdRegistrar.of(sentinel().system()).registeredIds();

  public static ResourceVerifier get() {
    return INSTANCE;
  }

  public static <T> TestCase<T> test(
      int status, Class<T> response, String path, String... parameters) {
    return TestCase.<T>builder()
        .path(apiPath + path)
        .parameters(parameters)
        .response(response)
        .status(status)
        .build();
  }

  /**
   * If the response is a bundle, then the query is a search. We want to verify paging parameters
   * restrict page >= 1, _count >=1, and _count <= 20
   */
  public <T> void assertPagingParameterBounds(TestCase<T> tc) {
    if (!AbstractBundle.class.isAssignableFrom(tc.response())) {
      return;
    }
    log.info("Verify {} page bounds", tc.label());
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

  public <T> T assertRequest(TestCase<T> tc) {
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

  public void verifyAll(TestCase<?>... testCases) {
    for (TestCase tc : testCases) {
      try {
        verify(tc);
      } catch (Exception e) {
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
