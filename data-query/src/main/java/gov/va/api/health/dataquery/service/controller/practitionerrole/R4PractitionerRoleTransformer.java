package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.controller.R4Transformers;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class R4PractitionerRoleTransformer {
  @NonNull private final DatamartPractitionerRole datamart;

  static CodeableConcept specialty(DatamartPractitionerRole.Specialty datamartSpecialty) {
    String code = null;
    if (!isBlank(datamartSpecialty.x12Code())) {
      code = datamartSpecialty.x12Code().get();
    } else if (!isBlank(datamartSpecialty.vaCode())) {
      code = datamartSpecialty.vaCode().get();
    } else if (!isBlank(datamartSpecialty.specialtyCode())) {
      code = datamartSpecialty.specialtyCode().get();
    }
    if (code == null) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder().system("http://nucc.org/provider-taxonomy").code(code).build()))
        .build();
  }

  private static ContactPoint telecom(DatamartPractitionerRole.Telecom telecom) {
    if (isBlank(telecom)) {
      return null;
    }
    ContactPointSystem r4system = telecomSystem(telecom.system());
    return ContactPoint.builder().system(r4system).value(telecom.value()).build();
  }

  private static ContactPoint.ContactPointSystem telecomSystem(
      DatamartPractitionerRole.Telecom.System system) {
    if (system == null) {
      return null;
    }
    switch (system) {
      case phone:
        return ContactPointSystem.phone;
      case fax:
        return ContactPointSystem.fax;
      case pager:
        return ContactPointSystem.pager;
      case email:
        return ContactPointSystem.email;
      default:
        return ContactPointSystem.other;
    }
  }

  List<CodeableConcept> code() {
    return emptyToNull(
        datamart.role().stream().map(R4Transformers::asCodeableConceptWrapping).collect(toList()));
  }

  List<Reference> healthcareService() {
    Optional<String> service = datamart.healthCareService();
    if (isBlank(service)) {
      return null;
    }
    return List.of(Reference.builder().display(service.get()).build());
  }

  List<Reference> locations() {
    return emptyToNull(
        datamart.location().stream().map(R4Transformers::asReference).collect(toList()));
  }

  List<CodeableConcept> specialties() {
    return emptyToNull(
        datamart.specialty().stream()
            .filter(Objects::nonNull)
            .map(R4PractitionerRoleTransformer::specialty)
            .collect(toList()));
  }

  List<ContactPoint> telecoms() {
    return emptyToNull(
        datamart.telecom().stream()
            .filter(Objects::nonNull)
            .map(R4PractitionerRoleTransformer::telecom)
            .collect(toList()));
  }

  public PractitionerRole toFhir() {
    return PractitionerRole.builder()
        .id(datamart.cdwId())
        .active(datamart.active())
        .practitioner(asReference(datamart.practitioner()))
        .organization(asReference(datamart.managingOrganization()))
        .code(code())
        .specialty(specialties())
        .healthcareService(healthcareService())
        .location(locations())
        .telecom(telecoms())
        .build();
  }
}
