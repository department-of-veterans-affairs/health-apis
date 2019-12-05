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

public class LocationDstu2TransformerTest {
  @Test
  public void address() {
    assertThat(LocationDstu2Transformer.address(null)).isNull();
    assertThat(
            LocationDstu2Transformer.address(
                DatamartLocation.Address.builder().line1(" ").city("x").build()))
        .isNull();
    assertThat(
            LocationDstu2Transformer.address(
                DatamartLocation.Address.builder().city(" ").state(" ").postalCode(" ").build()))
        .isNull();
    assertThat(
            LocationDstu2Transformer.address(
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
  public void phsyicalType() {
    assertThat(LocationDstu2Transformer.physicalType(Optional.empty())).isNull();
    assertThat(LocationDstu2Transformer.physicalType(Optional.of(" "))).isNull();
    assertThat(LocationDstu2Transformer.physicalType(Optional.of("x")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(asList(Coding.builder().display("x").build()))
                .build());
  }

  @Test
  public void status() {
    assertThat(LocationDstu2Transformer.status(null)).isNull();
    assertThat(LocationDstu2Transformer.status(DatamartLocation.Status.active))
        .isEqualTo(Location.Status.active);
    assertThat(LocationDstu2Transformer.status(DatamartLocation.Status.inactive))
        .isEqualTo(Location.Status.inactive);
  }

  @Test
  public void telecoms() {
    assertThat(LocationDstu2Transformer.telecoms(" ")).isNull();
    assertThat(LocationDstu2Transformer.telecoms("x"))
        .isEqualTo(
            asList(
                ContactPoint.builder()
                    .system(ContactPoint.ContactPointSystem.phone)
                    .value("x")
                    .build()));
  }

  @Test
  public void type() {
    assertThat(LocationDstu2Transformer.type(Optional.empty())).isNull();
    assertThat(LocationDstu2Transformer.type(Optional.of(" "))).isNull();
    assertThat(LocationDstu2Transformer.type(Optional.of("x")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(asList(Coding.builder().display("x").build()))
                .build());
  }
}
