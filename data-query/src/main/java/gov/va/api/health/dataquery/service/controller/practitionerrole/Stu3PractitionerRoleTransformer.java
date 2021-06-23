package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Stu3Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.elements.Reference;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class Stu3PractitionerRoleTransformer {
  @NonNull private final DatamartPractitionerRole datamart;

  private static List<CodeableConcept> code(DatamartPractitionerRole role) {
    return emptyToNull(
        role.role().stream().map(r -> asCodeableConceptWrapping(r)).collect(toList()));
  }

  static List<Reference> healthCareService(DatamartPractitionerRole role) {
    Optional<String> service = role.healthCareService();
    if (isBlank(service)) {
      return null;
    }
    return List.of(Reference.builder().display(service.get()).build());
  }

  private static List<Reference> locations(DatamartPractitionerRole role) {
    return emptyToNull(role.location().stream().map(loc -> asReference(loc)).collect(toList()));
  }

  private static Reference practitioner(String cdwId) {
    return asReference(
        DatamartReference.builder()
            .type(Optional.of("Practitioner"))
            .reference(Optional.ofNullable(cdwId))
            .build());
  }

  static List<CodeableConcept> specialty(DatamartPractitionerRole role) {
    return emptyToNull(
        role.specialty()
            .stream()
            .map(Stu3PractitionerRoleTransformer::specialty)
            .filter(Objects::nonNull)
            .collect(toList()));
  }

  static CodeableConcept specialty(DatamartPractitionerRole.Specialty dmSpecialty) {
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
        .practitioner(practitioner(datamart.cdwId()))
        .organization(asReference(datamart.managingOrganization()))
        .code(code(datamart))
        .specialty(specialty(datamart))
        .location(locations(datamart))
        .healthcareService(healthCareService(datamart))
        .build();
  }
}
