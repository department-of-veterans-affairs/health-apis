package gov.va.health.api.sentinel;

/**
 * The TestClient provides an abstraction to REST requests for a server that returns Rest Assured
 * responses decorated with easy to assert expectations.
 */
public interface TestClient {

  /**
   * Perform a get request with optional path params.
   *
   * <pre>
   *   tc.get("api/v1/awesome");
   *   tc.get("api/v1/awesome/{animal}/{id}","possum","harvey");
   * </pre>
   */
  ExpectedResponse get(String path, String... params);

  /** Perform a post request with the given body. */
  ExpectedResponse post(String path, Object body);

  ServiceDefinition service();
}
