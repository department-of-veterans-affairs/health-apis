package gov.va.api.health.dataquery.api;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.DataAbsentReason.Reason;
import lombok.SneakyThrows;
import org.junit.Test;

public class DataAbsentReasonTest {
  @Test
  @SneakyThrows
  public void reasonText() {
    assertThat(DataAbsentReason.of(Reason.not_asked).extension().size()).isEqualTo(1);
    assertThat(DataAbsentReason.of(Reason.not_asked).extension().get(0).valueCode())
        .isEqualTo("not-asked");
    assertThat(DataAbsentReason.of(Reason.unsupported).extension().get(0).valueCode())
        .isEqualTo("unsupported");
  }
}
