package gov.va.api.health.dataquery.service.controller.practitioner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.practitionerrole.DatamartPractitionerRole;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class Dstu2PractitionerTransformerTest {
  static Dstu2PractitionerTransformer tx(
      DatamartPractitioner dm, List<DatamartPractitionerRole> dmRoles) {
    return Dstu2PractitionerTransformer.builder().datamart(dm).datamartRoles(dmRoles).build();
  }

  @Test
  void address() {
    assertThat(Dstu2PractitionerTransformer.address(null)).isNull();
    assertThat(
            Dstu2PractitionerTransformer.address(
                DatamartPractitioner.Address.builder()
                    .city(" ")
                    .state(" ")
                    .postalCode(" ")
                    .build()))
        .isNull();
    assertThat(
            Dstu2PractitionerTransformer.address(
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
  void birthDate() {
    assertThat(Dstu2PractitionerTransformer.birthDate(Optional.empty())).isNull();
    assertThat(Dstu2PractitionerTransformer.birthDate(Optional.of(LocalDate.of(1990, 12, 12))))
        .isEqualTo("1990-12-12");
  }

  @Test
  void empty() {
    assertThat(
            Dstu2PractitionerTransformer.builder()
                .datamart(DatamartPractitioner.builder().build())
                .datamartRoles(List.of())
                .build()
                .toFhir())
        .isEqualTo(Practitioner.builder().build());
  }

  @Test
  void gender() {
    Dstu2PractitionerTransformer transformer = Dstu2PractitionerTransformer.builder().build();
    assertThat(transformer.gender(null)).isNull();
    assertThat(transformer.gender(DatamartPractitioner.Gender.male))
        .isEqualTo(Practitioner.Gender.male);
    assertThat(transformer.gender(DatamartPractitioner.Gender.female))
        .isEqualTo(Practitioner.Gender.female);
  }

  @Test
  void name() {
    assertThat(Dstu2PractitionerTransformer.name(null)).isNull();
    assertThat(
            Dstu2PractitionerTransformer.name(
                DatamartPractitioner.Name.builder()
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
  void nullChecks() {
    assertThat(Dstu2PractitionerTransformer.healthcareServices(Optional.empty())).isNull();
    assertThat(Dstu2PractitionerTransformer.telecom(null)).isNull();
    assertThat(Dstu2PractitionerTransformer.practitionerRole(null, null)).isNull();
  }

  @Test
  void practitioner() {
    assertThat(
            tx(PractitionerSamples.Datamart.create().practitioner("111:S", "222:I", "333:L"))
                .toFhir())
        .isEqualTo(PractitionerSamples.Dstu2.create().practitioner("111:S", "222:I", "333:L"));
  }
}
