package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
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
