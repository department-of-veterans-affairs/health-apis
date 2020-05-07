package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asCoding;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asDatamartReference;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asReferenceId;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.Test;

public class Dstu2TransformersTest {
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
  public void asCodingReturnsNullWhenOptionalHasNoValues() {
    assertThat(asCoding(Optional.of(DatamartCoding.of().build()))).isNull();
  }

  @Test
  public void asCodingReturnsNullWhenOptionalIsEmpty() {
    assertThat(asCoding(Optional.empty())).isNull();
  }

  @Test
  public void asCodingReturnsNullWhenOptionalIsNull() {
    assertThat(asCoding((Optional<DatamartCoding>) null)).isNull();
  }

  @Test
  public void asCodingReturnsNullWhenValueIsNull() {
    assertThat(asCoding((DatamartCoding) null)).isNull();
  }

  @Test
  public void asCodingReturnsValueWhenOptionalIsPresent() {
    assertThat(
            asCoding(Optional.of(DatamartCoding.of().system("s").code("c").display("d").build())))
        .isEqualTo(Coding.builder().system("s").code("c").display("d").build());
  }

  @Test
  public void asDatamartReferenceReturnsNullWhenRefIsEmpty() {
    Reference ref = Reference.builder().build();
    assertThat(asDatamartReference(ref)).isNull();
  }

  @Test
  public void asDatamartReferenceReturnsRef() {
    Reference ref = Reference.builder().display("WAT").reference("Patient/123V456").build();
    DatamartReference dataRef = asDatamartReference(ref);
    assertThat(dataRef.type().get()).isEqualTo("Patient");
    assertThat(dataRef.reference().get()).isEqualTo("123V456");
    assertThat(dataRef.display().get()).isEqualTo("WAT");
  }

  @Test
  public void asReferenceIdReturnsNullWhenRefIsEmpty() {
    Reference ref = Reference.builder().build();
    assertThat(asReferenceId(ref)).isNull();
  }

  @Test
  public void asReferenceIdReturnsRefId() {
    Reference ref = Reference.builder().display("WAT").reference("Patient/123V456").build();
    assertThat(asReferenceId(ref)).isEqualTo("123V456");
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
  public void convertReturnsConvertedWhenItemIsPopulated() {
    Function<Integer, String> tx = o -> "x" + o;
    assertThat(convert(1, tx)).isEqualTo("x1");
  }

  @Test
  public void convertReturnsNullWhenItemIsNull() {
    Function<String, String> tx = o -> "x" + o;
    assertThat(convert(null, tx)).isNull();
  }

  @Test
  public void emptyToNullReturnsNonNullEntries() {
    assertThat(emptyToNull(Arrays.asList("a", null, "b", null))).isEqualTo(List.of("a", "b"));
  }

  @Test
  public void emptyToNullReturnsNullIfEmpty() {
    assertThat(emptyToNull(List.of())).isNull();
  }

  @Test
  public void emptyToNullReturnsNullIfNull() {
    assertThat(emptyToNull(null)).isNull();
  }

  @Test
  public void ifPresentReturnsExtractWhenObjectIsNull() {
    Function<Object, String> extract = (o) -> "x" + o;
    assertThat(ifPresent("a", extract)).isEqualTo("xa");
  }

  @Test
  public void ifPresentReturnsNullWhenObjectIsNull() {
    Function<Object, String> extract = (o) -> "x" + o;
    assertThat(ifPresent(null, extract)).isNull();
  }
}
