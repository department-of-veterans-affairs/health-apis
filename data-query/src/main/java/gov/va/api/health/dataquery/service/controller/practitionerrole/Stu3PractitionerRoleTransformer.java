package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.datatypes.Period;
import gov.va.api.health.stu3.api.elements.Reference;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class Stu3PractitionerRoleTransformer {

  @NonNull private final DatamartPractitioner datamart;

  private CodeableConcept code(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    return asCodeableConceptWrapping(role.get().role());
  }

  private List<Reference> healthCareService(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    Optional<String> service = role.get().healthCareService();
    if (isBlank(service)) {
      return null;
    }
    return List.of(Reference.builder().display(service.get()).build());
  }

  private List<Reference> locations(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    return emptyToNull(
        role.get().location().stream().map(loc -> asReference(loc)).collect(Collectors.toList()));
  }

  private Reference organization(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    return asReference(role.get().managingOrganization());
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

  private Reference practitioner(String cdwId) {
    return asReference(
        DatamartReference.builder()
            .type(Optional.of("Practitioner"))
            .reference(Optional.ofNullable(cdwId))
            .build());
  }

  private CodeableConcept specialty(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    for (DatamartPractitioner.PractitionerRole.Specialty specialty : role.get().specialty()) {
      if (!isBlank(specialty.x12Code())) {
        return specialty(specialty.x12Code().get());
      }
    }
    for (DatamartPractitioner.PractitionerRole.Specialty specialty : role.get().specialty()) {
      if (!isBlank(specialty.vaCode())) {
        return specialty(specialty.vaCode().get());
      }
    }
    for (DatamartPractitioner.PractitionerRole.Specialty specialty : role.get().specialty()) {
      if (!isBlank(specialty.specialtyCode())) {
        return specialty(specialty.specialtyCode().get());
      }
    }
    return null;
  }

  private CodeableConcept specialty(String code) {
    if (isBlank(code)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder().system("http://nucc.org/provider-taxonomy").code(code).build()))
        .build();
  }

  /** Convert datamart structure to FHIR. */
  public PractitionerRole toFhir() {
    return PractitionerRole.builder()
        .resourceType("PractitionerRole")
        .id(datamart.cdwId())
        .period(period(datamart.practitionerRole()))
        .practitioner(practitioner(datamart.cdwId()))
        .organization(organization(datamart.practitionerRole()))
        .code(code(datamart.practitionerRole()))
        .specialty(specialty(datamart.practitionerRole()))
        .location(locations(datamart.practitionerRole()))
        .healthcareService(healthCareService(datamart.practitionerRole()))
        .build();
  }
}
