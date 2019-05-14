package gov.va.api.health.sentinel.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.crawler.Result.Outcome;
import java.time.Instant;
import org.junit.Test;
import org.mockito.Mockito;

public class SummarizingWithIgnoreListResultCollectorTest {

  @Test
  public void addFailResultIncrementsFailures() {
    ResultCollector resultCollector = Mockito.mock(ResultCollector.class);
    SummarizingWithIgnoreListResultCollector results =
        SummarizingWithIgnoreListResultCollector.wrap(resultCollector);
    results.useFilter("foo,bar");

    Result badnessResult =
        Result.builder()
            .query(
                "https://somepath.va.gov/services/argonaut/v0/Resource/z8z848z3-35zz-5zz-93zz-z4z8731z1z11")
            .outcome(Outcome.INVALID_PAYLOAD)
            .timestamp(Instant.ofEpochMilli(158994000000L))
            .build();
    results.add(badnessResult);
    assertThat(results.failures()).isOne();
  }

  @Test
  public void addFailureResultMatchingAnIgnoreListFilterIncrementsIgnores() {
    ResultCollector resultCollector = Mockito.mock(ResultCollector.class);
    SummarizingWithIgnoreListResultCollector results =
        SummarizingWithIgnoreListResultCollector.wrap(resultCollector);
    results.useFilter("foo,bar,Resource/z8z848z3-35zz-5zz-93zz-z4z8731z1z11");

    Result badnessResult =
        Result.builder()
            .query(
                "https://somepath.va.gov/services/argonaut/v0/Resource/z8z848z3-35zz-5zz-93zz-z4z8731z1z11")
            .outcome(Outcome.INVALID_PAYLOAD)
            .timestamp(Instant.ofEpochMilli(158994000000L))
            .build();
    results.add(badnessResult);
    assertThat(results.failures()).isZero();
    assertThat(results.ignoredFailures()).isOne();
  }

  @Test
  public void addOKResultIncrementsTotals() {
    ResultCollector resultCollector = Mockito.mock(ResultCollector.class);
    SummarizingWithIgnoreListResultCollector results =
        SummarizingWithIgnoreListResultCollector.wrap(resultCollector); // .ignoreList("foo");
    Result okResult =
        Result.builder()
            .query(
                "https://somepath.va.gov/services/argonaut/v0/Resource/z8z848z3-35zz-5zz-93zz-z4z8731z1z11")
            .outcome(Outcome.OK)
            .timestamp(Instant.ofEpochMilli(158994000000L))
            .build();
    results.add(okResult);
    assertThat(results.failures()).isZero();
    assertThat(results.ignoredFailures()).isZero();
  }
}
