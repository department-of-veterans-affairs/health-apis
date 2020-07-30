package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asCoding;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.eitherTextOrDisplay;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4TransformersTest {

  @Test
  public void asCodeableConceptWrappingReturnsNullIfCodingCannotBeConverted() {
    assertThat(asCodeableConceptWrapping(DatamartCoding.builder().build())).isNull();
    assertThat(asCodeableConceptWrapping(Optional.empty())).isNull();
  }

  @Test
  public void asCodeableConceptWrappingReturnsValueIfCodingCanBeConverted() {
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
  public void asReferenceReturnsNullWhenOptionalRefHasDisplayAndTypeAndReference() {
    DatamartReference ref = DatamartReference.of().display("d").type("t").reference("r").build();
    assertThat(asReference(Optional.of(ref)))
        .isEqualTo(Reference.builder().display("d").reference("t/r").build());
  }

  @Test
  public void asReferenceReturnsNullWhenOptionalRefIsNull() {
    assertThat(asReference((Optional<DatamartReference>) null)).isNull();
  }

  @Test
  public void asReferenceReturnsNullWhenRefHasDisplay() {
    DatamartReference ref = DatamartReference.of().display("d").build();
    assertThat(asReference(ref)).isEqualTo(Reference.builder().display("d").build());
  }

  @Test
  public void asReferenceReturnsNullWhenRefHasDisplayAndTypeAndReference() {
    DatamartReference ref = DatamartReference.of().display("d").type("t").reference("r").build();
    assertThat(asReference(ref))
        .isEqualTo(Reference.builder().display("d").reference("t/r").build());
  }

  @Test
  public void asReferenceReturnsNullWhenRefHasTypeAndReference() {
    DatamartReference ref = DatamartReference.of().type("t").reference("r").build();
    assertThat(asReference(ref)).isEqualTo(Reference.builder().reference("t/r").build());
  }

  @Test
  public void asReferenceReturnsNullWhenRefIsEmpty() {
    DatamartReference ref = DatamartReference.of().build();
    assertThat(asReference(ref)).isNull();
  }

  @Test
  public void asReferenceReturnsNullWhenRefIsNull() {
    assertThat(asReference((DatamartReference) null)).isNull();
  }

  @Test
  public void coding() {
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
    assertThat(eitherTextOrDisplay("t", Coding.builder().display("d").build())).isEqualTo("t");
    assertThat(eitherTextOrDisplay("t", null)).isEqualTo("t");
    assertThat(eitherTextOrDisplay("", Coding.builder().display("d").build())).isEqualTo("d");
    assertThat(eitherTextOrDisplay(" ", Coding.builder().display("d").build())).isEqualTo("d");
    assertThat(eitherTextOrDisplay(null, Coding.builder().display("d").build())).isEqualTo("d");
    assertThat(eitherTextOrDisplay(null, Coding.builder().build())).isNull();
  }

  @Test
  public void parseInstant() {
    assertThat(R4Transformers.parseInstant("2007-12-03T10:15:30Z"))
        .isEqualTo(Instant.ofEpochSecond(1196676930));
    assertThat(R4Transformers.parseInstant("2007-12-03T10:15:30"))
        .isEqualTo(Instant.ofEpochSecond(1196676930));
  }
}
