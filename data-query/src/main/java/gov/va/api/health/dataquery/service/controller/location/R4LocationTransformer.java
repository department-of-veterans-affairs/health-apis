package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.uscorer4.api.resources.Location;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.Collections.singletonList;

@Builder
public class R4LocationTransformer {
    @NonNull
    private final DatamartLocation datamart;

    Location.Status status(DatamartLocation.Status status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case active -> Location.Status.active;
            case inactive -> Location.Status.inactive;
            default -> throw new IllegalArgumentException("Unknown DatamartStatus: " + status.toString());
        };
    }

    List<CodeableConcept> types(Optional<String> maybeType) {
        String type = maybeType.orElse(null);
       if (isBlank(type)) {
           return null;
       }
       return List.of(CodeableConcept.builder().coding(List.of(Coding.builder().display(type).build())).build());
    }

    List<ContactPoint> telecoms(String telecom) {
        if (isBlank(telecom)) {
            return null;
        }
        return List.of(ContactPoint.builder()
                .system(ContactPoint.ContactPointSystem.phone)
                .value(telecom)
                .build());
    }

    Address address(DatamartLocation.Address address) {
        if (address == null || allBlank(address.line1(), address.city(), address.state(), address.postalCode())) {
            return null;
        }
        String addressText = Stream.of(address.line1(), address.city(), address.state(), address.postalCode())
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
        return Address.builder()
                .line(emptyToNull(singletonList(address.line1())))
                .city(address.city())
                .state(address.state())
                .postalCode(address.postalCode())
                .text(addressText)
                .build();
    }

    CodeableConcept physicalType(Optional<String> maybePhysicalType) {
        String physicalType = maybePhysicalType.orElse(null);
        if (isBlank(physicalType)) {
            return null;
        }
        return CodeableConcept.builder().coding(List.of(Coding.builder().display(physicalType).build())).build();
    }

    public Location toFhir() {
    return Location.builder()
        .resourceType("Location")
        .id(datamart.cdwId())
        .mode(Location.Mode.instance)
        .status(status(datamart.status()))
        .name(datamart.name())
        .description(datamart.description().orElse(null))
        .type(types(datamart.type()))
        .telecom(telecoms(datamart.telecom()))
        .address(address(datamart.address()))
        .physicalType(physicalType(datamart.physicalType()))
        .managingOrganization(asReference(datamart.managingOrganization()))
        .build();
    }
}
