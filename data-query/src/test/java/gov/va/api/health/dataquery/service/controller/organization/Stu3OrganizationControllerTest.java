package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.Stu3Bundler;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.stu3.api.bundle.BundleLink;
import gov.va.api.health.stu3.api.resources.Organization;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class Stu3OrganizationControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private OrganizationRepository repository;

  @SneakyThrows
  private static OrganizationEntity asEntity(DatamartOrganization dm) {
    return OrganizationEntity.builder()
        .cdwId(dm.cdwId())
        .npi(dm.npi().orElse(null))
        .name(dm.name())
        .street(
            trimToNull(trimToEmpty(dm.address().line1()) + " " + trimToEmpty(dm.address().line2())))
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

  private Stu3OrganizationController controller() {
    return new Stu3OrganizationController(
        new Stu3Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @Test
  public void emptyChecks() {
    String emptyness = " ";
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    assertThat(asJson(controller().searchByName(emptyness, 1, 0)))
        .isEqualTo(
            asJson(
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?name=" + emptyness,
                        1,
                        0))));
    assertThat(asJson(controller().searchByAddress(emptyness, null, null, null, 1, 0)))
        .isEqualTo(
            asJson(
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?address=" + emptyness,
                        1,
                        0))));
    Organization.Bundle actuallyEmpty =
        controller().searchByIdentifier("http://hl7.org/fhir/sid/us-npi|" + emptyness, 1, 0);
    assertThat(asJson(actuallyEmpty))
        .isEqualTo(
            asJson(
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?identifier=http://hl7.org/fhir/sid/us-npi|"
                            + emptyness,
                        1,
                        0))));
  }

  @Test
  public void read() {
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    repository.save(asEntity(dm));
    Organization actual = controller().read("1234");
    assertThat(actual).isEqualTo(OrganizationSamples.Stu3.create().organization("1234"));
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

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    addMockIdentities("x", "y");
    controller().readRaw("x", mock(HttpServletResponse.class));
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("x", mock(HttpServletResponse.class));
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenDataIsMissing() {
    addMockIdentities("x", "y");
    controller().read("x");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readThrowsNotFoundWhenIdIsUnknown() {
    controller().read("x");
  }

  @Test(expected = ResourceExceptions.MissingSearchParameters.class)
  public void searchAddressMissingParameters() {
    controller().searchByAddress(null, null, null, null, 1, 1);
  }

  @Test
  public void searchByCity() {
    String city = "NEW AMSTERDAM";
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByAddress(null, city, null, null, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(OrganizationSamples.Stu3.create().organization(publicId)),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Organization?address-city=" + city,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?address-city=" + city,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Organization?address-city=" + city,
                        1,
                        1))));
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
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(OrganizationSamples.Stu3.create().organization(publicId)),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Organization?identifier=" + publicId,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?identifier=" + publicId,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Organization?identifier=" + publicId,
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    String publicId = "abc";
    String cdwId = "123";
    String identifier = "http://hl7.org/fhir/sid/us-npi|1205983228";
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    addMockIdentities(publicId, cdwId);
    Organization.Bundle actual = controller().searchByIdentifier(identifier, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(OrganizationSamples.Stu3.create().organization(publicId)),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Organization?identifier=" + identifier,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?identifier=" + identifier,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Organization?identifier=" + identifier,
                        1,
                        1))));
  }

  @Test
  public void searchByName() {
    String name = "NEW AMSTERDAM CBOC";
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByName(name, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(OrganizationSamples.Stu3.create().organization(publicId)),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Organization?name=" + name,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?name=" + name,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Organization?name=" + name,
                        1,
                        1))));
  }

  @Test
  public void searchByPostal() {
    String postal = "44444-4160";
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByAddress(null, null, null, postal, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(OrganizationSamples.Stu3.create().organization(publicId)),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Organization?address-postalcode=" + postal,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?address-postalcode=" + postal,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Organization?address-postalcode=" + postal,
                        1,
                        1))));
  }

  @Test
  public void searchByState() {
    String state = "OH";
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByAddress(null, null, state, null, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(OrganizationSamples.Stu3.create().organization(publicId)),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Organization?address-state=" + state,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?address-state=" + state,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Organization?address-state=" + state,
                        1,
                        1))));
  }

  @Test
  public void searchByStreet() {
    String street = "10 MONROE AVE, SUITE 6B PO BOX 4160";
    String publicId = "abc";
    String cdwId = "123";
    addMockIdentities(publicId, cdwId);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization(cdwId);
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByAddress(street, null, null, null, 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.Stu3.asBundle(
                    "http://fonzy.com/cool",
                    List.of(OrganizationSamples.Stu3.create().organization(publicId)),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Organization?address=" + street,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?address=" + street,
                        1,
                        1),
                    OrganizationSamples.Stu3.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Organization?address=" + street,
                        1,
                        1))));
  }

  @Test(expected = ResourceExceptions.BadSearchParameter.class)
  public void searchIdentifierMissmatchingSystem() {
    controller().searchByIdentifier("xyz|123", 1, 1);
  }
}
