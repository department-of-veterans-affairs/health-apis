package gov.va.api.health.dataquery.service.controller.practitioner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.Test;

public class DatamartPractitionerTransformerTest {

  @Test
  public void address() {
    assertThat(DatamartPractitionerTransformer.address(null)).isNull();
    assertThat(
            DatamartPractitionerTransformer.address(
                DatamartPractitioner.Address.builder()
                    .city(" ")
                    .state(" ")
                    .postalCode(" ")
                    .build()))
        .isNull();
    assertThat(
            DatamartPractitionerTransformer.address(
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
  public void birthDate() {
    assertThat(DatamartPractitionerTransformer.birthDate(Optional.empty())).isNull();
    assertThat(DatamartPractitionerTransformer.birthDate(Optional.of(LocalDate.of(1990, 12, 12))))
        .isEqualTo("1990-12-12");
  }

  @Test
  public void gender() {
    DatamartPractitionerTransformer transformer = DatamartPractitionerTransformer.builder().build();
    assertThat(transformer.gender(null)).isNull();
    assertThat(transformer.gender(DatamartPractitioner.Gender.male))
        .isEqualTo(Practitioner.Gender.male);
    assertThat(transformer.gender(DatamartPractitioner.Gender.female))
        .isEqualTo(Practitioner.Gender.female);
  }

  @Test
  public void name() {
    assertThat(DatamartPractitionerTransformer.name(null)).isNull();

    assertThat(
            DatamartPractitionerTransformer.name(
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
  public void practitioner() {
    assertThat(tx(DatamartPractitionerSamples.Datamart.create().practitioner()).toFhir())
        .isEqualTo(DatamartPractitionerSamples.Datamart.Fhir.create().practitioner());
  }

  DatamartPractitionerTransformer tx(DatamartPractitioner dm) {
    return DatamartPractitionerTransformer.builder().datamart(dm).build();
  }
}
