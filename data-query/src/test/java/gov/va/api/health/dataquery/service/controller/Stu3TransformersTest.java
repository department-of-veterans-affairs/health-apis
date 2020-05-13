package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asCoding;
import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asReference;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.elements.Reference;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class Stu3TransformersTest {
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
  public void codableConcept() {
    assertThat(asCodeableConceptWrapping(Optional.empty())).isNull();
    assertThat(
            asCodeableConceptWrapping(
                Optional.of(
                    DatamartCoding.builder()
                        .code(Optional.of("code"))
                        .display(Optional.of("display"))
                        .system(Optional.of("system"))
                        .build())))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder().system("system").code("code").display("display").build()))
                .build());
    assertThat(asCodeableConceptWrapping(DatamartCoding.builder().build())).isNull();
    assertThat(
            asCodeableConceptWrapping(
                DatamartCoding.builder()
                    .code(Optional.of("code"))
                    .display(Optional.of("display"))
                    .system(Optional.of("system"))
                    .build()))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder().system("system").code("code").display("display").build()))
                .build());
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
}
