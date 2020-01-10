package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.stu3.api.elements.Reference;
import gov.va.api.health.stu3.api.resources.Practitioner;
import gov.va.api.health.stu3.api.datatypes.Period;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class Stu3PractitionerRoleTransformer {
  @NonNull private final DatamartPractitioner datamart;

  //  static Location.LocationAddress address(DatamartLocation.Address address) {
  //    if (address == null) {
  //      return null;
  //    }
  //    if (allBlank(address.line1(), address.city(), address.state(), address.postalCode())) {
  //      return null;
  //    }
  //    return Location.LocationAddress.builder()
  //        .line(asList(address.line1()))
  //        .city(address.city())
  //        .state(address.state())
  //        .postalCode(address.postalCode())
  //        .text(
  //            trimToNull(
  //                trimToEmpty(address.line1())
  //                    + " "
  //                    + trimToEmpty(address.city())
  //                    + " "
  //                    + trimToEmpty(address.state())
  //                    + " "
  //                    + trimToEmpty(address.postalCode())
  //                    + " "))
  //        .build();
  //  }
  //
  //  static CodeableConcept physicalType(Optional<String> maybePhysType) {
  //    if (maybePhysType.isEmpty()) {
  //      return null;
  //    }
  //
  //    String physType = maybePhysType.get();
  //    if (isBlank(physType)) {
  //      return null;
  //    }
  //
  //    return CodeableConcept.builder()
  //        .coding(asList(Coding.builder().display(physType).build()))
  //        .build();
  //  }
  //
  //  static Location.Status status(DatamartLocation.Status status) {
  //    if (status == null) {
  //      return null;
  //    }
  //    return EnumSearcher.of(Location.Status.class).find(status.toString());
  //  }
  //
  //  static List<ContactPoint> telecoms(String telecom) {
  //    if (isBlank(telecom)) {
  //      return null;
  //    }
  //    return asList(
  //        ContactPoint.builder()
  //            .system(ContactPoint.ContactPointSystem.phone)
  //            .value(telecom)
  //            .build());
  //  }
  //
  //  static CodeableConcept type(Optional<String> maybeType) {
  //    if (maybeType.isEmpty()) {
  //      return null;
  //    }
  //
  //    String type = maybeType.get();
  //    if (isBlank(type)) {
  //      return null;
  //    }
  //
  //    return
  // CodeableConcept.builder().coding(asList(Coding.builder().display(type).build())).build();
  //  }

  /** Convert datamart structure to FHIR. */
  public PractitionerRole toFhir() {
    // available:
    //	    private Optional<DatamartCoding> role;
    //	    private List<Specialty> specialty;
    //	    private Optional<Period> period;
    //	    private List<DatamartReference> location;
    //	    private Optional<String> healthCareService;

    return PractitionerRole.builder()
        .resourceType("PractitionerRole")
        .id(datamart.cdwId())
        .period(period(datamart.practitionerRole()))
        .practitioner(practitioner(datamart.cdwId()))
        .organization(organization(datamart.practitionerRole()))
        .code()
        .specialty()
        .location(locations)
        .healthCareService()
        .build();

    return Practitioner.PractitionerRole.builder()
        .location(
            emptyToNull(
                source
                    .location()
                    .stream()
                    .map(loc -> asReference(loc))
                    .collect(Collectors.toList())))
        .role(asCodeableConceptWrapping(source.role()))
        .managingOrganization(asReference(source.managingOrganization()))
        .healthcareService(healthcareServices(source.healthCareService()))
        .build();
  }

  private Reference organization(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    return asReference(role.get().managingOrganization());
  }

  private Reference practitioner(String cdwId) {
    return asReference(
        DatamartReference.builder()
            .type(Optional.of("Practitioner"))
            .reference(Optional.ofNullable(cdwId))
            .build());
  }

  private Period period(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    Optional<DatamartPractitioner.PractitionerRole.Period> period = role.get().period();
    if (period.isEmpty()) {
      return null;
    }
    return Period.builder()
        .start(period.get().start().map(LocalDate::toString).orElse(null))
        .end(period.get().end().map(LocalDate::toString).orElse(null))
        .build();
  }

  static List<Reference> healthcareServices(Optional<String> service) {
    if (isBlank(service)) {
      return null;
    }
    return List.of(Reference.builder().display(service.get()).build());
  }
}
