package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReferenceId;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.r4.api.elements.Reference;
import java.util.Optional;
import org.junit.Test;

public class R4TransformersTest {
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
}
