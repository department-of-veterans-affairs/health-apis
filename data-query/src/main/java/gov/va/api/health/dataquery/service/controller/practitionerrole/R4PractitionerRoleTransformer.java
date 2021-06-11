package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class R4PractitionerRoleTransformer {
  @NonNull private final DatamartPractitioner datamart;

  private static List<CodeableConcept> code(
      Optional<DatamartPractitioner.PractitionerRole> maybeRole) {
    if (isBlank(maybeRole)) {
      return null;
    }
    return List.of(asCodeableConceptWrapping(maybeRole.get().role()));
  }

  static List<Reference> healthcareService(
      Optional<DatamartPractitioner.PractitionerRole> maybeRole) {
    if (isBlank(maybeRole)) {
      return null;
    }
    Optional<String> service = maybeRole.get().healthCareService();
    if (isBlank(service)) {
      return null;
    }
    return List.of(Reference.builder().display(service.get()).build());
  }

  private static List<Reference> locations(
      Optional<DatamartPractitioner.PractitionerRole> maybeRole) {
    if (isBlank(maybeRole)) {
      return null;
    }
    return emptyToNull(
        maybeRole.get().location().stream().map(loc -> asReference(loc)).collect(toList()));
  }

  private static Reference organization(Optional<DatamartPractitioner.PractitionerRole> maybeRole) {
    if (isBlank(maybeRole)) {
      return null;
    }
    return asReference(maybeRole.get().managingOrganization());
  }

  static Period period(Optional<DatamartPractitioner.PractitionerRole> maybeRole) {
    if (isBlank(maybeRole)) {
      return null;
    }
    Optional<DatamartPractitioner.PractitionerRole.Period> period = maybeRole.get().period();
    if (isBlank(period) || allBlank(period.get().start(), period.get().end())) {
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

  static List<CodeableConcept> specialty(
      Optional<DatamartPractitioner.PractitionerRole> maybeRole) {
    if (isBlank(maybeRole)) {
      return null;
    }
    List<CodeableConcept> specialties =
        maybeRole.get().specialty().stream()
            .filter(Objects::nonNull)
            .map(s -> specialty(s))
            .collect(toList());
    return emptyToNull(specialties);
  }

  static CodeableConcept specialty(
      DatamartPractitioner.PractitionerRole.Specialty datamartSpecialty) {
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

  private static ContactPoint telecom(DatamartPractitioner.Telecom telecom) {
    if (isBlank(telecom)) {
      return null;
    }
    ContactPointSystem r4system = telecomSystem(telecom.system());
    return ContactPoint.builder().system(r4system).value(telecom.value()).build();
  }

  private static ContactPoint.ContactPointSystem telecomSystem(
      DatamartPractitioner.Telecom.System system) {
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

  static List<ContactPoint> telecoms(List<DatamartPractitioner.Telecom> telecoms) {
    if (isBlank(telecoms)) {
      return null;
    }
    List<ContactPoint> contactPoints =
        telecoms.stream()
            .filter(Objects::nonNull)
            .map(telecom -> telecom(telecom))
            .collect(toList());
    return emptyToNull(contactPoints);
  }

  public PractitionerRole toFhir() {
    return PractitionerRole.builder()
        .id(datamart.cdwId())
        .active(datamart.active())
        .practitioner(practitioner(datamart.cdwId()))
        .organization(organization(datamart.practitionerRole()))
        .period(period(datamart.practitionerRole()))
        .code(code(datamart.practitionerRole()))
        .specialty(specialty(datamart.practitionerRole()))
        .healthcareService(healthcareService(datamart.practitionerRole()))
        .location(locations(datamart.practitionerRole()))
        .telecom(telecoms(datamart.telecom()))
        .build();
  }
}
