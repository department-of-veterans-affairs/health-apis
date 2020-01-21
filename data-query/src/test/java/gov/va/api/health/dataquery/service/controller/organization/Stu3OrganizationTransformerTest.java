package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.resources.Organization;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

public class Stu3OrganizationTransformerTest {
  @Test
  public void address() {
    assertThat(Stu3OrganizationTransformer.addresses(null)).isNull();
    assertThat(
            Stu3OrganizationTransformer.addresses(
                DatamartOrganization.Address.builder()
                    .line1(" ")
                    .line2(" ")
                    .city(" ")
                    .state(" ")
                    .postalCode(" ")
                    .build()))
        .isNull();

    assertThat(
            Stu3OrganizationTransformer.addresses(
                DatamartOrganization.Address.builder().line1("v").build()))
        .isEqualTo(
            asList(Organization.OrganizationAddress.builder().line(asList("v")).text("v").build()));
    assertThat(
            Stu3OrganizationTransformer.addresses(
                DatamartOrganization.Address.builder().line2("w").build()))
        .isEqualTo(
            asList(Organization.OrganizationAddress.builder().line(asList("w")).text("w").build()));
    assertThat(
            Stu3OrganizationTransformer.addresses(
                DatamartOrganization.Address.builder().city("x").build()))
        .isEqualTo(asList(Organization.OrganizationAddress.builder().city("x").text("x").build()));
    assertThat(
            Stu3OrganizationTransformer.addresses(
                DatamartOrganization.Address.builder().state("y").build()))
        .isEqualTo(asList(Organization.OrganizationAddress.builder().state("y").text("y").build()));
    assertThat(
            Stu3OrganizationTransformer.addresses(
                DatamartOrganization.Address.builder().postalCode("z").build()))
        .isEqualTo(
            asList(Organization.OrganizationAddress.builder().postalCode("z").text("z").build()));

    assertThat(
            Stu3OrganizationTransformer.addresses(
                DatamartOrganization.Address.builder().line1("v").postalCode("z").build()))
        .isEqualTo(
            asList(
                Organization.OrganizationAddress.builder()
                    .line(asList("v"))
                    .postalCode("z")
                    .text("v z")
                    .build()));

    assertThat(
            Stu3OrganizationTransformer.addresses(
                DatamartOrganization.Address.builder()
                    .line1("1111 Test Ln")
                    .line2("Apt 1L")
                    .city("Delta")
                    .state("ZZ")
                    .postalCode("22222")
                    .build()))
        .isEqualTo(
            asList(
                Organization.OrganizationAddress.builder()
                    .line(asList("1111 Test Ln", "Apt 1L"))
                    .city("Delta")
                    .state("ZZ")
                    .postalCode("22222")
                    .text("1111 Test Ln Apt 1L Delta ZZ 22222")
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
