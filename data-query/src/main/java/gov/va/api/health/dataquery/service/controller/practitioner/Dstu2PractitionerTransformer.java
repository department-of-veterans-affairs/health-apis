package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.Address;
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
import org.apache.commons.lang3.BooleanUtils;

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
    return source.map(LocalDate::toString).orElse(null);
  }

  static List<Reference> healthcareServices(Optional<String> service) {
    if (isBlank(service)) {
      return null;
    }
    return List.of(Reference.builder().display(service.get()).build());
  }

  static HumanName name(DatamartPractitioner.Name source) {
    if (source == null
        || allBlank(source.family(), source.given(), source.prefix(), source.suffix())) {
      return null;
    }
    return HumanName.builder()
        .family(nameList(Optional.ofNullable(source.family())))
        .given(nameList(Optional.ofNullable(source.given())))
        .suffix(nameList(source.suffix()))
        .prefix(nameList(source.prefix()))
        .build();
  }

  static List<String> nameList(Optional<String> source) {
    if (isBlank(source)) {
      return null;
    }
    return singletonList(source.get());
  }

  static Practitioner.PractitionerRole practitionerRole(
      DatamartPractitioner.PractitionerRole source) {
    if (source == null
        || allBlank(
            source.healthCareService(),
            source.location(),
            source.managingOrganization(),
            source.role())) {
      return null;
    }
    return Practitioner.PractitionerRole.builder()
        .location(
            emptyToNull(
                source.location().stream()
                    .map(loc -> asReference(loc))
                    .collect(Collectors.toList())))
        .role(asCodeableConceptWrapping(source.role()))
        .managingOrganization(asReference(source.managingOrganization()))
        .healthcareService(healthcareServices(source.healthCareService()))
        .build();
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

  List<Practitioner.PractitionerRole> practitionerRoles() {
    return emptyToNull(
        datamart.practitionerRole().stream()
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
        .active(BooleanUtils.isTrue(datamart.active()))
        .name(name(datamart.name()))
        .telecom(telecoms())
        .address(addresses())
        .gender(gender(datamart.gender()))
        .birthDate(birthDate(datamart.birthDate()))
        .practitionerRole(practitionerRoles())
        .build();
  }
}
