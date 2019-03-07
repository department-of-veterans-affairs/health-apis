package gov.va.api.health.dataquery.service.controller.patient;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.datatypes.Address;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.ContactPoint;
import gov.va.api.health.dataquery.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.dataquery.api.datatypes.ContactPoint.ContactPointUse;
import gov.va.api.health.dataquery.api.datatypes.HumanName;
import gov.va.api.health.dataquery.api.datatypes.HumanName.NameUse;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.Patient;
import gov.va.api.health.dataquery.api.resources.Patient.Contact;
import gov.va.api.health.dataquery.api.resources.Patient.Gender;
import gov.va.dvp.cdw.xsd.model.CdwAdministrativeGenderCodes;
import gov.va.dvp.cdw.xsd.model.CdwBirthSexCodes;
import gov.va.dvp.cdw.xsd.model.CdwBirthsexExtension;
import gov.va.dvp.cdw.xsd.model.CdwContactPointSystemCodes;
import gov.va.dvp.cdw.xsd.model.CdwContactPointUseCodes;
import gov.va.dvp.cdw.xsd.model.CdwExtensions;
import gov.va.dvp.cdw.xsd.model.CdwExtensions.CdwExtension;
import gov.va.dvp.cdw.xsd.model.CdwExtensions.CdwExtension.CdwValueCoding;
import gov.va.dvp.cdw.xsd.model.CdwIdentifierUseCodes;
import gov.va.dvp.cdw.xsd.model.CdwMaritalStatusCodes;
import gov.va.dvp.cdw.xsd.model.CdwMaritalStatusSystems;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses.CdwAddress;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier.CdwAssigner;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier.CdwType;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwMaritalStatus;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwName;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms.CdwTelecom;
import gov.va.dvp.cdw.xsd.model.CdwPatientContactRelationshipCodes;
import gov.va.dvp.cdw.xsd.model.CdwPatientContactRelationshipSystem;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Test;

public class PatientTransformerTest {

  private final PatientTransformer tx = new PatientTransformer();
  private final CdwSampleData cdw = CdwSampleData.get();
  private final Expected expected = Expected.get();

  @Test
  public void addresses() {
    assertThat(tx.addresses(cdw.addresses())).isEqualTo(expected.address());
    assertThat(tx.addresses(null)).isNull();
    assertThat(tx.addresses(new CdwAddresses())).isNull();
  }

  @Test
  public void argoBirthsex() {
    assertThat(tx.argoBirthSex(cdw.argoBirthsex()))
        .isEqualTo(Optional.of(expected.argoCdwExtensions().get(2)));
    assertThat(tx.argoBirthSex(new CdwBirthsexExtension())).isEqualTo(Optional.empty());
    assertThat(tx.argoBirthSex(null)).isEqualTo(Optional.empty());
  }

  @Test
  public void argoEthnicity() {
    assertThat(tx.argoEthnicity(singletonList(cdw.argoEthnicities())))
        .isEqualTo(Optional.of(expected.argoCdwExtensions().get(1)));
    assertThat(tx.argoEthnicity(Collections.emptyList())).isEqualTo(Optional.empty());
    assertThat(tx.argoEthnicity(null)).isEqualTo(Optional.empty());
  }

  @Test
  public void argoRace() {
    assertThat(tx.argoRace(singletonList(cdw.argoRaces())))
        .isEqualTo(Optional.of(expected.argoCdwExtensions().get(0)));
    assertThat(tx.argoRace(Collections.emptyList())).isEqualTo(Optional.empty());
    assertThat(tx.argoRace(null)).isEqualTo(Optional.empty());
  }

  @Test
  public void argonautExtensions() {
    assertThat(tx.argonautExtensions(cdw.argoRaces().getExtension()))
        .isEqualTo(expected.argoCdwExtensions().get(0).extension());
    assertThat(tx.argonautExtensions(cdw.argoEthnicities().getExtension()))
        .isEqualTo(expected.argoCdwExtensions().get(1).extension());
    assertThat(tx.argonautExtensions(cdw.argoTextRace()))
        .isEqualTo(expected.argoTextCdwExtensions().get(0).extension());
    assertThat(tx.argonautExtensions(cdw.argoTextEthnicity()))
        .isEqualTo(expected.argoTextCdwExtensions().get(1).extension());
    assertThat(tx.argonautExtensions(null)).isNull();
    assertThat(tx.argonautExtensions(Collections.emptyList())).isNull();
  }

  @Test
  public void contact() {
    assertThat(tx.contact(cdw.contact())).isEqualTo(expected.contactTelecom());
    assertThat(tx.contact(null)).isEqualTo(Collections.emptyList());
    assertThat(tx.contact(new CdwContact())).isEqualTo(Collections.emptyList());
  }

  @Test
  public void contactAddress() {
    assertThat(tx.contactAddress(cdw.contact())).isEqualTo(expected.contactAddress());
    assertThat(tx.contactAddress(null)).isNull();
    assertThat(tx.contactAddress(new CdwContact())).isNull();
  }

  @Test
  public void contactRelationship() {
    assertThat(tx.contactRelationship(cdw.relationship()))
        .isEqualTo(expected.contactRelationship());
    assertThat(tx.contactRelationship(new CdwRelationship())).isNull();
    assertThat(tx.contactRelationship(null)).isNull();
  }

  @Test
  public void contactRelationshipCoding() {
    assertThat(tx.contactRelationshipCoding(cdw.relationshipCoding()))
        .isEqualTo(expected.contactRelationshipCoding());
    assertThat(tx.contactRelationshipCoding(new CdwCoding())).isNull();
    assertThat(tx.contactRelationshipCoding(null)).isNull();
  }

  @Test
  public void contacts() {
    assertThat(tx.contacts(cdw.contacts())).isEqualTo(expected.contact());
    assertThat(tx.contacts(null)).isNull();
    assertThat(tx.contacts(new CdwContacts())).isNull();
  }

  @Test
  public void gender() {
    assertThat(tx.gender(CdwAdministrativeGenderCodes.MALE)).isEqualTo(Gender.male);
    assertThat(tx.gender(CdwAdministrativeGenderCodes.OTHER)).isEqualTo(Gender.other);
    assertThat(tx.gender(CdwAdministrativeGenderCodes.UNKNOWN)).isEqualTo(Gender.unknown);
    assertThat(tx.gender(CdwAdministrativeGenderCodes.FEMALE)).isEqualTo(Gender.female);
    assertThat(tx.gender(null)).isNull();
  }

  @Test
  public void identifier() {
    assertThat(tx.identifiers(singletonList(cdw.identifier()))).isEqualTo(expected.identifier());
    assertThat(tx.identifiers(Collections.emptyList())).isNull();
    assertThat(tx.identifiers(null)).isNull();
  }

  @Test
  public void identifierTypeCodings() {
    assertThat(tx.identifierTypeCodings(cdw.identifierType().getCoding()))
        .isEqualTo(expected.identifierTypeCoding());
    assertThat(tx.identifierTypeCodings(singletonList(null))).isNull();
    assertThat(tx.identifierTypeCodings(null)).isNull();
  }

  @Test
  public void maritalStatus() {
    assertThat(tx.maritalStatus(cdw.maritalStatus())).isEqualTo(expected.maritalStatus());
    assertThat(tx.maritalStatus(new CdwMaritalStatus())).isNull();
    assertThat(tx.maritalStatus(null)).isNull();
  }

  @Test
  public void maritalStatusCodings() {
    assertThat(tx.maritalStatusCodings(cdw.maritalStatus().getCoding()))
        .isEqualTo(expected.maritalStatusCoding());
    assertThat(tx.maritalStatusCodings(singletonList(null))).isNull();
    assertThat(tx.maritalStatusCodings(null)).isNull();
  }

  @Test
  public void name() {
    assertThat(tx.names(cdw.name())).isEqualTo(expected.name());
    assertThat(tx.names(null)).isNull();
    assertThat(tx.names(new CdwName())).isNull();
  }

  @Test
  public void patient() {
    assertThat(tx.apply(cdw.patient())).isEqualTo(expected.patient());
    assertThat(tx.apply(cdw.noBooleanDeceasedPatient()))
        .isEqualTo(expected.noBooleanDeceasedPatient());
    assertThat(tx.apply(cdw.noDateTimeDeceasedPatient()))
        .isEqualTo(expected.noDateTimeDeceasedPatient());
    assertThat(tx.apply(cdw.noDateTimeNotDeceasedPatient()))
        .isEqualTo(expected.noDateTimeNotDeceasedPatient());
    assertThat(tx.apply(cdw.noDeceasedValuesPatient()))
        .isEqualTo(expected.noDeceasedValuesPatient());
    assertThat(tx.apply(cdw.notDeceasedPatient())).isEqualTo(expected.noBooleanDeceasedPatient());
  }

  @Test
  public void telecoms() {
    assertThat(tx.telecoms(cdw.telecoms())).isEqualTo(expected.telecom());
    assertThat(tx.telecoms(new CdwTelecoms())).isNull();
    assertThat(tx.telecoms(null)).isNull();
  }

  @Test
  public void valueCodings() {
    assertThat(tx.valueCoding(cdw.argoEthnicity().getValueCoding()))
        .isEqualTo(expected.argoEthnicityCoding());
    assertThat(tx.valueCoding(cdw.argoRace().getValueCoding()))
        .isEqualTo(expected.argoRaceCoding());
    assertThat(tx.valueCoding(null)).isNull();
  }

  @NoArgsConstructor(staticName = "get", access = AccessLevel.PUBLIC)
  static class CdwSampleData {

    private CdwAddress address() {
      CdwAddress cdw = new CdwAddress();
      cdw.setState("ARIZONA");
      cdw.setCity("TUCSON");
      cdw.setPostalCode("85713");
      cdw.setStreetAddress1("3601 S 6TH AVE");
      cdw.setStreetAddress2("CHANGE 1");
      cdw.setStreetAddress3("POST POSTGRES UPGRADE 5-22");
      cdw.setGisFipsCode("04019");
      cdw.setGisPatientAddressLatitude(32.1802f);
      cdw.setGisPatientAddressLongitude(-110.965f);
      return cdw;
    }

    private CdwAddresses addresses() {
      CdwAddresses cdw = new CdwAddresses();
      cdw.getAddress().add(address());
      return cdw;
    }

    private CdwBirthsexExtension argoBirthsex() {
      CdwBirthsexExtension cdw = new CdwBirthsexExtension();
      cdw.setUrl("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex");
      cdw.setValueCode(CdwBirthSexCodes.M);
      return cdw;
    }

    private CdwExtensions argoEthnicities() {
      CdwExtensions cdw = new CdwExtensions();
      cdw.setUrl("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity");
      cdw.getExtension().add(argoEthnicity());
      return cdw;
    }

    private CdwExtension argoEthnicity() {
      CdwExtension cdw = new CdwExtension();
      cdw.setUrl("ombCategory");
      cdw.setValueCoding(argoEthnicityCoding());
      return cdw;
    }

    private CdwValueCoding argoEthnicityCoding() {
      CdwValueCoding cdw = new CdwValueCoding();
      cdw.setCode("2186-5");
      cdw.setDisplay("Not Hispanic or Latino");
      cdw.setSystem("http://hl7.org/fhir/ValueSet/v3-Ethnicity");
      return cdw;
    }

    private CdwExtension argoRace() {
      CdwExtension cdw = new CdwExtension();
      cdw.setUrl("ombCategory");
      cdw.setValueCoding(argoRaceCoding());
      return cdw;
    }

    private CdwValueCoding argoRaceCoding() {
      CdwValueCoding cdw = new CdwValueCoding();
      cdw.setCode("1002-5");
      cdw.setDisplay("American Indian or Alaska Native");
      cdw.setSystem("http://hl7.org/fhir/v3/Race");
      return cdw;
    }

    private CdwExtensions argoRaces() {
      CdwExtensions cdw = new CdwExtensions();
      cdw.setUrl("http://fhir.org/guides/argonaut/StructureDefinition/argo-race");
      cdw.getExtension().add(argoRace());
      return cdw;
    }

    private List<CdwExtension> argoTextEthnicity() {
      CdwExtension cdw = new CdwExtension();
      cdw.setUrl("text");
      cdw.setValueString("Not Hispanic or Latino");
      return singletonList(cdw);
    }

    private List<CdwExtension> argoTextRace() {
      CdwExtension cdw = new CdwExtension();
      cdw.setUrl("text");
      cdw.setValueString("American Indian or Alaska Native");
      return singletonList(cdw);
    }

    private CdwAssigner assigner() {
      CdwAssigner cdw = new CdwAssigner();
      cdw.setDisplay("Master Veteran Index");
      return cdw;
    }

    private CdwContact contact() {
      CdwContact cdw = new CdwContact();
      cdw.setCity("TUCSON");
      cdw.setCountry("UNITED STATES OF AMERICA");
      cdw.setName("DUCK,DAFFY JOHN");
      cdw.setPhone("5206164321");
      cdw.setPostalCode("85713");
      cdw.setRelationship(relationship());
      cdw.setState("ARIZONA");
      cdw.setStreetAddress1("3601 S. 6TH AVE");
      cdw.setStreetAddress2("CHANGE 1");
      cdw.setStreetAddress3("85713");
      return cdw;
    }

    private CdwContacts contacts() {
      CdwContacts cdw = new CdwContacts();
      cdw.getContact().add(contact());
      return cdw;
    }

    @SneakyThrows
    private XMLGregorianCalendar dateTime(String timestamp) {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(timestamp);
    }

    CdwPatient.CdwIdentifier identifier() {
      CdwPatient.CdwIdentifier identifier = new CdwPatient.CdwIdentifier();
      identifier.setUse(CdwIdentifierUseCodes.USUAL);
      identifier.setType(identifierType());
      identifier.setSystem("http://va.gov/mvi");
      identifier.setValue("185601V825290");
      identifier.setAssigner(assigner());
      return identifier;
    }

    private CdwType.CdwCoding identifierCoding() {
      CdwType.CdwCoding cdw = new CdwType.CdwCoding();
      cdw.setSystem("http://hl7.org/fhir/v2/0203");
      cdw.setCode("MR");
      return cdw;
    }

    private CdwType identifierType() {
      CdwPatient.CdwIdentifier.CdwType cdw = new CdwPatient.CdwIdentifier.CdwType();
      cdw.getCoding().add(identifierCoding());
      return cdw;
    }

    private CdwMaritalStatus maritalStatus() {
      CdwMaritalStatus cdw = new CdwMaritalStatus();
      cdw.getCoding().add(maritalStatusCoding());
      cdw.setText("");
      return cdw;
    }

    private CdwMaritalStatus.CdwCoding maritalStatusCoding() {
      CdwMaritalStatus.CdwCoding cdw = new CdwMaritalStatus.CdwCoding();
      cdw.setSystem(CdwMaritalStatusSystems.HTTP_HL_7_ORG_FHIR_MARITAL_STATUS);
      cdw.setDisplay("Married");
      cdw.setCode(CdwMaritalStatusCodes.M);
      return cdw;
    }

    private CdwName name() {
      CdwName cdw = new CdwName();
      cdw.setFamily("VETERAN");
      cdw.setGiven("JOHN Q");
      cdw.setText("VETERAN,JOHN Q");
      cdw.setUse("usual");
      return cdw;
    }

    private CdwPatient noBooleanDeceasedPatient() {
      CdwPatient cdw = patient();
      cdw.setDeceasedDateTime(dateTime("1991-08-30T06:00:00Z"));
      return cdw;
    }

    private CdwPatient noDateTimeDeceasedPatient() {
      CdwPatient cdw = patient();
      cdw.setDeceasedDateTime(null);
      cdw.setDeceasedBoolean(true);
      return cdw;
    }

    private CdwPatient noDateTimeNotDeceasedPatient() {
      CdwPatient cdw = patient();
      cdw.setDeceasedDateTime(null);
      cdw.setDeceasedBoolean(false);
      return cdw;
    }

    private CdwPatient noDeceasedValuesPatient() {
      CdwPatient cdw = patient();
      cdw.setDeceasedBoolean(null);
      cdw.setDeceasedDateTime(null);
      return cdw;
    }

    private CdwPatient notDeceasedPatient() {
      CdwPatient cdw = patient();
      cdw.setDeceasedDateTime(dateTime("1991-08-30T06:00:00Z"));
      cdw.setDeceasedBoolean(false);
      return cdw;
    }

    private CdwPatient patient() {
      CdwPatient cdw = new CdwPatient();
      cdw.setCdwId("185601V825290");
      cdw.setAddresses(addresses());
      cdw.setArgoBirthsex(argoBirthsex());
      cdw.getArgoEthnicity().add(argoEthnicities());
      cdw.getArgoRace().add(argoRaces());
      cdw.setBirthDate(dateTime("1941-08-30"));
      cdw.setContacts(contacts());
      cdw.setDeceasedBoolean(true);
      cdw.setDeceasedDateTime(dateTime("1991-08-30T06:00:00Z"));
      cdw.setGender(CdwAdministrativeGenderCodes.MALE);
      cdw.setMaritalStatus(maritalStatus());
      cdw.setName(name());
      cdw.getIdentifier().add(identifier());
      cdw.setTelecoms(telecoms());
      return cdw;
    }

    private CdwRelationship relationship() {
      CdwRelationship cdw = new CdwRelationship();
      cdw.setCoding(relationshipCoding());
      cdw.setText("Emergency Contact");
      return cdw;
    }

    private CdwCoding relationshipCoding() {
      CdwCoding cdw = new CdwCoding();
      cdw.setCode(CdwPatientContactRelationshipCodes.EMERGENCY);
      cdw.setDisplay("Emergency");
      cdw.setSystem(
          CdwPatientContactRelationshipSystem.HTTP_HL_7_ORG_FHIR_PATIENT_CONTACT_RELATIONSHIP);
      return cdw;
    }

    private CdwTelecom telecom() {
      CdwTelecom cdw = new CdwTelecom();
      cdw.setSystem(CdwContactPointSystemCodes.PHONE);
      cdw.setUse(CdwContactPointUseCodes.HOME);
      cdw.setValue("5201234567");
      return cdw;
    }

    private CdwTelecoms telecoms() {
      CdwTelecoms cdw = new CdwTelecoms();
      cdw.getTelecom().add(telecom());
      return cdw;
    }
  }

  @NoArgsConstructor(staticName = "get")
  public static class Expected {

    private List<Address> address() {
      return singletonList(
          Address.builder()
              .line(addressLine())
              .postalCode("85713")
              .state("ARIZONA")
              .city("TUCSON")
              .build());
    }

    private List<String> addressLine() {
      return asList("3601 S 6TH AVE", "CHANGE 1", "POST POSTGRES UPGRADE 5-22");
    }

    private List<Extension> argoCdwExtensions() {
      List<Extension> race =
          singletonList(
              Extension.builder().url("ombCategory").valueCoding(argoRaceCoding()).build());

      List<Extension> ethnicity =
          singletonList(
              Extension.builder().url("ombCategory").valueCoding(argoEthnicityCoding()).build());

      return asList(
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
              .extension(race)
              .build(),
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
              .extension(ethnicity)
              .build(),
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
              .valueCode("M")
              .build());
    }

    private Coding argoEthnicityCoding() {
      return Coding.builder()
          .code("2186-5")
          .display("Not Hispanic or Latino")
          .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
          .build();
    }

    private Coding argoRaceCoding() {
      return Coding.builder()
          .code("1002-5")
          .display("American Indian or Alaska Native")
          .system("http://hl7.org/fhir/v3/Race")
          .build();
    }

    private List<Extension> argoTextCdwExtensions() {
      List<Extension> race =
          singletonList(
              Extension.builder()
                  .url("text")
                  .valueString("American Indian or Alaska Native")
                  .build());

      List<Extension> ethnicity =
          singletonList(
              Extension.builder().url("text").valueString("Not Hispanic or Latino").build());

      return asList(
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
              .extension(race)
              .build(),
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
              .extension(ethnicity)
              .build(),
          Extension.builder()
              .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
              .valueCode("M")
              .build());
    }

    private List<Contact> contact() {
      return singletonList(
          Contact.builder()
              .address(contactAddress())
              .name(contactName())
              .telecom(contactTelecom())
              .relationship(contactRelationship())
              .build());
    }

    private Address contactAddress() {
      return Address.builder()
          .country("UNITED STATES OF AMERICA")
          .city("TUCSON")
          .state("ARIZONA")
          .postalCode("85713")
          .line(contactAddressLine())
          .build();
    }

    private List<String> contactAddressLine() {
      return asList("3601 S. 6TH AVE", "CHANGE 1", "85713");
    }

    private HumanName contactName() {
      return HumanName.builder().text("DUCK,DAFFY JOHN").build();
    }

    private List<CodeableConcept> contactRelationship() {
      return singletonList(
          CodeableConcept.builder()
              .coding(contactRelationshipCoding())
              .text("Emergency Contact")
              .build());
    }

    private List<Coding> contactRelationshipCoding() {
      return singletonList(
          Coding.builder()
              .code("emergency")
              .display("Emergency")
              .system("http://hl7.org/fhir/patient-contact-relationship")
              .build());
    }

    private List<ContactPoint> contactTelecom() {
      return singletonList(
          ContactPoint.builder().value("5206164321").system(ContactPointSystem.phone).build());
    }

    private List<Identifier> identifier() {
      return singletonList(
          Identifier.builder()
              .use(IdentifierUse.usual)
              .system("http://va.gov/mvi")
              .value("185601V825290")
              .assigner(Reference.builder().display("Master Veteran Index").build())
              .type(identifierType())
              .build());
    }

    private CodeableConcept identifierType() {
      return CodeableConcept.builder().coding(identifierTypeCoding()).build();
    }

    private List<Coding> identifierTypeCoding() {
      return singletonList(
          Coding.builder().system("http://hl7.org/fhir/v2/0203").code("MR").build());
    }

    private CodeableConcept maritalStatus() {
      return CodeableConcept.builder().text("").coding(maritalStatusCoding()).build();
    }

    private List<Coding> maritalStatusCoding() {
      return singletonList(
          Coding.builder()
              .system("http://hl7.org/fhir/marital-status")
              .display("Married")
              .code("M")
              .build());
    }

    private List<HumanName> name() {
      return singletonList(
          HumanName.builder()
              .family(singletonList("VETERAN"))
              .given(singletonList("JOHN Q"))
              .use(NameUse.usual)
              .text("VETERAN,JOHN Q")
              .build());
    }

    private Patient noBooleanDeceasedPatient() {
      return patient().resourceType("Patient").deceasedDateTime("1991-08-30T06:00:00Z");
    }

    private Patient noDateTimeDeceasedPatient() {
      return patient().resourceType("Patient").deceasedBoolean(true).deceasedDateTime(null);
    }

    private Patient noDateTimeNotDeceasedPatient() {
      return patient().deceasedBoolean(false).deceasedDateTime(null);
    }

    private Patient noDeceasedValuesPatient() {
      return patient().deceasedBoolean(null).deceasedDateTime(null);
    }

    private Patient patient() {
      return Patient.builder()
          .id("185601V825290")
          .resourceType("Patient")
          .address(address())
          .identifier(identifier())
          .birthDate("1941-08-30")
          .contact(contact())
          .deceasedDateTime("1991-08-30T06:00:00Z")
          .gender(Gender.male)
          .name(name())
          .telecom(telecom())
          .extension(argoCdwExtensions())
          .maritalStatus(maritalStatus())
          .build();
    }

    private List<ContactPoint> telecom() {
      return singletonList(
          ContactPoint.builder()
              .system(ContactPointSystem.phone)
              .use(ContactPointUse.home)
              .value("5201234567")
              .build());
    }
  }
}
