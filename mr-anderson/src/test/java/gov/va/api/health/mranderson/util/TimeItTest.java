package gov.va.api.health.mranderson.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TimeItTest {

  @Test
  public void logTime() {
    assertThat(
            TimeIt.builder()
                .taskName("execution test")
                .build()
                .logTime(() -> "foobar".substring(0, 3)))
        .isEqualTo("foo");
  }

  @Test
  public void logTimeNullTask() {
    assertThat((Object[]) TimeIt.builder().build().logTime(null)).isNull();
  }
}
