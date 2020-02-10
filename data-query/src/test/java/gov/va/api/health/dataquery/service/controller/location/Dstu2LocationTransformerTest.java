package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.resources.Location;
import java.util.Optional;
import org.junit.Test;

public class Dstu2LocationTransformerTest {
  @Test
  public void address() {
    assertThat(Dstu2LocationTransformer.address(null)).isNull();
    assertThat(
            Dstu2LocationTransformer.address(
                DatamartLocation.Address.builder().line1(" ").city("x").build()))
        .isNull();
    assertThat(
            Dstu2LocationTransformer.address(
                DatamartLocation.Address.builder().city(" ").state(" ").postalCode(" ").build()))
        .isNull();
    assertThat(
            Dstu2LocationTransformer.address(
                DatamartLocation.Address.builder()
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
            Dstu2LocationTransformer.builder()
                .datamart(DatamartLocation.builder().build())
                .build()
                .toFhir())
        .isEqualTo(
            Location.builder().resourceType("Location").mode(Location.Mode.instance).build());
  }

  @Test
  public void phsyicalType() {
    assertThat(Dstu2LocationTransformer.physicalType(Optional.empty())).isNull();
    assertThat(Dstu2LocationTransformer.physicalType(Optional.of(" "))).isNull();
    assertThat(Dstu2LocationTransformer.physicalType(Optional.of("x")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(asList(Coding.builder().display("x").build()))
                .build());
  }

  @Test
  public void status() {
    assertThat(Dstu2LocationTransformer.status(null)).isNull();
    assertThat(Dstu2LocationTransformer.status(DatamartLocation.Status.active))
        .isEqualTo(Location.Status.active);
    assertThat(Dstu2LocationTransformer.status(DatamartLocation.Status.inactive))
        .isEqualTo(Location.Status.inactive);
  }

  @Test
  public void telecoms() {
    assertThat(Dstu2LocationTransformer.telecoms(" ")).isNull();
    assertThat(Dstu2LocationTransformer.telecoms("x"))
        .isEqualTo(
            asList(
                ContactPoint.builder()
                    .system(ContactPoint.ContactPointSystem.phone)
                    .value("x")
                    .build()));
  }

  @Test
  public void type() {
    assertThat(Dstu2LocationTransformer.type(Optional.empty())).isNull();
    assertThat(Dstu2LocationTransformer.type(Optional.of(" "))).isNull();
    assertThat(Dstu2LocationTransformer.type(Optional.of("x")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(asList(Coding.builder().display("x").build()))
                .build());
  }
}
