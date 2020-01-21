package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.resources.Location;
import java.util.Optional;
import org.junit.Test;

public class Stu3LocationTransformerTest {
  @Test
  public void address() {
    assertThat(Stu3LocationTransformer.address(null)).isNull();
    assertThat(
            Stu3LocationTransformer.address(
                DatamartLocation.Address.builder()
                    .line1(" ")
                    .city(" ")
                    .state(" ")
                    .postalCode(" ")
                    .build()))
        .isNull();

    assertThat(
            Stu3LocationTransformer.address(DatamartLocation.Address.builder().line1("w").build()))
        .isEqualTo(Location.LocationAddress.builder().line(asList("w")).text("w").build());
    assertThat(
            Stu3LocationTransformer.address(DatamartLocation.Address.builder().city("x").build()))
        .isEqualTo(Location.LocationAddress.builder().city("x").text("x").build());
    assertThat(
            Stu3LocationTransformer.address(DatamartLocation.Address.builder().state("y").build()))
        .isEqualTo(Location.LocationAddress.builder().state("y").text("y").build());
    assertThat(
            Stu3LocationTransformer.address(
                DatamartLocation.Address.builder().postalCode("z").build()))
        .isEqualTo(Location.LocationAddress.builder().postalCode("z").text("z").build());

    assertThat(
            Stu3LocationTransformer.address(
                DatamartLocation.Address.builder().line1("w").postalCode("z").build()))
        .isEqualTo(
            Location.LocationAddress.builder()
                .line(asList("w"))
                .postalCode("z")
                .text("w z")
                .build());

    assertThat(
            Stu3LocationTransformer.address(
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
    assertThat(Stu3LocationTransformer.physicalType(Optional.empty())).isNull();
    assertThat(Stu3LocationTransformer.physicalType(Optional.of(" "))).isNull();
    assertThat(Stu3LocationTransformer.physicalType(Optional.of("x")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(asList(Coding.builder().display("x").build()))
                .build());
  }

  @Test
  public void status() {
    assertThat(Stu3LocationTransformer.status(null)).isNull();
    assertThat(Stu3LocationTransformer.status(DatamartLocation.Status.active))
        .isEqualTo(Location.Status.active);
    assertThat(Stu3LocationTransformer.status(DatamartLocation.Status.inactive))
        .isEqualTo(Location.Status.inactive);
  }

  @Test
  public void telecoms() {
    assertThat(Stu3LocationTransformer.telecoms(" ")).isNull();
    assertThat(Stu3LocationTransformer.telecoms("x"))
        .isEqualTo(
            asList(
                ContactPoint.builder()
                    .system(ContactPoint.ContactPointSystem.phone)
                    .value("x")
                    .build()));
  }

  @Test
  public void type() {
    assertThat(Stu3LocationTransformer.type(Optional.empty())).isNull();
    assertThat(Stu3LocationTransformer.type(Optional.of(" "))).isNull();
    assertThat(Stu3LocationTransformer.type(Optional.of("x")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(asList(Coding.builder().display("x").build()))
                .build());
  }
}
