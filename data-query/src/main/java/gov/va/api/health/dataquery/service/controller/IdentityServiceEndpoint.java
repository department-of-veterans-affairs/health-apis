package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

/**
 * This used to implement a RESTful client for interacting with a standalone Identity service. Now
 * it just proxies to a the IdentityService implementation.
 */
@Component
@Endpoint(id = "ids")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class IdentityServiceEndpoint {
  private final IdentityService identityService;

  @ReadOperation
  public List<ResourceIdentity> decode(@Selector String publicId) {
    return identityService.lookup(publicId);
  }

  /** Convert to an encoded ID using the VISTA system. */
  @ReadOperation
  public List<Registration> encode(@Selector String resource, @Selector String privateId) {
    String system = "PATIENT".equalsIgnoreCase(resource) ? "MVI" : "CDW";
    return identityService.register(
        List.of(
            ResourceIdentity.builder()
                .system(system)
                .resource(resource)
                .identifier(privateId)
                .build()));
  }
}
