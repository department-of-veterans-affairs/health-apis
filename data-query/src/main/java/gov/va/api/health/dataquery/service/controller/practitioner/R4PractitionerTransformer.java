package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.resources.Practitioner;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public class R4PractitionerTransformer {
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
        .line(emptyToNull(asList(address.line1(), address.line2(), address.line3())))
        .city(address.city())
        .state(address.state())
        .postalCode(address.postalCode())
        .build();
  }

  static String birthDate(Optional<LocalDate> maybeBirthDate) {
    return maybeBirthDate.map(LocalDate::toString).orElse(null);
  }

  static List<HumanName> name(DatamartPractitioner.Name source) {
    if (source == null
        || allBlank(source.family(), source.given(), source.suffix(), source.prefix())) {
      return null;
    }
    return List.of(
        HumanName.builder()
            .family(source.family())
            .given(nameList(Optional.ofNullable(source.given())))
            .suffix(nameList(source.suffix()))
            .prefix(nameList(source.prefix()))
            .build());
  }

  static List<String> nameList(Optional<String> source) {
    return emptyToNull(asList(source.orElse(null)));
  }

  static ContactPoint telecom(DatamartPractitioner.Telecom telecom) {
    if (telecom == null || allBlank(telecom.system(), telecom.use(), telecom.value())) {
      return null;
    }
    return ContactPoint.builder()
        .system(telecomSystem(telecom.system()))
        .value(telecom.value())
        .use(telecomUse(telecom.use()))
        .build();
  }

  static ContactPoint.ContactPointSystem telecomSystem(DatamartPractitioner.Telecom.System tel) {
    if (tel == null) {
      return null;
    }
    return EnumSearcher.of(ContactPoint.ContactPointSystem.class).find(tel.toString());
  }

  static ContactPoint.ContactPointUse telecomUse(DatamartPractitioner.Telecom.Use tel) {
    if (tel == null) {
      return null;
    }
    return EnumSearcher.of(ContactPoint.ContactPointUse.class).find(tel.toString());
  }

  private List<Address> addresses() {
    return emptyToNull(
        datamart.address().stream().map(adr -> address(adr)).collect(Collectors.toList()));
  }

  Practitioner.GenderCode gender(DatamartPractitioner.Gender source) {
    return convert(
        source, gender -> EnumSearcher.of(Practitioner.GenderCode.class).find(gender.toString()));
  }

  List<ContactPoint> telecoms() {
    return emptyToNull(
        datamart.telecom().stream().map(tel -> telecom(tel)).collect(Collectors.toList()));
  }

  /** Converts from a datamart practitioner to a Fhir Practitioner. */
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
        .build();
  }
}
