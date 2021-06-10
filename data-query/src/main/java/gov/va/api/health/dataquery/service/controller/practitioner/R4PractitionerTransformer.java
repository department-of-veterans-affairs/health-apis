package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Practitioner;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;
import lombok.Builder;

/** Convert from datamart from R4. */
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

  static List<HumanName> name(DatamartPractitioner.Name name) {
    if (name == null || allBlank(name.family(), name.given(), name.suffix(), name.prefix())) {
      return null;
    }
    return List.of(
        HumanName.builder()
            .family(name.family())
            .given(emptyToNull(asList(name.given())))
            .suffix(emptyToNull(asList(name.suffix().orElse(null))))
            .prefix(emptyToNull(asList(name.prefix().orElse(null))))
            .build());
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
        datamart.address().stream().map(adr -> address(adr)).collect(toList()));
  }

  Practitioner.GenderCode gender(DatamartPractitioner.Gender providedGender) {
    if (providedGender == null) {
      return null;
    }
    return EnumSearcher.of(Practitioner.GenderCode.class).find(providedGender.toString());
  }

  List<Identifier> identifiers() {
    // ToDo is unknown the correct value to populate?
    return List.of(
        Identifier.builder()
            .system("http://hl7.org/fhir/sid/us-npi")
            .value(datamart.npi().orElse("Unknown"))
            .build());
  }

  List<ContactPoint> telecoms() {
    return emptyToNull(
        datamart.telecom().stream().map(tel -> telecom(tel)).collect(toList()));
  }

  public Practitioner toFhir() {
    return Practitioner.builder()
        .id(datamart.cdwId())
        .active(datamart.active())
        .name(name(datamart.name()))
        .telecom(telecoms())
        .address(addresses())
        .identifier(identifiers())
        .gender(gender(datamart.gender()))
        .birthDate(birthDate(datamart.birthDate()))
        .build();
  }
}
