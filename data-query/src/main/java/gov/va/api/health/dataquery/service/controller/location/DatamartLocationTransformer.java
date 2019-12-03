package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Arrays.asList;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.resources.Location;
import lombok.Builder;

@Builder
final class DatamartLocationTransformer {
  private final DatamartLocation datamart;

  /** Convert the datamart structure to FHIR compliant structure. */
  public Location toFhir() {
    return Location.builder()
        .resourceType("Location")
        .id(datamart.cdwId())
        .address(address(datamart.address()))
        .build();

    //    .description(source.getDescription())
    //    .managingOrganization(reference(source.getManagingOrganization()))
    //    .mode(mode(source.getMode()))
    //    .name(source.getName())
    //    .physicalType(locationPhysicalType(source.getPhysicalType()))
    //    .status(status(source.getStatus()))
    //    .telecom(telecoms(source.getTelecoms()))
    //    .type(locationType(source.getType()))
  }

  private Address address(DatamartLocation.Address address) {
    if (address == null) {
      return null;
    }
    if (isBlank(address.line1())
        || allBlank(address.city(), address.state(), address.postalCode())) {

      return null;
    }
    return Address.builder()
        .line(asList(address.line1()))
        .city(address.city())
        .state(address.state())
        .postalCode(address.postalCode())
        .build();
  }
}
