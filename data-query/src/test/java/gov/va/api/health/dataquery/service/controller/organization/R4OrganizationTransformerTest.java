package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization.FacilityId;
import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization.Telecom.System;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Organization;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class R4OrganizationTransformerTest {
  static Stream<Arguments> buildFacilityIdentifier() {
    /*
     * arguments(
     * Station Number: Facility's station number,
     * Facility Type: enum value for facility type,
     * Facility ID: expected Facility ID generated)
     * */
    return Stream.of(
        arguments("123", FacilityId.FacilityType.HEALTH, "vha_123"),
        arguments("456", FacilityId.FacilityType.BENEFITS, "vba_456"),
        arguments("789", FacilityId.FacilityType.VET_CENTER, "vc_789"),
        arguments("135", FacilityId.FacilityType.CEMETERY, "nca_135"),
        arguments("246", FacilityId.FacilityType.NONNATIONAL_CEMETERY, "ncas_246"));
  }

  @Test
  public void address() {
    assertThat(R4OrganizationTransformer.address(null)).isNull();
    assertThat(
            R4OrganizationTransformer.address(
                DatamartOrganization.Address.builder()
                    .line1(" ")
                    .line2(" ")
                    .city(" ")
                    .state(" ")
                    .postalCode(" ")
                    .build()))
        .isNull();
    assertThat(
            R4OrganizationTransformer.address(
                DatamartOrganization.Address.builder().line1("v").build()))
        .isEqualTo(asList(Address.builder().line(asList("v")).text("v").build()));
    assertThat(
            R4OrganizationTransformer.address(
                DatamartOrganization.Address.builder().line2("w").build()))
        .isEqualTo(asList(Address.builder().line(asList("w")).text("w").build()));
    assertThat(
            R4OrganizationTransformer.address(
                DatamartOrganization.Address.builder().city("x").build()))
        .isEqualTo(asList(Address.builder().city("x").text("x").build()));
    assertThat(
            R4OrganizationTransformer.address(
                DatamartOrganization.Address.builder().state("y").build()))
        .isEqualTo(asList(Address.builder().state("y").text("y").build()));
    assertThat(
            R4OrganizationTransformer.address(
                DatamartOrganization.Address.builder().postalCode("z").build()))
        .isEqualTo(asList(Address.builder().postalCode("z").text("z").build()));
    assertThat(
            R4OrganizationTransformer.address(
                DatamartOrganization.Address.builder().line1("v").postalCode("z").build()))
        .isEqualTo(asList(Address.builder().line(asList("v")).postalCode("z").text("v z").build()));
    assertThat(
            R4OrganizationTransformer.address(
                DatamartOrganization.Address.builder()
                    .line1("1111 Test Ln")
                    .line2("Apt 1L")
                    .city("Delta")
                    .state("ZZ")
                    .postalCode("22222")
                    .build()))
        .isEqualTo(
            asList(
                Address.builder()
                    .line(asList("1111 Test Ln", "Apt 1L"))
                    .city("Delta")
                    .state("ZZ")
                    .postalCode("22222")
                    .text("1111 Test Ln Apt 1L Delta ZZ 22222")
                    .build()));
  }

  @ParameterizedTest
  @MethodSource
  void buildFacilityIdentifier(
      String stationNumber, FacilityId.FacilityType facilityType, String expectedValue) {
    DatamartOrganization.FacilityId facilityId =
        FacilityId.builder().stationNumber(stationNumber).type(facilityType).build();
    var expected =
        Identifier.builder()
            .use(Identifier.IdentifierUse.usual)
            .type(
                CodeableConcept.builder()
                    .coding(
                        Collections.singletonList(
                            Coding.builder()
                                .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                .code("FI")
                                .display("Facility ID")
                                .build()))
                    .build())
            .system("https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-indentifier")
            .value(expectedValue)
            .build();
    assertThat(R4OrganizationTransformer.buildFacilityIdentifier(facilityId)).isEqualTo(expected);
  }

  @Test
  void empty() {
    assertThat(
            R4OrganizationTransformer.builder()
                .datamart(DatamartOrganization.builder().build())
                .build()
                .toFhir())
        .isEqualTo(Organization.builder().resourceType("Organization").build());
  }

  @Test
  void identifiers() {
    assertThat(R4OrganizationTransformer.identifiers(Optional.empty(), Optional.empty())).isNull();
    assertThat(R4OrganizationTransformer.identifiers(Optional.of("whodis"), Optional.empty()))
        .isEqualTo(
            Collections.singletonList(
                Identifier.builder()
                    .system("http://hl7.org/fhir/sid/us-npi")
                    .value("whodis")
                    .build()));
    assertThat(
            R4OrganizationTransformer.identifiers(
                Optional.of("whodis"),
                Optional.of(
                    DatamartOrganization.FacilityId.builder()
                        .type(DatamartOrganization.FacilityId.FacilityType.HEALTH)
                        .stationNumber("123")
                        .build())))
        .containsExactlyInAnyOrder(
            Identifier.builder().system("http://hl7.org/fhir/sid/us-npi").value("whodis").build(),
            Identifier.builder()
                .use(Identifier.IdentifierUse.usual)
                .type(
                    CodeableConcept.builder()
                        .coding(
                            Collections.singletonList(
                                Coding.builder()
                                    .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                    .code("FI")
                                    .display("Facility ID")
                                    .build()))
                        .build())
                .system(
                    "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-indentifier")
                .value("vha_123")
                .build());
    assertThat(
            R4OrganizationTransformer.identifiers(
                Optional.empty(),
                Optional.of(
                    DatamartOrganization.FacilityId.builder()
                        .type(DatamartOrganization.FacilityId.FacilityType.HEALTH)
                        .stationNumber("123")
                        .build())))
        .isEqualTo(
            Collections.singletonList(
                Identifier.builder()
                    .use(Identifier.IdentifierUse.usual)
                    .type(
                        CodeableConcept.builder()
                            .coding(
                                Collections.singletonList(
                                    Coding.builder()
                                        .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                        .code("FI")
                                        .display("Facility ID")
                                        .build()))
                            .build())
                    .system(
                        "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-indentifier")
                    .value("vha_123")
                    .build()));
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  void organization() {
    assertThat(
            json(
                R4OrganizationTransformer.builder()
                    .datamart(OrganizationSamples.Datamart.create().organization())
                    .build()
                    .toFhir()))
        .isEqualTo(json(OrganizationSamples.R4.create().organization()));
  }

  @Test
  void telecom() {
    assertThat(R4OrganizationTransformer.telecom(null)).isNull();
    assertThat(
            R4OrganizationTransformer.telecom(
                DatamartOrganization.Telecom.builder()
                    .system(System.phone)
                    .value("whodis")
                    .build()))
        .isEqualTo(ContactPoint.builder().system(ContactPointSystem.phone).value("whodis").build());
  }

  @Test
  void telecomSystem() {
    assertThat(R4OrganizationTransformer.telecomSystem(null)).isNull();
    assertThat(R4OrganizationTransformer.telecomSystem(System.phone))
        .isEqualTo(ContactPointSystem.phone);
    assertThat(R4OrganizationTransformer.telecomSystem(System.fax))
        .isEqualTo(ContactPointSystem.fax);
  }

  @Test
  void telecoms() {
    assertThat(R4OrganizationTransformer.telecoms(Collections.EMPTY_LIST)).isNull();
    assertThat(
            R4OrganizationTransformer.telecoms(
                List.of(
                    DatamartOrganization.Telecom.builder().value("1").system(System.phone).build(),
                    DatamartOrganization.Telecom.builder().value("2").system(System.fax).build())))
        .isEqualTo(
            List.of(
                ContactPoint.builder().value("1").system(ContactPointSystem.phone).build(),
                ContactPoint.builder().value("2").system(ContactPointSystem.fax).build()));
  }
}
