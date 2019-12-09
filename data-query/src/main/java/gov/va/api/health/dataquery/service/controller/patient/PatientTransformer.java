package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asDateString;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.ifPresent;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.argonaut.api.resources.Patient.Contact;
import gov.va.api.health.argonaut.api.resources.Patient.Gender;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.dvp.cdw.xsd.model.CdwAdministrativeGenderCodes;
import gov.va.dvp.cdw.xsd.model.CdwBirthSexCodes;
import gov.va.dvp.cdw.xsd.model.CdwBirthsexExtension;
import gov.va.dvp.cdw.xsd.model.CdwExtensions;
import gov.va.dvp.cdw.xsd.model.CdwExtensions.CdwExtension;
import gov.va.dvp.cdw.xsd.model.CdwExtensions.CdwExtension.CdwValueCoding;
import gov.va.dvp.cdw.xsd.model.CdwMaritalStatusCodes;
import gov.va.dvp.cdw.xsd.model.CdwMaritalStatusSystems;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses.CdwAddress;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier.CdwAssigner;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwMaritalStatus;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwName;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms;
import gov.va.dvp.cdw.xsd.model.CdwPatientContactRelationshipCodes;
import gov.va.dvp.cdw.xsd.model.CdwPatientContactRelationshipSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PatientTransformer implements PatientController.Transformer {
  List<String> addressLine(CdwAddress source) {
    return addressLines(
        source.getStreetAddress1(), source.getStreetAddress2(), source.getStreetAddress3());
  }

  private List<String> addressLines(String line1, String line2, String line3) {
    List<String> line = new LinkedList<>();
    if (StringUtils.isNotBlank(line1)) {
      line.add(line1);
    }
    if (StringUtils.isNotBlank(line2)) {
      line.add(line2);
    }
    if (StringUtils.isNotBlank(line3)) {
      line.add(line3);
    }
    return line.isEmpty() ? null : line;
  }

  List<Address> addresses(CdwAddresses optionalSource) {
    return convertAll(
        ifPresent(optionalSource, CdwAddresses::getAddress),
        cdw ->
            Address.builder()
                .line(addressLine(cdw))
                .city(cdw.getCity())
                .state(cdw.getState())
                .postalCode(cdw.getPostalCode())
                .build());
  }

  @Override
  public Patient apply(CdwPatient source) {
    return patient(source);
  }

  Optional<Extension> argoBirthSex(CdwBirthsexExtension optionalSource) {
    if (optionalSource == null
        || allBlank(optionalSource.getUrl(), optionalSource.getValueCode())) {
      return Optional.empty();
    }
    return Optional.of(
        Extension.builder()
            .url(optionalSource.getUrl())
            .valueCode(ifPresent(optionalSource.getValueCode(), CdwBirthSexCodes::value))
            .build());
  }

  Optional<Extension> argoEthnicity(List<CdwExtensions> optionalSource) {
    if (optionalSource == null || optionalSource.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        Extension.builder()
            .url(optionalSource.get(0).getUrl())
            .extension(argonautExtensions(optionalSource.get(0).getExtension()))
            .build());
  }

  Optional<Extension> argoRace(List<CdwExtensions> optionalSource) {
    if (optionalSource == null || optionalSource.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        Extension.builder()
            .url(optionalSource.get(0).getUrl())
            .extension(argonautExtensions(optionalSource.get(0).getExtension()))
            .build());
  }

  List<Extension> argonautExtensions(List<CdwExtension> source) {
    if (source == null || source.isEmpty()) {
      return null;
    }
    List<Extension> extensions = new LinkedList<>();
    for (CdwExtension extension : source) {
      if ("text".equals(ifPresent(extension, CdwExtension::getUrl))) {
        extensions.add(
            Extension.builder()
                .url(ifPresent(extension, CdwExtension::getUrl))
                .valueString(ifPresent(extension, CdwExtension::getValueString))
                .build());
      } else {
        extensions.add(
            Extension.builder()
                .url(ifPresent(extension, CdwExtension::getUrl))
                .valueCoding(valueCoding(ifPresent(extension, CdwExtension::getValueCoding)))
                .build());
      }
    }
    return extensions;
  }

  List<ContactPoint> contact(CdwContact optionalSource) {
    if (optionalSource == null || isBlank(optionalSource.getPhone())) {
      return Collections.emptyList();
    }
    return singletonList(
        ContactPoint.builder()
            .system(ContactPoint.ContactPointSystem.phone)
            .value(optionalSource.getPhone())
            .build());
  }

  Address contactAddress(CdwContact source) {
    if (isUnusableContactAddress(source)) {
      return null;
    }
    return convert(
        source,
        cdw ->
            Address.builder()
                .line(contactAddressLine(cdw))
                .city(cdw.getCity())
                .state(cdw.getState())
                .country(cdw.getCountry())
                .postalCode(cdw.getPostalCode())
                .build());
  }

  List<String> contactAddressLine(CdwContact source) {
    return addressLines(
        source.getStreetAddress1(), source.getStreetAddress2(), source.getStreetAddress3());
  }

  List<CodeableConcept> contactRelationship(CdwRelationship source) {
    if (source == null || allBlank(source.getCoding(), source.getText())) {
      return null;
    }
    return singletonList(
        CodeableConcept.builder()
            .coding(contactRelationshipCoding(source.getCoding()))
            .text(source.getText())
            .build());
  }

  List<Coding> contactRelationshipCoding(CdwRelationship.CdwCoding source) {
    if (source == null || allBlank(source.getCode(), source.getDisplay(), source.getSystem())) {
      return null;
    }
    return singletonList(
        Coding.builder()
            .system(ifPresent(source.getSystem(), CdwPatientContactRelationshipSystem::value))
            .code(ifPresent(source.getCode(), CdwPatientContactRelationshipCodes::value))
            .display(source.getDisplay())
            .build());
  }

  List<Contact> contacts(CdwContacts optionalSource) {
    return convertAll(
        ifPresent(optionalSource, CdwContacts::getContact),
        cdw ->
            Contact.builder()
                .relationship(contactRelationship(cdw.getRelationship()))
                .name(humanName(cdw.getName()))
                .telecom(contact(cdw))
                .address(contactAddress(cdw))
                .build());
  }

  List<Extension> extensions(
      Optional<Extension> race, Optional<Extension> ethnicity, Optional<Extension> birthSex) {
    List<Extension> extensions = new ArrayList<>(3);
    race.ifPresent(extensions::add);
    ethnicity.ifPresent(extensions::add);
    birthSex.ifPresent(extensions::add);
    return extensions;
  }

  Gender gender(CdwAdministrativeGenderCodes source) {
    return ifPresent(source, gender -> EnumSearcher.of(Patient.Gender.class).find(gender.value()));
  }

  HumanName humanName(String optionalSource) {
    return convert(optionalSource, cdw -> HumanName.builder().text(cdw).build());
  }

  Reference identifierAssigner(CdwAssigner optionalSource) {
    return Reference.builder().display(optionalSource.getDisplay()).build();
  }

  CodeableConcept identifierType(CdwIdentifier.CdwType source) {
    return CodeableConcept.builder().coding(identifierTypeCodings(source.getCoding())).build();
  }

  private Coding identifierTypeCoding(CdwIdentifier.CdwType.CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder().system(cdw.getSystem()).code(cdw.getCode()).build();
  }

  List<Coding> identifierTypeCodings(List<CdwIdentifier.CdwType.CdwCoding> source) {
    return convertAll(source, this::identifierTypeCoding);
  }

  Identifier.IdentifierUse identifierUse(CdwIdentifier source) {
    return ifPresent(source.getUse(), use -> Identifier.IdentifierUse.valueOf(use.value()));
  }

  List<Identifier> identifiers(List<CdwIdentifier> optionalSource) {
    return convertAll(
        optionalSource,
        cdw ->
            Identifier.builder()
                .use(identifierUse(cdw))
                .type(identifierType(cdw.getType()))
                .system(cdw.getSystem())
                .value(cdw.getValue())
                .assigner(identifierAssigner(cdw.getAssigner()))
                .build());
  }

  private Boolean isUnusableContactAddress(CdwContact source) {
    if (source == null
        || allBlank(
            source.getName(),
            source.getPostalCode(),
            source.getCity(),
            source.getState(),
            source.getStreetAddress1(),
            source.getStreetAddress2(),
            source.getStreetAddress3(),
            source.getCountry(),
            source.getPhone(),
            source.getRelationship())) {
      return true;
    }
    return false;
  }

  CodeableConcept maritalStatus(CdwMaritalStatus optionalSource) {
    if (optionalSource == null) {
      return null;
    }
    if (isBlank(optionalSource.getText()) && optionalSource.getCoding().isEmpty()) {
      return null;
    }
    return convert(
        optionalSource,
        cdw ->
            CodeableConcept.builder()
                .text(cdw.getText())
                .coding(maritalStatusCodings(cdw.getCoding()))
                .build());
  }

  private Coding maritalStatusCoding(CdwMaritalStatus.CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(ifPresent(cdw.getSystem(), CdwMaritalStatusSystems::value))
        .code(ifPresent(cdw.getCode(), CdwMaritalStatusCodes::value))
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> maritalStatusCodings(List<CdwMaritalStatus.CdwCoding> source) {
    return convertAll(source, this::maritalStatusCoding);
  }

  List<HumanName> names(CdwName optionalSource) {
    if (optionalSource == null
        || allBlank(
            optionalSource.getUse(),
            optionalSource.getText(),
            optionalSource.getGiven(),
            optionalSource.getFamily())) {
      return null;
    }
    return convert(
        optionalSource,
        cdw ->
            singletonList(
                HumanName.builder()
                    .use(ifPresent(cdw.getUse(), HumanName.NameUse::valueOf))
                    .text(cdw.getText())
                    .family(singletonList(cdw.getFamily()))
                    .given(singletonList(cdw.getGiven()))
                    .build()));
  }

  private Patient patient(CdwPatient source) {
    return Patient.builder()
        .id(source.getCdwId())
        .resourceType("Patient")
        .extension(
            extensions(
                argoRace(source.getArgoRace()),
                argoEthnicity(source.getArgoEthnicity()),
                argoBirthSex(source.getArgoBirthsex())))
        .identifier(identifiers(source.getIdentifier()))
        .name(names(source.getName()))
        .telecom(telecoms(source.getTelecoms()))
        .address(addresses(source.getAddresses()))
        .gender(gender(source.getGender()))
        .birthDate(asDateString(source.getBirthDate()))
        .deceasedBoolean(source.getDeceasedDateTime() == null ? source.isDeceasedBoolean() : null)
        .deceasedDateTime(asDateTimeString(source.getDeceasedDateTime()))
        .maritalStatus(maritalStatus(source.getMaritalStatus()))
        .contact(contacts(source.getContacts()))
        .build();
  }

  List<ContactPoint> telecoms(CdwTelecoms optionalSource) {
    return convertAll(
        ifPresent(optionalSource, CdwTelecoms::getTelecom),
        cdw ->
            ContactPoint.builder()
                .system(
                    ifPresent(
                        cdw.getSystem(),
                        system -> ContactPoint.ContactPointSystem.valueOf(system.value())))
                .value(cdw.getValue())
                .use(
                    ifPresent(
                        cdw.getUse(), use -> ContactPoint.ContactPointUse.valueOf(use.value())))
                .build());
  }

  Coding valueCoding(CdwValueCoding source) {
    if (source == null || allBlank(source.getSystem(), source.getDisplay(), source.getCode())) {
      return null;
    }
    return Coding.builder()
        .display(source.getDisplay())
        .code(source.getCode())
        .system(source.getSystem())
        .build();
  }
}
