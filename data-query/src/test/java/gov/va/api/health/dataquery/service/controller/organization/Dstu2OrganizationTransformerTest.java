package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import lombok.SneakyThrows;
import org.junit.Test;

public class Dstu2OrganizationTransformerTest {

  @Test
  public void address() {
    assertThat(Dstu2OrganizationTransformer.address(null)).isNull();
    assertThat(
            Dstu2OrganizationTransformer.address(
                DatamartOrganization.Address.builder()
                    .line1(" ")
                    .line2(" ")
                    .city(" ")
                    .state(" ")
                    .postalCode(" ")
                    .build()))
        .isNull();
    assertThat(
            Dstu2OrganizationTransformer.address(
                DatamartOrganization.Address.builder()
                    .line1("1111 Test Ln")
                    .city("Delta")
                    .state("ZZ")
                    .postalCode("22222")
                    .build()))
        .isEqualTo(
            asList(
                Address.builder()
                    .line(asList("1111 Test Ln"))
                    .city("Delta")
                    .state("ZZ")
                    .postalCode("22222")
                    .build()));
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void organization() {
    assertThat(
            json(
                Dstu2OrganizationTransformer.builder()
                    .datamart(Dstu2OrganizationSamples.Datamart.create().organization())
                    .build()
                    .toFhir()))
        .isEqualTo(json(Dstu2OrganizationSamples.Dstu2.create().organization()));
  }

  @Test
  public void telecom() {
    assertThat(Dstu2OrganizationTransformer.telecom(null)).isNull();
    assertThat(
            Dstu2OrganizationTransformer.telecom(
                DatamartOrganization.Telecom.builder()
                    .system(DatamartOrganization.Telecom.System.phone)
                    .value("abc")
                    .build()))
        .isEqualTo(
            ContactPoint.builder()
                .system(ContactPoint.ContactPointSystem.phone)
                .value("abc")
                .build());
  }

  @Test
  public void telecomSystem() {
    assertThat(Dstu2OrganizationTransformer.telecomSystem(null)).isNull();
    assertThat(
            Dstu2OrganizationTransformer.telecomSystem(DatamartOrganization.Telecom.System.phone))
        .isEqualTo(ContactPoint.ContactPointSystem.phone);
  }
}
