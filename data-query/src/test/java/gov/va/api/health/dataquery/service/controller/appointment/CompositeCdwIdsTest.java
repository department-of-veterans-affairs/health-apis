package gov.va.api.health.dataquery.service.controller.appointment;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import gov.va.api.health.dataquery.service.controller.CompositeCdwIds;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

public class CompositeCdwIdsTest {
  @Test
  void requireCompositeIdFormatString() {
    assertThat(CompositeCdwIds.requireCompositeIdStringFormat("123:A"))
        .isEqualTo(
            CompositeCdwId.builder()
                .cdwIdNumber(new BigInteger("123"))
                .cdwIdResourceCode('A')
                .build());
    assertThatExceptionOfType(ResourceExceptions.NotFound.class)
        .isThrownBy(() -> CompositeCdwIds.requireCompositeIdStringFormat("12:"));
    assertThatExceptionOfType(ResourceExceptions.NotFound.class)
        .isThrownBy(() -> CompositeCdwIds.requireCompositeIdStringFormat(":"));
    assertThatExceptionOfType(ResourceExceptions.NotFound.class)
        .isThrownBy(() -> CompositeCdwIds.requireCompositeIdStringFormat("12"));
  }
}
