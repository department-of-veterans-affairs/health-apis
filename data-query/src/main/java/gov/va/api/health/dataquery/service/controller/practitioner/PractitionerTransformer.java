package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.dataquery.api.datatypes.Address;
import gov.va.api.health.dataquery.api.datatypes.Address.AddressUse;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.ContactPoint;
import gov.va.api.health.dataquery.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.dataquery.api.datatypes.ContactPoint.ContactPointUse;
import gov.va.api.health.dataquery.api.datatypes.HumanName;
import gov.va.api.health.dataquery.api.datatypes.HumanName.NameUse;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.Practitioner;
import gov.va.api.health.dataquery.api.resources.Practitioner.Gender;
import gov.va.api.health.dataquery.api.resources.Practitioner.PractitionerRole;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwAddresses;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwAddresses.CdwAddress;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwAddresses.CdwAddress.CdwLines;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwName;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwPractitionerRoles;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwPractitionerRoles.CdwPractitionerRole;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwPractitionerRoles.CdwPractitionerRole.CdwHealthcareServices;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwPractitionerRoles.CdwPractitionerRole.CdwLocations;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwTelecoms;
import gov.va.dvp.cdw.xsd.model.CdwPractitioner100Root.CdwPractitioners.CdwPractitioner.CdwTelecoms.CdwTelecom;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerAddressUse;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerGender;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerNameUse;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerRoleCoding;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerRoleCoding.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerRoleCodingCode;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerRoleCodingDisplay;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerTelecomSystem;
import gov.va.dvp.cdw.xsd.model.CdwPractitionerTelecomUse;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PractitionerTransformer implements PractitionerController.Transformer {
  Address address(CdwAddress source) {
    return convert(
        source,
        cdw ->
            Address.builder()
                .line(addressLines(cdw.getLines()))
                .city(cdw.getCity())
                .state(cdw.getState())
                .postalCode(cdw.getPostalCode())
                .use(addressUse(cdw.getUse()))
                .build());
  }

  List<String> addressLines(CdwLines source) {
    if (source == null || source.getLine().isEmpty()) {
      return null;
    }
    return source.getLine();
  }

  AddressUse addressUse(CdwPractitionerAddressUse source) {
    return convert(source, cdw -> EnumSearcher.of(AddressUse.class).find(cdw.value()));
  }

  List<Address> addresses(CdwAddresses optionalSource) {
    return convertAll(ifPresent(optionalSource, CdwAddresses::getAddress), this::address);
  }

  @Override
  public Practitioner apply(CdwPractitioner source) {
    return practitioner(source);
  }

  Gender gender(CdwPractitionerGender source) {
    return convert(source, cdw -> EnumSearcher.of(Gender.class).find(cdw.value()));
  }

  List<Reference> healthcareService(CdwHealthcareServices source) {
    return convertAll(
        ifPresent(source, CdwHealthcareServices::getHealthcareService),
        cdw -> Reference.builder().display(cdw.getDisplay()).reference(cdw.getReference()).build());
  }

  List<Reference> locations(CdwLocations source) {
    return convertAll(
        ifPresent(source, CdwLocations::getLocation),
        cdw -> Reference.builder().display(cdw.getDisplay()).reference(cdw.getReference()).build());
  }

  Reference managingOrganization(CdwReference source) {
    if (source == null || allBlank(source.getDisplay(), source.getReference())) {
      return null;
    }
    return convert(
        source,
        cdw -> Reference.builder().reference(cdw.getReference()).display(cdw.getDisplay()).build());
  }

  HumanName name(CdwName source) {
    if (source == null
        || allBlank(
            source.getFamily(),
            source.getGiven(),
            source.getPrefix(),
            source.getSuffix(),
            source.getText(),
            source.getUse())) {
      return null;
    }
    return convert(
        source,
        cdw ->
            HumanName.builder()
                .use(nameUse(cdw.getUse()))
                .text(cdw.getText())
                .family(nameList(cdw.getFamily()))
                .given(nameList(cdw.getGiven()))
                .suffix(nameList(cdw.getSuffix()))
                .prefix(nameList(cdw.getPrefix()))
                .build());
  }

  List<String> nameList(String source) {
    if (isBlank(source)) {
      return null;
    }
    return singletonList(source);
  }

  NameUse nameUse(CdwPractitionerNameUse source) {
    return convert(source, cdw -> EnumSearcher.of(NameUse.class).find(source.value()));
  }

  private Practitioner practitioner(CdwPractitioner source) {
    return Practitioner.builder()
        .id(source.getCdwId())
        .resourceType("Practitioner")
        .active(source.isActive())
        .name(name(source.getName()))
        .telecom(telecoms(source.getTelecoms()))
        .address(addresses(source.getAddresses()))
        .gender(gender(source.getGender()))
        .birthDate(asDateTimeString(source.getBirthDate()))
        .practitionerRole(practitionerRoles(source.getPractitionerRoles()))
        .build();
  }

  PractitionerRole practitionerRole(CdwPractitionerRole source) {
    if (source == null
        || allBlank(
            source.getHealthcareServices(),
            source.getLocations(),
            source.getManagingOrganization(),
            source.getRole())) {
      return null;
    }
    return PractitionerRole.builder()
        .healthcareService(healthcareService(source.getHealthcareServices()))
        .location(locations(source.getLocations()))
        .managingOrganization(managingOrganization(source.getManagingOrganization()))
        .role(role(source.getRole()))
        .build();
  }

  List<PractitionerRole> practitionerRoles(CdwPractitionerRoles source) {
    List<PractitionerRole> practitionerRoles =
        convertAll(
            ifPresent(source, CdwPractitionerRoles::getPractitionerRole), this::practitionerRole);
    return practitionerRoles == null || practitionerRoles.isEmpty() ? null : practitionerRoles;
  }

  CodeableConcept role(CdwPractitionerRoleCoding source) {
    if (source == null || source.getCoding() == null) {
      return null;
    }
    return CodeableConcept.builder().coding(roleCoding(source.getCoding())).build();
  }

  List<Coding> roleCoding(CdwCoding source) {
    if (source == null || allBlank(source.getSystem(), source.getDisplay(), source.getCode())) {
      return null;
    }
    return convert(
        source,
        cdw ->
            singletonList(
                Coding.builder()
                    .code(ifPresent(cdw.getCode(), CdwPractitionerRoleCodingCode::value))
                    .display(ifPresent(cdw.getDisplay(), CdwPractitionerRoleCodingDisplay::value))
                    .system(cdw.getSystem())
                    .build()));
  }

  ContactPoint telecom(CdwTelecom source) {
    if (source == null || allBlank(source.getSystem(), source.getUse(), source.getValue())) {
      return null;
    }
    return convert(
        source,
        cdw ->
            ContactPoint.builder()
                .system(telecomSystem(cdw.getSystem()))
                .value(cdw.getValue())
                .use(telecomUse(cdw.getUse()))
                .build());
  }

  ContactPointSystem telecomSystem(CdwPractitionerTelecomSystem cdw) {
    return convert(cdw, source -> EnumSearcher.of(ContactPointSystem.class).find(source.value()));
  }

  ContactPointUse telecomUse(CdwPractitionerTelecomUse cdw) {
    return ifPresent(cdw, source -> EnumSearcher.of(ContactPointUse.class).find(source.value()));
  }

  List<ContactPoint> telecoms(CdwTelecoms optionalSource) {
    return convertAll(ifPresent(optionalSource, CdwTelecoms::getTelecom), this::telecom);
  }
}
