package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.ifPresent;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public class Dstu2PractitionerTransformer {

  private final DatamartPractitioner datamart;

  static Address address(DatamartPractitioner.Address address) {
    if (address == null
        || allBlank(
            address.line1(),
            address.line2(),
            address.line3(),
            address.city(),
            address.state(),
            address.postalCode())) {
      return null;
    }
    return Address.builder()
        .line(emptyToNull(Arrays.asList(address.line1(), address.line2(), address.line3())))
        .city(address.city())
        .state(address.state())
        .postalCode(address.postalCode())
        .build();
  }

  static String birthDate(Optional<LocalDate> source) {
    LocalDate date = source.isEmpty() ? null : source.get();
    if (date == null) {
      return null;
    }
    return date.toString();
  }

  static Reference healthCareService(String s) {
    return Reference.builder().display(s).build();
  }

  static List<Reference> healthcareServices(Optional<String> results) {
    if (results.isEmpty()) {
      return null;
    }
    return results.stream().map(s -> healthCareService(s)).collect(Collectors.toList());
  }

  static HumanName name(DatamartPractitioner.Name source) {
    if (source == null
        || allBlank(source.family(), source.given(), source.prefix(), source.suffix())) {
      return null;
    }
    return convert(
        source,
        name ->
            HumanName.builder()
                .family(nameList(name.family()))
                .given(nameList(name.given()))
                .suffix(nameList(name.prefix().isEmpty() ? null : name.suffix().get()))
                .prefix(nameList(name.suffix().isEmpty() ? null : name.prefix().get()))
                .build());
  }

  static List<String> nameList(String source) {
    if (isBlank(source)) {
      return null;
    }
    return singletonList(source);
  }

  static CodeableConcept role(Optional<DatamartCoding> source) {
    if (source == null || source.get().code().get() == null) {
      return null;
    }
    return CodeableConcept.builder().coding(roleCoding(source.get())).build();
  }

  static List<Coding> roleCoding(DatamartCoding source) {
    if (source == null || allBlank(source.system(), source.display(), source.code())) {
      return null;
    }
    return convert(
        source,
        cdw ->
            List.of(
                Coding.builder()
                    .code(cdw.code().get())
                    .display(cdw.display().get())
                    .system(cdw.system().get())
                    .build()));
  }

  static ContactPoint telecom(DatamartPractitioner.Telecom telecom) {
    if (telecom == null || allBlank(telecom.system(), telecom.use(), telecom.value())) {
      return null;
    }
    return convert(
        telecom,
        tel ->
            ContactPoint.builder()
                .system(telecomSystem(tel.system()))
                .value(tel.value())
                .use(telecomUse(tel.use()))
                .build());
  }

  static ContactPoint.ContactPointSystem telecomSystem(DatamartPractitioner.Telecom.System tel) {
    return convert(
        tel, source -> EnumSearcher.of(ContactPoint.ContactPointSystem.class).find(tel.toString()));
  }

  static ContactPoint.ContactPointUse telecomUse(DatamartPractitioner.Telecom.Use tel) {
    return ifPresent(
        tel, source -> EnumSearcher.of(ContactPoint.ContactPointUse.class).find(source.toString()));
  }

  private List<Address> addresses() {
    return emptyToNull(
        datamart.address().stream().map(adr -> address(adr)).collect(Collectors.toList()));
  }

  Practitioner.Gender gender(DatamartPractitioner.Gender source) {
    return convert(
        source, gender -> EnumSearcher.of(Practitioner.Gender.class).find(gender.toString()));
  }

  Practitioner.PractitionerRole practitionerRole(DatamartPractitioner.PractitionerRole source) {
    if (source == null
        || allBlank(
            source.healthCareService(),
            source.location(),
            source.managingOrganization(),
            source.role())) {
      return null;
    }
    return Practitioner.PractitionerRole.builder()
        .location(singletonList(asReference(datamart.practitionerRole().get().location().get(0))))
        .role(role(source.role()))
        .managingOrganization(asReference(datamart.practitionerRole().get().managingOrganization()))
        .healthcareService(healthcareServices(source.healthCareService()))
        .build();
  }

  List<Practitioner.PractitionerRole> practitionerRoles() {
    return emptyToNull(
        datamart
            .practitionerRole()
            .stream()
            .map(rol -> practitionerRole(rol))
            .collect(Collectors.toList()));
  }

  List<ContactPoint> telecoms() {
    return emptyToNull(
        datamart.telecom().stream().map(tel -> telecom(tel)).collect(Collectors.toList()));
  }

  /** Convert the datamart structure to FHIR compliant structure. */
  public Practitioner toFhir() {
    return Practitioner.builder()
        .id(datamart.cdwId())
        .resourceType("Practitioner")
        .active(datamart.active())
        .name(name(datamart.name()))
        .telecom(telecoms())
        .address(addresses())
        .gender(gender(datamart.gender()))
        .birthDate(birthDate(datamart.birthDate()))
        .practitionerRole(practitionerRoles())
        .build();
  }
}