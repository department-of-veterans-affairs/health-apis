package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.util.Optional;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
final class DatamartLocationSamples {
  DatamartLocation location(String id, String organizationId) {
    return DatamartLocation.builder()
        .cdwId(id)
        .status(DatamartLocation.Status.active)
        .name("TEM MH PSO TRS IND93EH")
        .description(Optional.of("BLDG 146, RM W02"))
        .type(Optional.of("PSYCHIATRY CLINIC"))
        .telecom("254-743-2867")
        .address(
            DatamartLocation.Address.builder()
                .line1("1901 VETERANS MEMORIAL DRIVE")
                .city("TEMPLE")
                .state("TEXAS")
                .postalCode("76504")
                .build())
        .physicalType(Optional.of("BLDG 146, RM W02"))
        .managingOrganization(
            DatamartReference.builder()
                .reference(Optional.of(organizationId))
                .display(Optional.of("OLIN E. TEAGUE VET CENTER"))
                .build())
        .build();
  }
}
