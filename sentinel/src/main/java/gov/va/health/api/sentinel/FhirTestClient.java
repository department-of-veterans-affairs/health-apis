package gov.va.health.api.sentinel;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@Builder
@Slf4j
public class FhirTestClient implements TestClient {
  private final ServiceDefinition service;

  private final ExecutorService executorService = Executors.newFixedThreadPool(threadCount());

  Supplier<ObjectMapper> mapper;

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
   * Remove data from the OO that is unique for each instance. This includes the generated ID and
   * the timestamp.
   */
  private OperationOutcome asOperationOutcomeWithoutDiagnostics(ResponseBody<?> body) {
    try {
      OperationOutcome oo = body.as(OperationOutcome.class);
      oo.id("REMOVED-FOR-COMPARISON");
      oo.issue()
          .forEach(
              i -> {
                if (i.diagnostics() != null) {
                  i.diagnostics(
                      i.diagnostics()
                          .replaceAll("Timestamp:.*(\n|$)", "Timestamp:REMOVED-FOR-COMPARISON"));
                }
              });
      return oo;
    } catch (Exception e) {
      log.error("Failed read response as OperationOutcome: {}", body.prettyPrint());
      throw e;
    }
  }

  @Override
  @SneakyThrows
  public ExpectedResponse get(String path, String... params) {
    Future<Response> baselineResponseFuture =
        executorService.submit(
            () -> {
              return get("application/json", path, params);
            });

    if (path.startsWith("/actuator")) {
      /* Health checks, metrics, etc. do not have FHIR compliance requirements */
      return ExpectedResponse.of(baselineResponseFuture.get(5, TimeUnit.MINUTES));
    }

    Future<Response> fhirJsonResponseFuture =
        executorService.submit(
            () -> {
              return get("application/fhir+json", path, params);
            });
    Future<Response> jsonFhirResponseFuture =
        executorService.submit(
            () -> {
              return get("application/json+fhir", path, params);
            });

    final Response baselineResponse = baselineResponseFuture.get(5, TimeUnit.MINUTES);
    final Response fhirJsonResponse = fhirJsonResponseFuture.get(5, TimeUnit.MINUTES);
    final Response jsonFhirResponse = jsonFhirResponseFuture.get(5, TimeUnit.MINUTES);

    assertThat(fhirJsonResponse.getStatusCode())
        .withFailMessage(
            "status: application/json ("
                + baselineResponse.getStatusCode()
                + ") does not equal application/fhir+json ("
                + fhirJsonResponse.getStatusCode()
                + ")")
        .isEqualTo(baselineResponse.getStatusCode());
    assertThat(jsonFhirResponse.getStatusCode())
        .withFailMessage(
            "status: application/json ("
                + baselineResponse.getStatusCode()
                + ") does not equal application/json+fhir ("
                + jsonFhirResponse.getStatusCode()
                + ")")
        .isEqualTo(baselineResponse.getStatusCode());

    if (baselineResponse.getStatusCode() >= 400) {
      /*
       * Error responses must be returned as OOs but contain a timestamp in the diagnostics
       * that prevents direct comparison.
       */
      assertThat(asOperationOutcomeWithoutDiagnostics(baselineResponse.body()))
          .isEqualTo(asOperationOutcomeWithoutDiagnostics(fhirJsonResponse.body()))
          .isEqualTo(asOperationOutcomeWithoutDiagnostics(jsonFhirResponse.body()));
    } else {
      // OK responses
      assertThat(baselineResponse.body().asString())
          .isEqualTo(fhirJsonResponse.body().asString())
          .isEqualTo(jsonFhirResponse.body().asString());
    }
    return ExpectedResponse.of(baselineResponse);
  }

  private Response get(String contentType, String path, Object[] params) {
    return service()
        .requestSpecification()
        .contentType(contentType)
        .accept(contentType)
        .request(Method.GET, path, params);
  }

  @Override
  public ExpectedResponse post(String path, Object body) {
    return ExpectedResponse.of(
        service()
            .requestSpecification()
            .contentType("application/fhir+json")
            .accept("application/fhir+json")
            .body(body)
            .request(Method.POST, path));
  }
}
