package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.datatypes.Period;
import gov.va.api.health.stu3.api.elements.Reference;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class Stu3PractitionerRoleTransformer {
  @NonNull private final DatamartPractitioner datamart;

  private static List<CodeableConcept> code(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    return emptyToNull(asList(asCodeableConceptWrapping(role.get().role())));
  }

  static List<Reference> healthCareService(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    Optional<String> service = role.get().healthCareService();
    if (isBlank(service)) {
      return null;
    }
    return List.of(Reference.builder().display(service.get()).build());
  }

  private static List<Reference> locations(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    return emptyToNull(
        role.get().location().stream().map(loc -> asReference(loc)).collect(toList()));
  }

  private static Reference organization(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (role.isEmpty()) {
      return null;
    }
    return asReference(role.get().managingOrganization());
  }

  static Period period(Optional<DatamartPractitioner.PractitionerRole> role) {
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

  private static Reference practitioner(String cdwId) {
    return asReference(
        DatamartReference.builder()
            .type(Optional.of("Practitioner"))
            .reference(Optional.ofNullable(cdwId))
            .build());
  }

  static List<CodeableConcept> specialty(Optional<DatamartPractitioner.PractitionerRole> role) {
    if (isBlank(role)) {
      return null;
    }

    return emptyToNull(
        role.get().specialty().stream()
            .map(Stu3PractitionerRoleTransformer::specialty)
            .filter(Objects::nonNull)
            .collect(toList()));
  }

  static CodeableConcept specialty(DatamartPractitioner.PractitionerRole.Specialty dmSpecialty) {
    if (!isBlank(dmSpecialty.x12Code())) {
      return specialty(dmSpecialty.x12Code().get());
    } else if (!isBlank(dmSpecialty.vaCode())) {
      return specialty(dmSpecialty.vaCode().get());
    } else if (!isBlank(dmSpecialty.specialtyCode())) {
      return specialty(dmSpecialty.specialtyCode().get());
    }
    return null;
  }

  static CodeableConcept specialty(String code) {
    if (isBlank(code)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder().system("http://nucc.org/provider-taxonomy").code(code).build()))
        .build();
  }

  public PractitionerRole toFhir() {
    return PractitionerRole.builder()
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
