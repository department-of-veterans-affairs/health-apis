package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.stu3.api.datatypes.Address;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.datatypes.HumanName;
import gov.va.api.health.stu3.api.datatypes.Identifier;
import gov.va.api.health.stu3.api.resources.Practitioner;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.Builder;

@Builder
public class Stu3PractitionerTransformer {
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

  static String birthDate(Optional<LocalDate> source) {
    return source.map(LocalDate::toString).orElse(null);
  }

  private static Practitioner.Gender gender(DatamartPractitioner.Gender source) {
    return convert(
        source, gender -> EnumSearcher.of(Practitioner.Gender.class).find(gender.toString()));
  }

  private static List<Identifier> identifiers(Optional<String> npi) {
    // TODO is unknown the correct value to populate in case of missing NPI?
    return asList(
        Identifier.builder()
            .system("http://hl7.org/fhir/sid/us-npi")
            .value(npi.orElse("Unknown"))
            .build());
  }

  static List<HumanName> name(DatamartPractitioner.Name source) {
    if (source == null || isBlank(source.family())) {
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
    if (isBlank(source)) {
      return null;
    }
    return singletonList(source.get());
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
    return emptyToNull(datamart.address().stream().map(adr -> address(adr)).collect(toList()));
  }

  List<ContactPoint> telecoms() {
    return emptyToNull(datamart.telecom().stream().map(tel -> telecom(tel)).collect(toList()));
  }

  Practitioner toFhir() {
    return Practitioner.builder()
        .id(datamart.cdwId())
        .active(datamart.active())
        .telecom(telecoms())
        .address(addresses())
        .gender(gender(datamart.gender()))
        .birthDate(birthDate(datamart.birthDate()))
        .name(name(datamart.name()))
        .identifier(identifiers(datamart.npi()))
        .build();
  }
}
