package gov.va.api.health.dataquery.service.controller.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.resources.Organization;
import gov.va.api.health.dstu2.api.resources.Organization.Bundle;
import gov.va.dvp.cdw.xsd.model.CdwOrganization100Root;
import gov.va.dvp.cdw.xsd.model.CdwOrganization100Root.CdwOrganizations;
import gov.va.dvp.cdw.xsd.model.CdwOrganization100Root.CdwOrganizations.CdwOrganization;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
import javax.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.MultiValueMap;

@SuppressWarnings("WeakerAccess")
public class OrganizationControllerTest {
  @Mock MrAndersonClient client;

  @Mock OrganizationController.Transformer tx;

  OrganizationController controller;
  @Mock Bundler bundler;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new OrganizationController(tx, client, bundler);
  }

  private void assertSearch(
      Supplier<Organization.Bundle> invocation, MultiValueMap<String, String> params) {
    CdwOrganization100Root root = new CdwOrganization100Root();
    root.setPageNumber(BigInteger.valueOf(1));
    root.setRecordsPerPage(BigInteger.valueOf(10));
    root.setRecordCount(BigInteger.valueOf(3));
    root.setOrganizations(new CdwOrganizations());
    CdwOrganization cdwOrg1 = new CdwOrganization();
    CdwOrganization cdwOrg2 = new CdwOrganization();
    CdwOrganization cdwOrg3 = new CdwOrganization();
    root.getOrganizations().getOrganization().addAll(Arrays.asList(cdwOrg1, cdwOrg2, cdwOrg3));
    Organization org1 = Organization.builder().build();
    Organization org2 = Organization.builder().build();
    Organization org3 = Organization.builder().build();
    when(tx.apply(cdwOrg1)).thenReturn(org1);
    when(tx.apply(cdwOrg2)).thenReturn(org2);
    when(tx.apply(cdwOrg3)).thenReturn(org3);
    when(client.search(Mockito.any())).thenReturn(root);

    Organization.Bundle mockBundle = new Organization.Bundle();
    when(bundler.bundle(Mockito.any())).thenReturn(mockBundle);

    Organization.Bundle actual = invocation.get();

    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<
            BundleContext<CdwOrganization, Organization, Organization.Entry, Organization.Bundle>>
        captor = ArgumentCaptor.forClass(BundleContext.class);

    verify(bundler).bundle(captor.capture());

    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("Organization")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEqualTo(root.getOrganizations().getOrganization());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Organization.Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(Organization.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private Bundle bundleOf(Organization resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                Organization.Entry.builder()
                    .fullUrl("http://example.com")
                    .resource(resource)
                    .build()))
        .build();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void read() {
    CdwOrganization100Root root = new CdwOrganization100Root();
    root.setOrganizations(new CdwOrganizations());
    CdwOrganization100Root.CdwOrganizations.CdwOrganization xmlOrganization =
        new CdwOrganization100Root.CdwOrganizations.CdwOrganization();
    root.getOrganizations().getOrganization().add(xmlOrganization);
    Organization organization = Organization.builder().build();
    when(client.search(Mockito.any())).thenReturn(root);
    when(tx.apply(xmlOrganization)).thenReturn(organization);
    Organization actual = controller.read("hello");
    assertThat(actual).isSameAs(organization);
    ArgumentCaptor<Query<CdwOrganization100Root>> captor = ArgumentCaptor.forClass(Query.class);
    verify(client).search(captor.capture());
    assertThat(captor.getValue().parameters()).isEqualTo(Parameters.forIdentity("hello"));
  }

  @Test
  public void searchById() {
    assertSearch(
        () -> controller.searchById("me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByIdentifier() {
    assertSearch(
        () -> controller.searchByIdentifier("me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    Organization resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-organization-1.01.json"),
                Organization.class);

    Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @Test(expected = ConstraintViolationException.class)
  @SneakyThrows
  public void validateThrowsExceptionForInvalidBundle() {
    Organization resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-organization-1.01.json"),
                Organization.class);
    resource.resourceType(null);

    Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
