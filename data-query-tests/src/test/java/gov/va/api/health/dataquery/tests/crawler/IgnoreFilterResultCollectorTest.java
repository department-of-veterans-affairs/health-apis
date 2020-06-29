package gov.va.api.health.dataquery.tests.crawler;

import static gov.va.api.health.dataquery.tests.EnvironmentAssumptions.assumeLocal;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.crawler.Result.Outcome;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class IgnoreFilterResultCollectorTest {

  @Test
  public void addFailResultIncrementsFailures() {
    ResultCollector resultCollector = Mockito.mock(ResultCollector.class);
    IgnoreFilterResultCollector results = IgnoreFilterResultCollector.wrap(resultCollector);
    results.useFilter("foo,bar");
    Result badnessResult =
        Result.builder()
            .query(
                "https://somepath.va.gov/services/argonaut/v0/Resource/z8z848z3-35zz-5zz-93zz-z4z8731z1z11")
            .outcome(Outcome.INVALID_PAYLOAD)
            .timestamp(Instant.ofEpochMilli(158994000000L))
            .duration(Duration.ofMillis(10))
            .build();
    results.add(badnessResult);
    assertThat(results.failures()).isOne();
    assertThat(results.ignoredFailures()).isZero();
  }

  @Test
  public void addFailureResultMatchingAnIgnoreListFilterIncrementsIgnores() {
    ResultCollector resultCollector = Mockito.mock(ResultCollector.class);
    IgnoreFilterResultCollector results = IgnoreFilterResultCollector.wrap(resultCollector);
    results.useFilter("foo,bar,Resource/z8z848z3-35zz-5zz-93zz-z4z8731z1z11");
    Result badnessResult =
        Result.builder()
            .query(
                "https://somepath.va.gov/services/argonaut/v0/Resource/z8z848z3-35zz-5zz-93zz-z4z8731z1z11")
            .outcome(Outcome.INVALID_PAYLOAD)
            .timestamp(Instant.ofEpochMilli(158994000000L))
            .duration(Duration.ofMillis(10))
            .build();
    results.add(badnessResult);
    assertThat(results.failures()).isZero();
    assertThat(results.ignoredFailures()).isOne();
  }

  @Test
  public void addOKResult() {
    ResultCollector resultCollector = Mockito.mock(ResultCollector.class);
    IgnoreFilterResultCollector results = IgnoreFilterResultCollector.wrap(resultCollector);
    results.useFilter("");
    Result okResult =
        Result.builder()
            .query(
                "https://somepath.va.gov/services/argonaut/v0/Resource/z8z848z3-35zz-5zz-93zz-z4z8731z1z11")
            .outcome(Outcome.OK)
            .timestamp(Instant.ofEpochMilli(158994000000L))
            .duration(Duration.ofMillis(10))
            .build();
    results.add(okResult);
    assertThat(results.failures()).isZero();
    assertThat(results.ignoredFailures()).isZero();
  }
}
