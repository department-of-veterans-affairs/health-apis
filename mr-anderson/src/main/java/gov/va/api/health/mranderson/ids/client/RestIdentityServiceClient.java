package gov.va.api.health.mranderson.ids.client;

import static gov.va.api.health.mranderson.util.Checks.notNull;

import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Rest client for the identity service. Requires `identityservice.url` to be defined a property.
 */
@Component
@AllArgsConstructor(onConstructor = @__({@Autowired}))
@Slf4j
public class RestIdentityServiceClient implements IdentityService {
  private final RestTemplate restTemplate;

  @Value("${identityservice.url}")
  private final String url;

  private HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    return headers;
  }

  @Override
  public List<ResourceIdentity> lookup(String id) {
    log.info("Looking up {}", id);
    restTemplate.setErrorHandler(new LookupErrorHandler(id));
    ResponseEntity<List<ResourceIdentity>> response =
        notNull(
            restTemplate.exchange(
                url + "/api/resourceIdentity/{id}",
                HttpMethod.GET,
                new HttpEntity<List<ResourceIdentity>>(headers()),
                new ParameterizedTypeReference<List<ResourceIdentity>>() {},
                id));
    List<ResourceIdentity> body = notNull(response.getBody());
    log.info("{} {}", response.getStatusCode(), body);
    if (body.isEmpty()) {
      throw new LookupFailed(
          id, "No identities returned, but status was " + response.getStatusCode());
    }
    return body;
  }

  @Override
  public List<Registration> register(List<ResourceIdentity> identities) {
    log.info("Registering {} identities", identities.size());
    log.debug("Registering {}", identities);
    restTemplate.setErrorHandler(new RegisterErrorHandler());
    ResponseEntity<List<Registration>> response =
        notNull(
            restTemplate.exchange(
                url + "/api/resourceIdentity",
                HttpMethod.POST,
                new HttpEntity<>(identities, headers()),
                new ParameterizedTypeReference<List<Registration>>() {}));
    List<Registration> body = notNull(response.getBody());
    log.debug("{}: {} identities registered", response.getStatusCode(), body.size());
    if (body.isEmpty()) {
      throw new RegistrationFailed(
          "No registrations returned, but status was " + response.getStatusCode());
    }
    return body;
  }

  @AllArgsConstructor
  static class LookupErrorHandler implements ResponseErrorHandler {
    private final String id;

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
      if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new UnknownIdentity(id);
      }
      if (response.getStatusCode() != HttpStatus.OK) {
        throw new LookupFailed(id, "Http Response: " + response.getStatusCode());
      }
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
      return response.getStatusCode().isError();
    }
  }

  static class RegisterErrorHandler implements ResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
      if (response.getStatusCode() != HttpStatus.OK) {
        throw new RegistrationFailed("Http Response: " + response.getStatusCode());
      }
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
      return response.getStatusCode().isError();
    }
  }
}
