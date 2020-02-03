package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.junit.Test;

public final class DatamartEntityTest {
  @Test(expected = ResourceExceptions.InvalidDatamartPayload.class)
  public void badPayload() {
    new FooEntity().deserializeDatamart("{wat]", Foo.class);
  }

  @Test
  public void deserializeDatamart() {
    assertThat(new FooEntity().deserializeDatamart("{\"bar\":\"x\"}", Foo.class))
        .isEqualTo(new Foo("x"));
  }

  @Test(expected = ResourceExceptions.InvalidDatamartPayload.class)
  public void noPayload() {
    new FooEntity().deserializeDatamart(null, String.class);
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
