package gov.va.health.api.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class IdsIT {

  private TestClient client() {
    return Sentinel.get().clients().ids();
  }

  private final String apiPath() {
    return Sentinel.get().system().clients().argonaut().service().apiPath();
  }

  @Test
  public void legacyApiSupportedForOldMuleApplications() {
    ResourceIdentity identity =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("WHATEVER")
            .identifier("whatever")
            .build();

    List<Registration> registrations =
        client()
            .post(apiPath() + "resourceIdentity", Collections.singletonList(identity))
            .expect(201)
            .expectListOf(Registration.class);
    assertThat(registrations.size()).isEqualTo(1);

    List<ResourceIdentity> identities =
        client()
            .get("/api/resourceIdentity/{id}", registrations.get(0).uuid())
            .expect(200)
            .expectListOf(ResourceIdentity.class);

    assertThat(identities).containsExactly(identity);
  }

  @Test
  public void lookupReturns404ForUnknownId() {
    client().get("/api/v1/ids/{id}", UUID.randomUUID().toString()).expect(404);
  }

  @Test
  public void registerFlow() {
    ResourceIdentity identity =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("WHATEVER")
            .identifier("whatever")
            .build();

    List<Registration> registrations =
        client()
            .post("/api/v1/ids", Collections.singletonList(identity))
            .expect(201)
            .expectListOf(Registration.class);
    assertThat(registrations.size()).isEqualTo(1);

    List<Registration> repeatedRegistrations =
        client()
            .post("/api/v1/ids", Collections.singletonList(identity))
            .expect(201)
            .expectListOf(Registration.class);
    assertThat(repeatedRegistrations).isEqualTo(registrations);

    List<ResourceIdentity> identities =
        client()
            .get("/api/v1/ids/{id}", registrations.get(0).uuid())
            .expect(200)
            .expectListOf(ResourceIdentity.class);

    assertThat(identities).containsExactly(identity);
  }

  @Test
  public void registerPatientFlowUsesPatientProvidedIdentifier() {
    String icn = "185601V825290";
    ResourceIdentity identity =
        ResourceIdentity.builder().system("CDW").resource("PATIENT").identifier(icn).build();

    List<Registration> registrations =
        client()
            .post("/api/v1/ids", Collections.singletonList(identity))
            .expect(201)
            .expectListOf(Registration.class);
    assertThat(registrations.size()).isEqualTo(1);

    List<ResourceIdentity> identities =
        client().get("/api/v1/ids/{id}", icn).expect(200).expectListOf(ResourceIdentity.class);

    assertThat(identities).containsExactly(identity);
  }

  @Test
  public void registerReturns400ForInvalidRequest() {
    ResourceIdentity identity =
        ResourceIdentity.builder().system("CDW").resource("WHATEVER").identifier(null).build();
    client().post("/api/v1/ids", Collections.singletonList(identity)).expect(400);
  }
}
