package gov.va.api.health.dataquery.api.resources;

import static gov.va.api.health.dataquery.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.dataquery.api.bundle.AbstractBundle;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.samples.SampleMedications;
import java.util.Collections;
import org.junit.Test;

public class MedicationTest {
  private final SampleMedications data = SampleMedications.get();

  @Test
  public void bundlerCanBuildMedicationBundles() {
    Medication.Entry entry =
        Medication.Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://medication.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(BundleLink.LinkRelation.self)
                        .url(("http://medication.com/1"))
                        .build()))
            .resource(data.medication())
            .search(data.search())
            .request(data.request())
            .response(data.response())
            .build();

    Medication.Bundle bundle =
        Medication.Bundle.builder()
            .entry(Collections.singletonList(entry))
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(BundleLink.LinkRelation.self)
                        .url(("http://medication.com/2"))
                        .build()))
            .type(AbstractBundle.BundleType.searchset)
            .build();

    assertRoundTrip(bundle);
  }

  @Test
  public void medication() {
    assertRoundTrip(data.medication());
  }
}
