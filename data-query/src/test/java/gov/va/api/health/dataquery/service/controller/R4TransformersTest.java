package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asCoding;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.textOrElseDisplay;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class R4TransformersTest {
  static Stream<Arguments> facilityIdentifier() {
    /*
     * arguments(
     * Station Number: Facility's station number,
     * Facility Type: enum value for facility type,
     * Facility ID: expected Facility ID generated)
     */
    return Stream.of(
        arguments("123", FacilityId.FacilityType.HEALTH, "vha_123"),
        arguments("456", FacilityId.FacilityType.BENEFITS, "vba_456"),
        arguments("789", FacilityId.FacilityType.VET_CENTER, "vc_789"),
        arguments("135", FacilityId.FacilityType.CEMETERY, "nca_135"),
        arguments("246", FacilityId.FacilityType.NONNATIONAL_CEMETERY, "ncas_246"));
  }

  @Test
  void asCodeableConceptWrappingReturnsNullIfCodingCannotBeConverted() {
    assertThat(asCodeableConceptWrapping(DatamartCoding.builder().build())).isNull();
    assertThat(asCodeableConceptWrapping(Optional.empty())).isNull();
    assertThat(
            R4Transformers.asCodeableConceptWrapping(Optional.of(DatamartCoding.builder().build())))
        .isNull();
  }

  @Test
  void asCodeableConceptWrappingReturnsValueIfCodingCanBeConverted() {
    assertThat(
            asCodeableConceptWrapping(
                DatamartCoding.of().system("s").code("c").display("d").build()))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(List.of(Coding.builder().system("s").code("c").display("d").build()))
                .build());
    assertThat(
            asCodeableConceptWrapping(
                Optional.of(DatamartCoding.of().system("s").code("c").display("d").build())))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(List.of(Coding.builder().system("s").code("c").display("d").build()))
                .build());
  }

  @Test
  void asReferenceReturnsNullWhenOptionalRefHasDisplayAndTypeAndReference() {
    DatamartReference ref = DatamartReference.of().display("d").type("t").reference("r").build();
    assertThat(asReference(Optional.of(ref)))
        .isEqualTo(Reference.builder().display("d").reference("t/r").build());
  }

  @Test
  void asReferenceReturnsNullWhenOptionalRefIsNull() {
    assertThat(asReference((Optional<DatamartReference>) null)).isNull();
  }

  @Test
  void asReferenceReturnsNullWhenRefHasDisplay() {
    DatamartReference ref = DatamartReference.of().display("d").build();
    assertThat(asReference(ref)).isEqualTo(Reference.builder().display("d").build());
  }

  @Test
  void asReferenceReturnsNullWhenRefHasDisplayAndTypeAndReference() {
    DatamartReference ref = DatamartReference.of().display("d").type("t").reference("r").build();
    assertThat(asReference(ref))
        .isEqualTo(Reference.builder().display("d").reference("t/r").build());
  }

  @Test
  void asReferenceReturnsNullWhenRefHasTypeAndReference() {
    DatamartReference ref = DatamartReference.of().type("t").reference("r").build();
    assertThat(asReference(ref)).isEqualTo(Reference.builder().reference("t/r").build());
  }

  @Test
  void asReferenceReturnsNullWhenRefIsEmpty() {
    DatamartReference ref = DatamartReference.of().build();
    assertThat(asReference(ref)).isNull();
  }

  @Test
  void asReferenceReturnsNullWhenRefIsNull() {
    assertThat(asReference((DatamartReference) null)).isNull();
  }

  @MethodSource
  @ParameterizedTest
  void facilityIdentifier(
      String stationNumber, FacilityId.FacilityType facilityType, String expectedValue) {
    FacilityId facilityId =
        FacilityId.builder().stationNumber(stationNumber).type(facilityType).build();
    var expected =
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
            .system("https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier")
            .value(expectedValue)
            .build();
    assertThat(R4Transformers.facilityIdentifier(facilityId)).isEqualTo(expected);
  }

  @Test
  void coding() {
    assertThat(asCoding(Optional.empty())).isNull();
    assertThat(asCoding(Optional.of(DatamartCoding.builder().build()))).isNull();
    assertThat(
            asCoding(
                Optional.of(
                    DatamartCoding.builder()
                        .code(Optional.of("code"))
                        .display(Optional.of("display"))
                        .system(Optional.of("system"))
                        .build())))
        .isEqualTo(Coding.builder().system("system").code("code").display("display").build());
  }

  @Test
  void eitherTextOrDisplayReturns() {
    assertThat(textOrElseDisplay("t", Coding.builder().display("d").build())).isEqualTo("t");
    assertThat(textOrElseDisplay("t", null)).isEqualTo("t");
    assertThat(textOrElseDisplay("", Coding.builder().display("d").build())).isEqualTo("d");
    assertThat(textOrElseDisplay(" ", Coding.builder().display("d").build())).isEqualTo("d");
    assertThat(textOrElseDisplay(null, Coding.builder().display("d").build())).isEqualTo("d");
    assertThat(textOrElseDisplay(null, Coding.builder().build())).isNull();
  }

  @Test
  void parseInstant() {
    assertThat(R4Transformers.parseInstant("2007-12-03T10:15:30Z"))
        .isEqualTo(Instant.ofEpochSecond(1196676930));
    assertThat(R4Transformers.parseInstant("2007-12-03T10:15:30"))
        .isEqualTo(Instant.ofEpochSecond(1196676930));
  }
}
