package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.MultiValueMap;

public class WitnessProtectionTest {

  @Mock IdentityService ids;
  WitnessProtection wp;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    wp = new WitnessProtection(ids);
  }

  @Test
  public void registerAndUpdateModifiesReferences() {
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                registration("WHATEVER", "x"),
                registration("WHATEVER", "y"),
                registration("WHATEVER", "z"),
                registration("EVERYONE", "a")));
    Map<String, List<DatamartReference>> refs =
        Map.of(
            "x",
            List.of(
                DatamartReference.of().type("whatever").reference("xcdw").build(),
                DatamartReference.of().type("everyone").reference("acdw").build()),
            "y",
            List.of(
                DatamartReference.of().type("whatever").reference("ycdw").build(),
                DatamartReference.of().type("everyone").reference("acdw").build()),
            "z",
            List.of(
                DatamartReference.of().type("whatever").reference("zcdw").build(),
                DatamartReference.of().type("everyone").reference("acdw").build()));

    wp.registerAndUpdateReferences(List.of("x", "y", "z"), s -> refs.get(s).stream());
    assertThat(refs.get("x").get(0).reference().get()).isEqualTo("x");
    assertThat(refs.get("x").get(1).reference().get()).isEqualTo("a");
    assertThat(refs.get("y").get(0).reference().get()).isEqualTo("y");
    assertThat(refs.get("y").get(1).reference().get()).isEqualTo("a");
    assertThat(refs.get("z").get(0).reference().get()).isEqualTo("z");
    assertThat(refs.get("z").get(1).reference().get()).isEqualTo("a");
  }

  @Test
  public void registerEmptyReturnsEmpty() {
    assertThat(wp.register(null)).isEmpty();
    assertThat(wp.register(List.of())).isEmpty();
  }

  private Registration registration(String resource, String id) {
    return Registration.builder()
        .uuid(id)
        .resourceIdentity(
            ResourceIdentity.builder()
                .system("CDW")
                .resource(resource)
                .identifier(id + "cdw")
                .build())
        .build();
  }

  @Test
  public void replacePublicIdsWithCdwIdsReplacesValues() {
    when(ids.lookup("x"))
        .thenReturn(
            List.of(
                ResourceIdentity.builder().system("CDW").resource("X").identifier("XXX").build()));
    MultiValueMap<String, String> actual =
        wp.replacePublicIdsWithCdwIds(Parameters.forIdentity("x"));
    assertThat(actual).isEqualTo(Parameters.forIdentity("XXX"));
  }

  @Test(expected = ResourceExceptions.SearchFailed.class)
  public void replacePublicIdsWithCdwIdsThrowsSearchFailedIfIdsFails() {
    when(ids.lookup(Mockito.any())).thenThrow(new IdentityService.LookupFailed("x", "x"));
    wp.replacePublicIdsWithCdwIds(Parameters.forIdentity("x"));
  }

  @Test(expected = ResourceExceptions.UnknownIdentityInSearchParameter.class)
  public void replacePublicIdsWithCdwIdsThrowsUnknownIdentityIfIdsFails() {
    when(ids.lookup(Mockito.any())).thenThrow(new IdentityService.UnknownIdentity("x"));
    wp.replacePublicIdsWithCdwIds(Parameters.forIdentity("x"));
  }
}
