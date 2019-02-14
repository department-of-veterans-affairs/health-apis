package gov.va.api.health.ids.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.ids.api.IdentityService.LookupFailed;
import gov.va.api.health.ids.api.IdentityService.RegistrationFailed;
import gov.va.api.health.ids.api.IdentityService.UnknownIdentity;
import lombok.SneakyThrows;
import org.junit.Test;

public class ModelTest {
  @SuppressWarnings("ThrowableNotThrown")
  @Test
  public void exceptionConstructors() {
    new UnknownIdentity("some id");
    new LookupFailed("some id", "some reason");
    new RegistrationFailed("some reason");
  }

  private ResourceIdentity id() {
    return ResourceIdentity.builder().identifier("i1").resource("r1").system("s1").build();
  }

  @Test
  public void registration() {
    roundTrip(Registration.builder().uuid("u1").resourceIdentity(id()).build());
  }

  @Test
  public void resouceIdentity() {
    roundTrip(id());
  }

  @SneakyThrows
  private <T> void roundTrip(T object) {
    ObjectMapper mapper = new JacksonConfig().objectMapper();
    String json = mapper.writeValueAsString(object);
    Object evilTwin = mapper.readValue(json, object.getClass());
    assertThat(evilTwin).isEqualTo(object);
  }
}
