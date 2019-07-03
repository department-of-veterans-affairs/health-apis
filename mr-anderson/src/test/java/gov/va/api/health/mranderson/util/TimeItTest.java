package gov.va.api.health.mranderson.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TimeItTest {

  @Test
  public void logTime() {
    assertThat(TimeIt.logTime(() -> "foobar".substring(0, 3), "execution test")).isEqualTo("foo");
  }

  @Test
  public void logTimeAllNull() {
    assertThat((Object[]) TimeIt.logTime(null, null)).isNull();
  }

  @Test
  public void logTimeNullDescription() {
    assertThat(TimeIt.logTime(() -> "foobar".substring(0, 3), null)).isEqualTo("foo");
  }
}
