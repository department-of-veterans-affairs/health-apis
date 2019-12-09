package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.resources.Location;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class Stu3LocationTransformer {
  @NonNull private final DatamartLocation datamart;

  static Location.LocationAddress address(DatamartLocation.Address address) {
    if (address == null) {
      return null;
    }
    if (allBlank(address.line1(), address.city(), address.state(), address.postalCode())) {
      return null;
    }
    return Location.LocationAddress.builder()
        .line(asList(address.line1()))
        .city(address.city())
        .state(address.state())
        .postalCode(address.postalCode())
        .text(
            trimToNull(
                trimToEmpty(address.line1())
                    + " "
                    + trimToEmpty(address.city())
                    + " "
                    + trimToEmpty(address.state())
                    + " "
                    + trimToEmpty(address.postalCode())
                    + " "))
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

  /** Convert datamart structure to FHIR. */
  public Location toFhir() {
    return Location.builder()
        .resourceType("Location")
        .mode(Location.Mode.instance)
        .id(datamart.cdwId())
        .status(status(datamart.status()))
        .name(datamart.name())
        .description(datamart.description().orElse(null))
        .type(type(datamart.type()))
        .telecom(telecoms(datamart.telecom()))
        .address(address(datamart.address()))
        .physicalType(physicalType(datamart.physicalType()))
        .managingOrganization(asReference(datamart.managingOrganization()))
        .build();
  }
}
