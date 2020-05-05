package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.Transformers.asDateString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
