package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.stu3.api.elements.Reference;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class Stu3TransformersTest {

  @Test
  public void allBlank() {
    assertThat(Stu3Transformers.allBlank()).isTrue();
    assertThat(Stu3Transformers.allBlank(null, null, null, null)).isTrue();
    assertThat(Stu3Transformers.allBlank(null, "", " ")).isTrue();
    assertThat(Stu3Transformers.allBlank(null, 1, null, null)).isFalse();
    assertThat(Stu3Transformers.allBlank(1, "x", "z", 2.0)).isFalse();
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
  public void isBlankCollection() {
    assertThat(isBlank(List.of())).isTrue();
    assertThat(isBlank(List.of("x"))).isFalse();
  }

  @Test
  public void isBlankMap() {
    assertThat(isBlank(Map.of())).isTrue();
    assertThat(isBlank(Map.of("x", "y"))).isFalse();
  }
}
