package gov.va.api.health.dataquery.service.controller.datamart;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class DatamartEntityTest {
  @Test
  public void badPayload() {
    Assertions.assertThrows(
        ResourceExceptions.InvalidDatamartPayload.class,
        () -> new FooEntity().deserializeDatamart("{wat]", Foo.class));
  }

  @Test
  @SneakyThrows
  public void deserializeDatamart() {
    String payload = JacksonConfig.createMapper().writeValueAsString(new Foo("x"));
    assertThat(new FooEntity().deserializeDatamart(payload, Foo.class)).isEqualTo(new Foo("x"));
  }

  @Test
  public void noPayload() {
    Assertions.assertThrows(
        ResourceExceptions.InvalidDatamartPayload.class,
        () -> new FooEntity().deserializeDatamart(null, String.class));
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  private static final class Foo {
    String bar;
  }

  @Value
  private static final class FooEntity implements DatamartEntity {
    String cdwId = "unused";
  }
}
