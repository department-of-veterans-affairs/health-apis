package gov.va.health.api.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.health.api.sentinel.categories.Local;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class IdsIT {

  private String apiPath() {
    return Sentinel.get().system().clients().argonaut().service().apiPath();
  }

  private TestClient client() {
    return Sentinel.get().clients().ids();
  }

  @Category(Local.class)
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
            .get(apiPath() + "resourceIdentity/{id}", registrations.get(0).uuid())
            .expect(200)
            .expectListOf(ResourceIdentity.class);

    assertThat(identities).containsExactly(identity);
  }

  @Category(Local.class)
  @Test
  public void lookupReturns404ForUnknownId() {
    client().get(apiPath() + "v1/ids/{id}", UUID.randomUUID().toString()).expect(404);
  }

  @Category(Local.class)
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
            .post(apiPath() + "v1/ids", Collections.singletonList(identity))
            .expect(201)
            .expectListOf(Registration.class);
    assertThat(registrations.size()).isEqualTo(1);

    List<Registration> repeatedRegistrations =
        client()
            .post(apiPath() + "v1/ids", Collections.singletonList(identity))
            .expect(201)
            .expectListOf(Registration.class);
    assertThat(repeatedRegistrations).isEqualTo(registrations);

    List<ResourceIdentity> identities =
        client()
            .get(apiPath() + "v1/ids/{id}", registrations.get(0).uuid())
            .expect(200)
            .expectListOf(ResourceIdentity.class);

    assertThat(identities).containsExactly(identity);
  }

  @Category(Local.class)
  @Test
  public void registerPatientFlowUsesPatientProvidedIdentifier() {
    String icn = "185601V825290";
    ResourceIdentity identity =
        ResourceIdentity.builder().system("CDW").resource("PATIENT").identifier(icn).build();

    List<Registration> registrations =
        client()
            .post(apiPath() + "v1/ids", Collections.singletonList(identity))
            .expect(201)
            .expectListOf(Registration.class);
    assertThat(registrations.size()).isEqualTo(1);

    List<ResourceIdentity> identities =
        client()
            .get(apiPath() + "v1/ids/{id}", icn)
            .expect(200)
            .expectListOf(ResourceIdentity.class);

    assertThat(identities).containsExactly(identity);
  }

  @Category(Local.class)
  @Test
  public void registerReturns400ForInvalidRequest() {
    ResourceIdentity identity =
        ResourceIdentity.builder().system("CDW").resource("WHATEVER").identifier(null).build();
    client().post(apiPath() + "v1/ids", Collections.singletonList(identity)).expect(400);
  }
}
