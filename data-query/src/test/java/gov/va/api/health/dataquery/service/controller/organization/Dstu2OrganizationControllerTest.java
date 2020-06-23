package gov.va.api.health.dataquery.service.controller.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.resources.Organization;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
public class Dstu2OrganizationControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private OrganizationRepository repository;

  @SneakyThrows
  private static OrganizationEntity asEntity(DatamartOrganization dm) {
    return OrganizationEntity.builder()
        .cdwId(dm.cdwId())
        .name(dm.name())
        .city(dm.address().city())
        .state(dm.address().state())
        .postalCode(dm.address().postalCode())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @SneakyThrows
  private static String asJson(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  private void addMockIdentities(String orgPubId, String orgCdwId) {
    ResourceIdentity orgResource =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("ORGANIZATION")
            .identifier(orgCdwId)
            .build();
    when(ids.lookup(orgPubId)).thenReturn(List.of(orgResource));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(orgPubId)
                    .resourceIdentities(List.of(orgResource))
                    .build()));
  }

  @SneakyThrows
  private DatamartOrganization asObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartOrganization.class);
  }

  private Dstu2OrganizationController controller() {
    return new Dstu2OrganizationController(
        new Dstu2Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @Test
  public void read() {
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    repository.save(asEntity(dm));
    Organization actual = controller().read("1234");
    assertThat(actual).isEqualTo(OrganizationSamples.Dstu2.create().organization("1234"));
  }

  @Test
  public void readRaw() {
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    String json = controller().readRaw(publicId, servletResponse);
    assertThat(asObject(json)).isEqualTo(dm);
    verify(servletResponse).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    addMockIdentities("x", "y");
    assertThrows(
        ResourceExceptions.NotFound.class,
        () -> controller().readRaw("x", mock(HttpServletResponse.class)));
  }

  @Test
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(
        ResourceExceptions.NotFound.class,
        () -> controller().readRaw("x", mock(HttpServletResponse.class)));
  }

  @Test
  public void readThrowsNotFoundWhenDataIsMissing() {
    addMockIdentities("x", "y");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  public void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  public void searchById() {
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchById(publicId, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(OrganizationSamples.Dstu2.create().organization(publicId)),
                    OrganizationSamples.Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Organization?identifier=" + publicId,
                        1,
                        1),
                    OrganizationSamples.Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Organization?identifier=" + publicId,
                        1,
                        1),
                    OrganizationSamples.Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Organization?identifier=" + publicId,
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByIdentifier(publicId, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(OrganizationSamples.Dstu2.create().organization(publicId)),
                    OrganizationSamples.Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Organization?identifier=" + publicId,
                        1,
                        1),
                    OrganizationSamples.Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/Organization?identifier=" + publicId,
                        1,
                        1),
                    OrganizationSamples.Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Organization?identifier=" + publicId,
                        1,
                        1))));
  }
}
