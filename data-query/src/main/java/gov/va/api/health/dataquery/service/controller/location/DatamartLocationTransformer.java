package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.resources.Location;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class DatamartLocationTransformer {
  @NonNull private final DatamartLocation datamart;

  static Address address(DatamartLocation.Address address) {
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

  static CodeableConcept physicalType(Optional<String> maybePhysType) {
    if (maybePhysType.isEmpty()) {
      return null;
    }

    String physType = maybePhysType.get();
    if (isBlank(physType)) {
      return null;
    }

    return CodeableConcept.builder()
        .coding(asList(Coding.builder().display(physType).build()))
        .build();
  }

  static Location.Status status(DatamartLocation.Status status) {
    if (status == null) {
      return null;
    }
    return EnumSearcher.of(Location.Status.class).find(status.toString());
  }

  static List<ContactPoint> telecoms(String telecom) {
    if (isBlank(telecom)) {
      return null;
    }
    return asList(
        ContactPoint.builder()
            .system(ContactPoint.ContactPointSystem.phone)
            .value(telecom)
            .build());
  }

  static CodeableConcept type(Optional<String> maybeType) {
    if (maybeType.isEmpty()) {
      return null;
    }

    String type = maybeType.get();
    if (isBlank(type)) {
      return null;
    }

    return CodeableConcept.builder().coding(asList(Coding.builder().display(type).build())).build();
  }

  /** Convert the datamart structure to FHIR compliant structure. */
  public Location toFhir() {
    return Location.builder()
        .resourceType("Location")
        .id(datamart.cdwId())
        .address(address(datamart.address()))
        .description(datamart.description().orElse(null))
        .managingOrganization(asReference(datamart.managingOrganization()))
        .mode(Location.Mode.instance)
        .name(datamart.name())
        .physicalType(physicalType(datamart.physicalType()))
        .status(status(datamart.status()))
        .telecom(telecoms(datamart.telecom()))
        .type(type(datamart.type()))
        .build();
  }
}
