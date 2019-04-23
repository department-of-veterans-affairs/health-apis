package gov.va.api.health.dataquery.api.resources;

import static gov.va.api.health.dataquery.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.dataquery.api.ExactlyOneOfVerifier;
import gov.va.api.health.dataquery.api.ZeroOrOneOfVerifier;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.Bundle;
import gov.va.api.health.dataquery.api.resources.MedicationOrder.Entry;
import gov.va.api.health.dataquery.api.samples.SampleMedicationOrders;
import java.util.Collections;
import org.junit.Test;

public class MedicationOrderTest {
  private final SampleMedicationOrders data = SampleMedicationOrders.get();

  @Test
  public void bundlerCanBuildMedicationOrderBundles() {
    Entry entry =
        Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://medicationorder.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(LinkRelation.self)
                        .url(("http://medicationorder/1"))
                        .build()))
            .resource(data.medicationOrder())
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
                        .url(("http://medicationorder/2"))
                        .build()))
            .type(BundleType.searchset)
            .build();

    assertRoundTrip(bundle);

    AbstractEntry.Search.builder().build().id();
  }

  @Test
  public void medicationOrder() {
    assertRoundTrip(data.medicationOrder());
  }

  @Test
  public void relatedFields() {
    ExactlyOneOfVerifier.builder()
        .sample(data.medicationOrder())
        .fieldPrefix("medication")
        .build()
        .verify();
    ZeroOrOneOfVerifier.builder()
        .sample(data.medicationOrder())
        .fieldPrefix("reason")
        .omission("reasonEnded")
        .build()
        .verify();
    ZeroOrOneOfVerifier.builder()
        .sample(data.medicationOrder().dosageInstruction().get(0))
        .fieldPrefix("asNeeded")
        .build()
        .verify();
    ZeroOrOneOfVerifier.builder()
        .sample(data.medicationOrder().dosageInstruction().get(0))
        .fieldPrefix("site")
        .build()
        .verify();
    ZeroOrOneOfVerifier.builder()
        .sample(data.medicationOrder().dosageInstruction().get(0))
        .fieldPrefix("dose")
        .build()
        .verify();
    ZeroOrOneOfVerifier.builder()
        .sample(data.medicationOrder().dosageInstruction().get(0))
        .fieldPrefix("rate")
        .build()
        .verify();
    ZeroOrOneOfVerifier.builder()
        .sample(data.medicationOrder().dispenseRequest())
        .fieldPrefix("medication")
        .build()
        .verify();
  }
}
