package gov.va.api.health.dataquery.api.resources;

import static gov.va.api.health.dataquery.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.dataquery.api.ExactlyOneOfExtensionVerifier;
import gov.va.api.health.dataquery.api.ExactlyOneOfVerifier;
import gov.va.api.health.dataquery.api.ZeroOrOneOfVerifier;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dataquery.api.resources.Procedure.Bundle;
import gov.va.api.health.dataquery.api.resources.Procedure.Entry;
import gov.va.api.health.dataquery.api.samples.SampleProcedures;
import java.util.Collections;
import org.junit.Test;

public class ProcedureTest {
  private final SampleProcedures data = SampleProcedures.get();

  @Test
  public void bundlerCanBuildProcedureBundles() {
    Entry entry =
        Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://procedure.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(LinkRelation.self)
                        .url(("http://procedure.com/1"))
                        .build()))
            .resource(data.procedure())
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
                        .url(("http://procedure.com/2"))
                        .build()))
            .type(BundleType.searchset)
            .build();

    assertRoundTrip(bundle);
  }

  @Test
  public void patient() {
    assertRoundTrip(data.procedure());
  }

  @Test
  public void relatedFields() {
    ZeroOrOneOfVerifier.builder()
        .sample(data.procedure())
        .fieldPrefix("reason")
        .omission("reasonNotPerformed")
        .build()
        .verify();
    ExactlyOneOfExtensionVerifier.builder()
        .sample(data.procedure())
        .field("status")
        .build()
        .verify();
    ExactlyOneOfVerifier.builder()
        .sample(data.procedure())
        .fieldPrefix("performed")
        .build()
        .verify();
  }
}
