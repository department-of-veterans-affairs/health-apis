package gov.va.api.health.dataquery.service.controller.practitioner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Practitioner;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4PractitionerTransformerTest {
  @Test
  public void address() {
    assertThat(R4PractitionerTransformer.address(null)).isNull();
    assertThat(
            R4PractitionerTransformer.address(
                DatamartPractitioner.Address.builder()
                    .city(" ")
                    .state(" ")
                    .postalCode(" ")
                    .build()))
        .isNull();
    assertThat(
            R4PractitionerTransformer.address(
                DatamartPractitioner.Address.builder()
                    .line1("w")
                    .city("x")
                    .state("y")
                    .postalCode("z")
                    .build()))
        .isEqualTo(
            Address.builder().line(asList("w")).city("x").state("y").postalCode("z").build());
  }

  @Test
  public void empty() {
    assertThat(
            R4PractitionerTransformer.builder()
                .datamart(DatamartPractitioner.builder().build())
                .build()
                .toFhir())
        .isEqualTo(
            Practitioner.builder()
                .resourceType("Practitioner")
                .identifier(
                    List.of(
                        Identifier.builder()
                            .system("http://hl7.org/fhir/sid/us-npi")
                            .value("Unknown")
                            .build()))
                .build());
  }

  @Test
  public void gender() {
    R4PractitionerTransformer transformer = R4PractitionerTransformer.builder().build();
    assertThat(transformer.gender(null)).isNull();
    assertThat(transformer.gender(DatamartPractitioner.Gender.male))
        .isEqualTo(Practitioner.GenderCode.male);
    assertThat(transformer.gender(DatamartPractitioner.Gender.female))
        .isEqualTo(Practitioner.GenderCode.female);
  }

  @Test
  public void name() {
    assertThat(R4PractitionerTransformer.name(null)).isNull();
    assertThat(
            R4PractitionerTransformer.name(
                DatamartPractitioner.Name.builder()
                    .family("family")
                    .given("given")
                    .suffix(Optional.of("suffix"))
                    .prefix(Optional.of("prefix"))
                    .build()))
        .isEqualTo(
            List.of(
                HumanName.builder()
                    .family("family")
                    .given(asList("given"))
                    .prefix(asList("prefix"))
                    .suffix(asList("suffix"))
                    .build()));
  }

  @Test
  public void telecom() {
    assertThat(R4PractitionerTransformer.telecom(null)).isNull();
    assertThat(
            R4PractitionerTransformer.telecom(
                DatamartPractitioner.Telecom.builder().system(null).use(null).value(" ").build()))
        .isNull();
    assertThat(
            R4PractitionerTransformer.telecom(
                DatamartPractitioner.Telecom.builder()
                    .system(DatamartPractitioner.Telecom.System.phone)
                    .use(DatamartPractitioner.Telecom.Use.mobile)
                    .value("123-456-1234")
                    .build()))
        .isEqualTo(
            ContactPoint.builder()
                .system(ContactPoint.ContactPointSystem.phone)
                .use(ContactPoint.ContactPointUse.mobile)
                .value("123-456-1234")
                .build());
  }

  @Test
  public void toFhir() {
    assertThat(
            R4PractitionerTransformer.builder()
                .datamart(PractitionerSamples.Datamart.create().practitioner())
                .build()
                .toFhir())
        .isEqualTo(PractitionerSamples.R4.create().practitioner());
  }
}
