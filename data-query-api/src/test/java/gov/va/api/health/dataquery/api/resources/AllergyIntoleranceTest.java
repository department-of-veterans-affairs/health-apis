package gov.va.api.health.dataquery.api.resources;

import static gov.va.api.health.dataquery.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Bundle;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Entry;
import gov.va.api.health.dataquery.api.samples.SampleAllergyIntolerances;
import java.util.Collections;
import org.junit.Test;

public class AllergyIntoleranceTest {

  private final SampleAllergyIntolerances data = SampleAllergyIntolerances.get();

  @Test
  public void allergyIntolerance() {
    assertRoundTrip(data.allergyIntolerance());
  }

  @Test
  public void bundlerCanBuildAllergyIntoleranceBundles() {
    Entry entry =
        Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://AllergyIntolerance.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(LinkRelation.self)
                        .url(("http://AllergyIntolerance.com/1"))
                        .build()))
            .resource(data.allergyIntolerance())
            .search(data.search())
            .request(data.request())
            .response(data.response())
            .build();

    Bundle bundle =
        Bundle.builder()
            .entry(Collections.singletonList(entry))
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(LinkRelation.self)
                        .url(("http://AllergyIntolerance.com/2"))
                        .build()))
            .type(BundleType.searchset)
            .build();

    assertRoundTrip(bundle);
  }
}
