package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.resources.Location;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

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
        .line(emptyToNull(asList(address.line1())))
        .city(address.city())
        .state(address.state())
        .postalCode(address.postalCode())
        .text(
            Stream.of(address.line1(), address.city(), address.state(), address.postalCode())
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" ")))
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
