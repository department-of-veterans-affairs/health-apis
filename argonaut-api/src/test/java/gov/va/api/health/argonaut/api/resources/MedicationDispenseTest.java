package gov.va.api.health.argonaut.api.resources;

import static gov.va.api.health.argonaut.api.RoundTrip.assertRoundTrip;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.ExactlyOneOfVerifier;
import gov.va.api.health.argonaut.api.ZeroOrOneOfVerifier;
import gov.va.api.health.argonaut.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.argonaut.api.resources.MedicationDispense.Bundle;
import gov.va.api.health.argonaut.api.resources.MedicationDispense.Entry;
import gov.va.api.health.argonaut.api.samples.SampleMedicationDispenses;
import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.junit.Test;

public class MedicationDispenseTest {
  private final SampleMedicationDispenses data = SampleMedicationDispenses.get();

  @Test
  public void bundlerCanBuildMedicationDispenseBundles() {
    Entry entry =
        Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://medicationdispense.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(LinkRelation.self)
                        .url("http://medicationdispense/123")
                        .build()))
            .resource(data.medicationDispense())
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
                        .url("https://medicationdispense?patient=456")
                        .build()))
            .type(BundleType.searchset)
            .build();
    assertRoundTrip(bundle);
  }

  @Test
  public void medicationDispense() {
    assertRoundTrip(data.medicationDispense());
  }

  @Test
  public void relatedFields() {
    ExactlyOneOfVerifier.builder()
        .sample(data.medicationDispense())
        .fieldPrefix("medication")
        .build();
    ZeroOrOneOfVerifier.builder().sample(data.medicationDispense()).fieldPrefix("rate").build();
    ZeroOrOneOfVerifier.builder().sample(data.medicationDispense()).fieldPrefix("asNeeded").build();
    ZeroOrOneOfVerifier.builder().sample(data.medicationDispense()).fieldPrefix("site").build();
    ZeroOrOneOfVerifier.builder().sample(data.medicationDispense()).fieldPrefix("dose").build();
  }

  @Test
  public void validationFailsGivenEarlyHandedOver() {
    assertThat(
            violationsOf(
                data.medicationDispense()
                    .whenPrepared("2019-02-01T04:00:00Z")
                    .whenHandedOver("2019-01-01T13:00:00Z")))
        .isNotEmpty();
  }

  @Test
  public void validationIgnoresMissingWhenPrepared() {
    assertThat(violationsOf(data.medicationDispense().whenPrepared(null))).isEmpty();
  }

  @Test
  public void validationPassesGivenGoodPreparedAndHandedOver() {
    assertThat(
            violationsOf(
                data.medicationDispense()
                    .whenPrepared("2019-01-01T04:00:00Z")
                    .whenHandedOver("2019-01-02T04:00:00Z")))
        .isEmpty();
  }

  @Test
  public void validationReturnsOnlyRegexViolationForBadDate() {
    Set<ConstraintViolation<MedicationDispense>> violations =
        violationsOf(data.medicationDispense().whenPrepared("not-a-date"));
    assertThat(violations).isNotEmpty();
    // Checking to make sure no message about the dates being in the wrong order comes back
    assertThat(violations)
        .filteredOn(violation -> violation.getMessage().contains("before"))
        .isEmpty();
  }

  private <T> Set<ConstraintViolation<T>> violationsOf(T object) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator().validate(object);
  }
}
