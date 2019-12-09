package gov.va.api.health.dataquery.service.controller.practitioner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.Test;

public class Dstu2PractitionerTransformerTest {

  @Test
  public void address() {
    assertThat(Dstu2PractitionerTransformer.address(null)).isNull();
    assertThat(
            Dstu2PractitionerTransformer.address(
                Dstu2Practitioner.Address.builder().city(" ").state(" ").postalCode(" ").build()))
        .isNull();
    assertThat(
            Dstu2PractitionerTransformer.address(
                Dstu2Practitioner.Address.builder()
                    .line1("w")
                    .city("x")
                    .state("y")
                    .postalCode("z")
                    .build()))
        .isEqualTo(
            Address.builder().line(asList("w")).city("x").state("y").postalCode("z").build());
  }

  @Test
  public void birthDate() {
    assertThat(Dstu2PractitionerTransformer.birthDate(Optional.empty())).isNull();
    assertThat(Dstu2PractitionerTransformer.birthDate(Optional.of(LocalDate.of(1990, 12, 12))))
        .isEqualTo("1990-12-12");
  }

  @Test
  public void gender() {
    Dstu2PractitionerTransformer transformer = Dstu2PractitionerTransformer.builder().build();
    assertThat(transformer.gender(null)).isNull();
    assertThat(transformer.gender(Dstu2Practitioner.Gender.male))
        .isEqualTo(Practitioner.Gender.male);
    assertThat(transformer.gender(Dstu2Practitioner.Gender.female))
        .isEqualTo(Practitioner.Gender.female);
  }

  @Test
  public void name() {
    assertThat(Dstu2PractitionerTransformer.name(null)).isNull();
    assertThat(
            Dstu2PractitionerTransformer.name(
                Dstu2Practitioner.Name.builder()
                    .family("family")
                    .given("given")
                    .suffix(Optional.of("suffix"))
                    .prefix(Optional.of("prefix"))
                    .build()))
        .isEqualTo(
            HumanName.builder()
                .family(asList("family"))
                .given(asList("given"))
                .prefix(asList("prefix"))
                .suffix(asList("suffix"))
                .build());
  }

  @Test
  public void nullChecks() {
    assertThat(Dstu2PractitionerTransformer.healthcareServices(Optional.empty())).isNull();
    assertThat(Dstu2PractitionerTransformer.roleCoding(null)).isNull();
    assertThat(Dstu2PractitionerTransformer.role(null)).isNull();
    assertThat(Dstu2PractitionerTransformer.telecom(null)).isNull();
    // .isEqualTo("1990-12-12");
  }

  @Test
  public void practitioner() {
    assertThat(tx(Dstu2PractitionerSamples.Datamart.create().practitioner()).toFhir())
        .isEqualTo(Dstu2PractitionerSamples.Datamart.Dstu2.create().practitioner());
  }

  Dstu2PractitionerTransformer tx(Dstu2Practitioner dm) {
    return Dstu2PractitionerTransformer.builder().datamart(dm).build();
  }
}
