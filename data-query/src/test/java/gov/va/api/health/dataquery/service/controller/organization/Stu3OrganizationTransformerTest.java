package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.resources.Organization;
import java.util.Optional;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.SneakyThrows;
import org.junit.Test;

public class Stu3OrganizationTransformerTest {

  @Test
  public void address() {
    assertThat(Stu3OrganizationTransformer.address(null)).isNull();
    assertThat(
            Stu3OrganizationTransformer.address(
                DatamartOrganization.Address.builder()
                    .line1(" ")
                    .line2(" ")
                    .city(" ")
                    .state(" ")
                    .postalCode(" ")
                    .build()))
        .isNull();
    assertThat(
            Stu3OrganizationTransformer.address(
                DatamartOrganization.Address.builder()
                    .line1("1111 Test Ln")
                    .city("Delta")
                    .state("ZZ")
                    .postalCode("22222")
                    .build()))
        .isEqualTo(
            asList(
                Organization.OrganizationAddress.builder()
                    .text("1111 Test Ln  Delta ZZ 22222")
                    .line(asList("1111 Test Ln", null))
                    .city("Delta")
                    .state("ZZ")
                    .postalCode("22222")
                    .build()));
  }

  @Test
  public void identifier() {
    assertThat(Stu3OrganizationTransformer.identifier(Optional.empty())).isNull();
    assertThat(Stu3OrganizationTransformer.identifier(Optional.of("abc")))
        .isEqualTo(
            asList(
                Organization.OrganizationIdentifier.builder()
                    .system("http://hl7.org/fhir/sid/us-npi")
                    .value("abc")
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
                Stu3OrganizationTransformer.builder()
                    .datamart(OrganizationSamples.Datamart.create().organization())
                    .build()
                    .toFhir()))
        .isEqualTo(json(OrganizationSamples.Stu3.create().organization()));
  }

  @Test
  public void telecom() {
    assertThat(Stu3OrganizationTransformer.telecom(null)).isNull();
    assertThat(
            Stu3OrganizationTransformer.telecom(
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
    assertThat(Stu3OrganizationTransformer.telecomSystem(null)).isNull();
    assertThat(Stu3OrganizationTransformer.telecomSystem(DatamartOrganization.Telecom.System.phone))
        .isEqualTo(ContactPoint.ContactPointSystem.phone);
  }

}
