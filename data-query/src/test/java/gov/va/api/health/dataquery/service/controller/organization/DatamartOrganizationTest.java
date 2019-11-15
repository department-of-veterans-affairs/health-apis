package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Arrays.asList;
import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization.Telecom;
import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization.Address;
import lombok.SneakyThrows;

public class DatamartOrganizationTest {
  @SneakyThrows
  private void assertReadable(String json) {
    DatamartOrganization dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartOrganization.class);
    assertThat(dm).isEqualTo(sample());
  }

  private static DatamartOrganization sample() {
    return DatamartOrganization.builder()
        .cdwId("561596:I")
        .stationIdentifier(Optional.of("442"))
        .npi(Optional.of("1205983228"))
        .providerId(Optional.of("0040000000000"))
        .ediId(Optional.of("36273"))
        .agencyId(Optional.of("other"))
        .active(true)
        .type(
            Optional.of(
                DatamartCoding.builder()
                    .system(Optional.of("institution"))
                    .code(Optional.of("CBOC"))
                    .display(Optional.of("COMMUNITY BASED OUTPATIENT CLINIC"))
                    .build()))
        .name("NEW AMSTERDAM CBOC")
        .telecom(
            asList(
                Telecom.builder().system(Telecom.System.phone).value("800 555-7710").build(),
                Telecom.builder().system(Telecom.System.phone).value("800 555-7720").build(),
                Telecom.builder().system(Telecom.System.phone).value("800-555-7730").build()))
        .address(
            Address.builder()
                .line1("10 MONROE AVE, SUITE 6B")
                .line2("PO BOX 4160")
                .city("NEW AMSTERDAM")
                .state("OH")
                .postalCode("44444-4160")
                .build())
        .partOf(
            Optional.of(
                DatamartReference.builder()
                    .reference(Optional.of("568060:I"))
                    .display(Optional.of("NEW AMSTERDAM VAMC"))
                    .build()))
        .build();
  }

  @Test
  public void unmarshalSample() {
    assertReadable("datamart-organization.json");
  }
}
