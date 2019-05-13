package gov.va.api.health.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.dataquery.api.resources.OperationOutcome;
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

@Slf4j
@Value
@Builder
public final class FhirTestClient implements TestClient {
  private final ServiceDefinition service;

  private final ExecutorService executorService =
      Executors.newFixedThreadPool(
          SentinelProperties.threadCount(
              "sentinel.threads", Runtime.getRuntime().availableProcessors()));

  Supplier<ObjectMapper> mapper;

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
    Response response = null;

    // We'll make the request at least one time and as many as maxAttempts if we get a 500 error.
    final int maxAttempts = 3;
    for (int i = 0; i < maxAttempts; i++) {
      if (i > 0) {
        log.info("Making retry attempt {} for {}:{} after failure.", i, contentType, path);
      }

      response =
          service()
              .requestSpecification()
              .contentType(contentType)
              .accept(contentType)
              .request(Method.GET, path, params);

      if (response.getStatusCode() != 500) {
        break;
      }
    }

    return response;
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
