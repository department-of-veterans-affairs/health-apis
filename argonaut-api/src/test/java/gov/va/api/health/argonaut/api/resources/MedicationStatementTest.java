package gov.va.api.health.argonaut.api.resources;

import static gov.va.api.health.argonaut.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.argonaut.api.ExactlyOneOfVerifier;
import gov.va.api.health.argonaut.api.ZeroOrOneOfVerifier;
import gov.va.api.health.argonaut.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Bundle;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Entry;
import gov.va.api.health.argonaut.api.samples.SampleMedicationStatements;
import java.util.Collections;
import org.junit.Test;

public class MedicationStatementTest {

  private final SampleMedicationStatements data = SampleMedicationStatements.get();

  @Test
  public void bundlerCanBuildMedicationStatementBundles() {
    Entry entry =
        Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://medicationstatement.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(LinkRelation.self)
                        .url(("http://medicationstatement.com/1"))
                        .build()))
            .resource(data.medicationStatement())
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
                        .url(("http://medicationstatement.com/2"))
                        .build()))
            .type(BundleType.searchset)
            .build();

    assertRoundTrip(bundle);
  }

  @Test
  public void medicationStatement() {
    assertRoundTrip(data.medicationStatement());
    assertRoundTrip(data.medicationStatementWithAlternateValues());
  }

  @Test
  public void relatedGroups() {
    ZeroOrOneOfVerifier.builder()
        .sample(data.medicationStatement())
        .fieldPrefix("reasonForUse")
        .build()
        .verify();
    ZeroOrOneOfVerifier.builder()
        .sample(data.medicationStatement())
        .fieldPrefix("effective")
        .build()
        .verify();
    ExactlyOneOfVerifier.builder()
        .sample(data.medicationStatement())
        .fieldPrefix("medication")
        .build()
        .verify();
  }
}
