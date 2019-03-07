package gov.va.api.health.dataquery.api.resources;

import static gov.va.api.health.dataquery.api.RoundTrip.assertRoundTrip;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.ZeroOrOneOfVerifier;
import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dataquery.api.resources.Observation.Bundle;
import gov.va.api.health.dataquery.api.resources.Observation.Entry;
import gov.va.api.health.dataquery.api.samples.SampleObservations;
import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.junit.Test;

public class ObservationTest {

  private final SampleObservations data = SampleObservations.get();

  @Test
  public void bundlerCanBuildObservationBundles() {
    Entry entry =
        Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://observation.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(LinkRelation.self)
                        .url(("http://observation.com/1"))
                        .build()))
            .resource(data.observation())
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
                        .url(("http://observation.com/2"))
                        .build()))
            .type(BundleType.searchset)
            .build();

    assertRoundTrip(bundle);
  }

  @Test
  public void observation() {
    assertRoundTrip(data.observation());
  }

  @Test
  public void relatedGroups() {
    ZeroOrOneOfVerifier.builder()
        .sample(data.observation())
        .fieldPrefix("effective")
        .build()
        .verify();
    ZeroOrOneOfVerifier.builder().sample(data.observation()).fieldPrefix("value").build().verify();
  }

  @Test
  public void validationFailsGivenBadCategory() {
    assertThat(violationsOf(data.observation().category(data.codeableConcept()))).isNotEmpty();
  }

  @Test
  public void validationFailsGivenNoCategory() {
    assertThat(violationsOf(data.observation().category(null))).isNotEmpty();
  }

  @Test
  public void validationPassesGivenGoodCategory() {
    assertThat(
            violationsOf(
                data.observation()
                    .category()
                    .coding()
                    .get(0)
                    .code("http://hl7.org/fhir/observation-category")))
        .isEmpty();
  }

  private <T> Set<ConstraintViolation<T>> violationsOf(T object) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator().validate(object);
  }
}
