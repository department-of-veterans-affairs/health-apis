package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.resources.Location;
import java.util.Optional;
import org.junit.Test;

public class LocationStu3TransformerTest {
  @Test
  public void address() {
    assertThat(LocationStu3Transformer.address(null)).isNull();
    assertThat(
            LocationStu3Transformer.address(
                DatamartLocation.Address.builder()
                    .line1(" ")
                    .city(" ")
                    .state(" ")
                    .postalCode(" ")
                    .build()))
        .isNull();
    assertThat(
            LocationStu3Transformer.address(
                DatamartLocation.Address.builder()
                    .line1("w")
                    .city("x")
                    .state("y")
                    .postalCode("z")
                    .build()))
        .isEqualTo(
            Location.LocationAddress.builder()
                .line(asList("w"))
                .city("x")
                .state("y")
                .postalCode("z")
                .text("w x y z")
                .build());
  }

  @Test
  public void phsyicalType() {
    assertThat(LocationStu3Transformer.physicalType(Optional.empty())).isNull();
    assertThat(LocationStu3Transformer.physicalType(Optional.of(" "))).isNull();
    assertThat(LocationStu3Transformer.physicalType(Optional.of("x")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(asList(Coding.builder().display("x").build()))
                .build());
  }

  @Test
  public void status() {
    assertThat(LocationStu3Transformer.status(null)).isNull();
    assertThat(LocationStu3Transformer.status(DatamartLocation.Status.active))
        .isEqualTo(Location.Status.active);
    assertThat(LocationStu3Transformer.status(DatamartLocation.Status.inactive))
        .isEqualTo(Location.Status.inactive);
  }

  @Test
  public void telecoms() {
    assertThat(LocationStu3Transformer.telecoms(" ")).isNull();
    assertThat(LocationStu3Transformer.telecoms("x"))
        .isEqualTo(
            asList(
                ContactPoint.builder()
                    .system(ContactPoint.ContactPointSystem.phone)
                    .value("x")
                    .build()));
  }

  @Test
  public void type() {
    assertThat(LocationStu3Transformer.type(Optional.empty())).isNull();
    assertThat(LocationStu3Transformer.type(Optional.of(" "))).isNull();
    assertThat(LocationStu3Transformer.type(Optional.of("x")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(asList(Coding.builder().display("x").build()))
                .build());
  }
}
