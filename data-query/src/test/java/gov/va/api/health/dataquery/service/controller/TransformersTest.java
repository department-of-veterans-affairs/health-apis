package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.Transformers.asBigDecimal;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDatamartReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReferenceId;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.Test;

public class TransformersTest {

  @Test
  public void allBlank() {
    assertThat(Transformers.allBlank()).isTrue();
    assertThat(Transformers.allBlank(null, null, null, null)).isTrue();
    assertThat(Transformers.allBlank(null, "", " ")).isTrue();
    assertThat(Transformers.allBlank(null, 1, null, null)).isFalse();
    assertThat(Transformers.allBlank(1, "x", "z", 2.0)).isFalse();
  }

  @Test
  public void asBigDecimalEmptyTest() {
    Optional<Double> empty = Optional.empty();
    assertThat(asBigDecimal(empty)).isNull();
  }

  @Test
  public void asBigDecimalValuesTest() {
    Optional<Double> optionalDouble = Optional.of(14.0);
    Optional<Integer> optionalInteger = Optional.of(23);
    Optional<Long> optionalLong = Optional.of(Long.MAX_VALUE);
    Optional<String> optionalString = Optional.of("COOL COOL COOL");

    assertThat(asBigDecimal(optionalDouble)).isEqualTo(BigDecimal.valueOf(14.0));
    assertThat(asBigDecimal(optionalInteger)).isEqualTo(BigDecimal.valueOf(23));
    assertThat(asBigDecimal(optionalLong)).isEqualTo(BigDecimal.valueOf(Long.MAX_VALUE));
    assertThat(asBigDecimal(optionalString)).isNull();
  }

  @Test
  public void asDatamartReferenceReturnsNullWhenRefIsEmpty() {
    Reference ref = Reference.builder().build();
    assertThat(asDatamartReference(ref)).isNull();
  }

  @Test
  public void asDatamartReferenceReturnsNullWhenRefIsNotResourceSlashId() {
    assertThat(asDatamartReference(Reference.builder().reference("no").build())).isNull();
    assertThat(asDatamartReference(Reference.builder().reference("/no").build())).isNull();
    assertThat(asDatamartReference(Reference.builder().reference("no/").build())).isNull();
    assertThat(asDatamartReference(Reference.builder().reference("no/ ").build())).isNull();
    assertThat(asDatamartReference(Reference.builder().reference("no/no").build())).isNull();
    assertThat(asDatamartReference(Reference.builder().reference("whatever/NO/").build())).isNull();
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
  public void asDateStringReturnsNullWhenInstantIsNull() {
    assertThat(asDateString((LocalDate) null)).isNull();
  }

  @Test
  public void asDateStringReturnsNullWhenOptionalInstantIsEmpty() {
    assertThat(asDateString(Optional.empty())).isNull();
  }

  @Test
  public void asDateStringReturnsNullWhenOptionalInstantIsNull() {
    assertThat(asDateString((Optional<LocalDate>) null)).isNull();
  }

  @Test
  public void asDateStringReturnsStringWhenInstantIsNotNull() {
    LocalDate time = LocalDate.parse("2005-01-21");
    assertThat(asDateString(time)).isEqualTo("2005-01-21");
  }

  @Test
  public void asDateStringReturnsStringWhenOptionalInstantIsNotNull() {
    LocalDate time = LocalDate.parse("2005-01-21");
    assertThat(asDateString(Optional.of(time))).isEqualTo("2005-01-21");
  }

  @Test
  public void asDateTimeStringReturnsNullWhenInstantIsNull() {
    assertThat(asDateTimeString((Instant) null)).isNull();
  }

  @Test
  public void asDateTimeStringReturnsNullWhenOptionalInstantIsEmpty() {
    assertThat(asDateTimeString(Optional.empty())).isNull();
  }

  @Test
  public void asDateTimeStringReturnsNullWhenOptionalInstantIsNull() {
    assertThat(asDateTimeString((Optional<Instant>) null)).isNull();
  }

  @Test
  public void asDateTimeStringReturnsStringWhenInstantIsNotNull() {
    Instant time = Instant.parse("2005-01-21T07:57:00.000Z");
    assertThat(asDateTimeString(time)).isEqualTo("2005-01-21T07:57:00Z");
  }

  @Test
  public void asDateTimeStringReturnsStringWhenOptionalInstantIsPresent() {
    Instant time = Instant.parse("2005-01-21T07:57:00.000Z");
    assertThat(asDateTimeString(Optional.of(time))).isEqualTo("2005-01-21T07:57:00Z");
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

  @Test
  public void isBlankOptional() {
    assertThat(isBlank(Optional.empty())).isTrue();
    assertThat(isBlank(Optional.of(""))).isTrue();
    assertThat(isBlank(Optional.of("x"))).isFalse();
  }

  @Test
  public void parseInstantParsesWhenPossible() {
    Instant expected = Instant.parse("2005-01-12T07:57:00Z");
    assertThat(parseInstant("2005-01-12T07:57:00Z")).isEqualTo(expected);
    assertThat(parseInstant("2005-01-12T07:57:00")).isEqualTo(expected);
    assertThat(parseInstant("nope")).isNull();
  }
}
