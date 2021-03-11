package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FacilityTransformersTest {
  static Stream<Arguments> fapiFacilityId() {
    // station number, facility type, fapi ID
    return Stream.of(
        arguments("123", FacilityId.FacilityType.HEALTH, "vha_123"),
        arguments("456", FacilityId.FacilityType.BENEFITS, "vba_456"),
        arguments("789", FacilityId.FacilityType.VET_CENTER, "vc_789"),
        arguments("135", FacilityId.FacilityType.CEMETERY, "nca_135"),
        arguments("246", FacilityId.FacilityType.NONNATIONAL_CEMETERY, "nca_s246"));
  }

  @Test
  void facilityIdentifier() {
    assertThat(
            FacilityTransformers.facilityIdentifier(
                FacilityId.builder()
                    .stationNumber("123")
                    .type(FacilityId.FacilityType.HEALTH)
                    .build()))
        .isEqualTo(
            Identifier.builder()
                .use(Identifier.IdentifierUse.usual)
                .type(
                    CodeableConcept.builder()
                        .coding(
                            List.of(
                                Coding.builder()
                                    .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                    .code("FI")
                                    .display("Facility ID")
                                    .build()))
                        .build())
                .system(
                    "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier")
                .value("vha_123")
                .build());
  }

  @Test
  void facilityIdentifier_empty() {
    assertThat(FacilityTransformers.facilityIdentifier(null)).isNull();
    assertThat(FacilityTransformers.facilityIdentifier(FacilityId.builder().build())).isNull();
    assertThat(
            FacilityTransformers.facilityIdentifier(
                FacilityId.builder()
                    .stationNumber(" ")
                    .type(FacilityId.FacilityType.HEALTH)
                    .build()))
        .isNull();
    assertThat(
            FacilityTransformers.facilityIdentifier(
                FacilityId.builder().stationNumber("123").build()))
        .isNull();
  }

  @Test
  void fapiClinicId() {
    assertThat(
            FacilityTransformers.fapiClinicId(
                FacilityId.builder()
                    .stationNumber("123")
                    .type(FacilityId.FacilityType.HEALTH)
                    .build(),
                "456"))
        .isEqualTo("vha_123_456");
  }

  @Test
  void fapiClinicId_empty() {
    assertThat(FacilityTransformers.fapiClinicId(null, null)).isNull();
    assertThat(FacilityTransformers.fapiClinicId(null, "123")).isNull();
    assertThat(FacilityTransformers.fapiClinicId(FacilityId.builder().build(), null)).isNull();
    assertThat(FacilityTransformers.fapiClinicId(FacilityId.builder().build(), "123")).isNull();
    assertThat(
            FacilityTransformers.fapiClinicId(
                FacilityId.builder()
                    .stationNumber(" ")
                    .type(FacilityId.FacilityType.HEALTH)
                    .build(),
                null))
        .isNull();
    assertThat(
            FacilityTransformers.fapiClinicId(
                FacilityId.builder()
                    .stationNumber(" ")
                    .type(FacilityId.FacilityType.HEALTH)
                    .build(),
                "123"))
        .isNull();
    assertThat(
            FacilityTransformers.fapiClinicId(
                FacilityId.builder().stationNumber("123").build(), null))
        .isNull();
    assertThat(
            FacilityTransformers.fapiClinicId(
                FacilityId.builder().stationNumber("123").build(), "123"))
        .isNull();
    assertThat(
            FacilityTransformers.fapiClinicId(
                FacilityId.builder()
                    .stationNumber("123")
                    .type(FacilityId.FacilityType.BENEFITS)
                    .build(),
                "456"))
        .isNull();
  }

  @MethodSource
  @ParameterizedTest
  void fapiFacilityId(String stationNumber, FacilityId.FacilityType facilityType, String expected) {
    assertThat(
            FacilityTransformers.fapiFacilityId(
                FacilityId.builder().stationNumber(stationNumber).type(facilityType).build()))
        .isEqualTo(expected);
  }

  @Test
  void fapiFacilityId_empty() {
    assertThat(FacilityTransformers.fapiFacilityId(null)).isNull();
    assertThat(FacilityTransformers.fapiFacilityId(FacilityId.builder().build())).isNull();
    assertThat(
            FacilityTransformers.fapiFacilityId(
                FacilityId.builder().type(FacilityId.FacilityType.HEALTH).build()))
        .isNull();
    assertThat(
            FacilityTransformers.fapiFacilityId(FacilityId.builder().stationNumber("123").build()))
        .isNull();
  }
}
