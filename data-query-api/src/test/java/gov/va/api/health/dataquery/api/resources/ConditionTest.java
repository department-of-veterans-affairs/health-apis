package gov.va.api.health.dataquery.api.resources;

import static gov.va.api.health.dataquery.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.dataquery.api.ZeroOrOneOfVerifier;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.resources.Condition.Bundle;
import gov.va.api.health.dataquery.api.samples.SampleConditions;
import java.util.Collections;
import org.junit.Test;

public class ConditionTest {

  private final SampleConditions data = SampleConditions.get();

  @Test
  public void bundlerCanBuildConditionBundles() {
    Condition.Entry entry =
        Condition.Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://condition.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(BundleLink.LinkRelation.self)
                        .url(("http://condition/1"))
                        .build()))
            .resource(data.condition())
            .search(data.search())
            .request(data.request())
            .response(data.response())
            .build();

    Bundle bundle =
        Condition.Bundle.builder()
            .entry(Collections.singletonList(entry))
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(BundleLink.LinkRelation.self)
                        .url(("http://condition.com/2"))
                        .build()))
            .type(AbstractBundle.BundleType.searchset)
            .build();

    assertRoundTrip(bundle);
  }

  @Test
  public void condition() {
    assertRoundTrip(data.condition());
  }

  @Test
  public void relatedGroups() {
    ZeroOrOneOfVerifier.builder().sample(data.condition()).fieldPrefix("onset").build().verify();
    ZeroOrOneOfVerifier.builder()
        .sample(data.condition())
        .fieldPrefix("abatement")
        .build()
        .verify();
  }
}
