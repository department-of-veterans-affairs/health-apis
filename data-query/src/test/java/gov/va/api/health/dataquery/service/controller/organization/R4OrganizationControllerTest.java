package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Organization;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class R4OrganizationControllerTest {
  private final IdentityService ids = mock(IdentityService.class);

  @Autowired OrganizationRepository repository;

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

  @SneakyThrows
  private DatamartOrganization asObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartOrganization.class);
  }

  private Organization.Bundle bundleOf(
      String queryString, Collection<Organization> resources, int page, int count) {
    return OrganizationSamples.R4.asBundle(
        "http://fonzy.com/cool",
        resources,
        resources.size(),
        OrganizationSamples.R4.link(
            BundleLink.LinkRelation.first,
            "http://fonzy.com/cool/Organization?" + queryString,
            page,
            count),
        OrganizationSamples.R4.link(
            BundleLink.LinkRelation.self,
            "http://fonzy.com/cool/Organization?" + queryString,
            page,
            count),
        OrganizationSamples.R4.link(
            BundleLink.LinkRelation.last,
            "http://fonzy.com/cool/Organization?" + queryString,
            page,
            count));
  }

  private R4OrganizationController controller() {
    return new R4OrganizationController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private void mockOrganizationIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("ORGANIZATION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  @Test
  void read() {
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    repository.save(asEntity(dm));
    mockOrganizationIdentity("x", dm.cdwId());
    Organization actual = controller().read("x");
    assertThat(actual).isEqualTo(OrganizationSamples.R4.create().organization("x"));
  }

  @Test
  void readRaw() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    mockOrganizationIdentity("x", dm.cdwId());
    repository.save(asEntity(dm));
    DatamartOrganization actual = asObject(controller().readRaw("x", response));
    assertThat(actual).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test
  void readThrowsNotFoundWhenDataIsMissing() {
    mockOrganizationIdentity("x", "24");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  void searchByAddress() {
    String addressStreet =
        Stream.of(
                OrganizationSamples.ORGANIZATION_ADDRESS_LINE_ONE,
                OrganizationSamples.ORGANIZATION_ADDRESS_LINE_TWO)
            .collect(Collectors.joining(" "));
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    repository.save(asEntity(dm));
    Organization.Bundle actual =
        controller().searchByAddress(addressStreet, null, null, null, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "address=" + encode(addressStreet),
                    List.of(OrganizationSamples.R4.create().organization()),
                    1,
                    15)));
  }

  @Test
  void searchByAddressCity() {
    String city = OrganizationSamples.ORGANIZATION_ADDRESS_CITY;
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByAddress(null, city, null, null, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "address-city=" + encode(city),
                    List.of(OrganizationSamples.R4.create().organization()),
                    1,
                    15)));
  }

  @Test
  void searchByAddressPostalCode() {
    String postalCode = OrganizationSamples.ORGANIZATION_ADDRESS_POSTAL_CODE;
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByAddress(null, null, null, postalCode, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "address-postalcode=" + postalCode,
                    List.of(OrganizationSamples.R4.create().organization()),
                    1,
                    15)));
  }

  @Test
  void searchByAddressState() {
    String state = OrganizationSamples.ORGANIZATION_ADDRESS_STATE;
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByAddress(null, null, state, null, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "address-state=" + state,
                    List.of(OrganizationSamples.R4.create().organization()),
                    1,
                    15)));
  }

  @Test
  void searchByAddressThrowsMissingSearchParametersWhenAllParamsAreNull() {
    assertThrows(
        ResourceExceptions.MissingSearchParameters.class,
        () -> controller().searchByAddress(null, null, null, null, 1, 15));
  }

  @Test
  void searchById() {
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    mockOrganizationIdentity("x", dm.cdwId());
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchById("x", 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "identifier=x",
                    List.of(OrganizationSamples.R4.create().organization("x")),
                    1,
                    1)));
  }

  @Test
  void searchByIdentifier() {
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    mockOrganizationIdentity("x", dm.cdwId());
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByIdentifier("x", 1, 1);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "identifier=x",
                    List.of(OrganizationSamples.R4.create().organization("x")),
                    1,
                    1)));
  }

  @Test
  void searchByIdentifierWithCount0() {
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    mockOrganizationIdentity("x", dm.cdwId());
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByIdentifier("x", 1, 0);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    1,
                    OrganizationSamples.R4.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?identifier=x",
                        1,
                        0))));
  }

  @Test
  void searchByName() {
    String name = OrganizationSamples.ORGANIZATION_NAME;
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByName(name, 1, 15);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                bundleOf(
                    "name=" + encode(name),
                    List.of(OrganizationSamples.R4.create().organization()),
                    1,
                    15)));
  }

  @Test
  void searchByNameWithCount0() {
    String name = OrganizationSamples.ORGANIZATION_NAME;
    DatamartOrganization dm = OrganizationSamples.Datamart.create().organization();
    repository.save(asEntity(dm));
    Organization.Bundle actual = controller().searchByName(name, 1, 0);
    assertThat(asJson(actual))
        .isEqualTo(
            asJson(
                OrganizationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    1,
                    OrganizationSamples.R4.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Organization?name=" + encode(name),
                        1,
                        0))));
  }
}
