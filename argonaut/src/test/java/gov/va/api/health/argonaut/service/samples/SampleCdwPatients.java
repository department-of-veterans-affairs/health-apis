package gov.va.api.health.argonaut.service.samples;

import gov.va.dvp.cdw.xsd.model.*;
import java.math.BigInteger;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;

public class SampleCdwPatients {

  private DatatypeFactory datatypeFactory;

  @SneakyThrows
  public SampleCdwPatients() {
    datatypeFactory = DatatypeFactory.newInstance();
  }

  CdwBirthsexExtension CdwBirthsexExtension() {
    CdwBirthsexExtension testCdwBirthsexExtension = new CdwBirthsexExtension();
    testCdwBirthsexExtension.setValueCode(CdwBirthSexCodes.M);
    testCdwBirthsexExtension.setUrl("http://test-birthsex");
    return testCdwBirthsexExtension;
  }

  CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses addresses() {

    CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses addresses =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses();

    CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses.CdwAddress address =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses.CdwAddress();
    address.setState("Missing*");
    addresses.getAddress().add(address);

    address = new CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses.CdwAddress();
    address.setStreetAddress1("1234 Test Road");
    address.setStreetAddress2("Testland");
    address.setStreetAddress3("Test POSTGRES");
    address.setCity("Testville");
    address.setState("Testlina");
    address.setPostalCode("12345");
    addresses.getAddress().add(address);

    address = new CdwPatient103Root.CdwPatients.CdwPatient.CdwAddresses.CdwAddress();
    address.setStreetAddress1("9876 Fake Lane");
    address.setCity("Fooville");
    address.setState("Foolina");
    address.setPostalCode("98765");
    addresses.getAddress().add(address);

    return addresses;
  }

  public CdwPatient103Root.CdwPatients.CdwPatient alivePatient() {
    CdwPatient103Root.CdwPatients.CdwPatient patient =
        new CdwPatient103Root.CdwPatients.CdwPatient();
    patient.setRowNumber(new BigInteger("1"));
    patient.setCdwId("123456789");
    patient.getArgoRace().add(raceCdwExtensions());
    patient.getArgoEthnicity().add(ethnicityCdwExtensions());
    patient.setArgoBirthsex(CdwBirthsexExtension());
    patient.getIdentifier().add(identifier());
    patient.setName(name());
    patient.setTelecoms(telecoms());
    patient.setAddresses(addresses());
    patient.setGender(CdwAdministrativeGenderCodes.MALE);
    patient.setBirthDate(birthdate());
    patient.setDeceasedBoolean(false);
    patient.setMaritalStatus(maritalStatus());
    patient.setContacts(contacts());
    return patient;
  }

  @SneakyThrows
  XMLGregorianCalendar birthdate() {
    XMLGregorianCalendar birthdate = datatypeFactory.newXMLGregorianCalendar();
    birthdate.setYear(2018);
    birthdate.setMonth(11);
    birthdate.setDay(06);
    return birthdate;
  }

  CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts contacts() {
    CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts contacts =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts();

    CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact contact1 =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact();

    CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship relationship1 =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship();

    CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship.CdwCoding
        relationshipCoding1 =
            new CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship
                .CdwCoding();
    relationshipCoding1.setSystem(
        CdwPatientContactRelationshipSystem.HTTP_HL_7_ORG_FHIR_PATIENT_CONTACT_RELATIONSHIP);
    relationshipCoding1.setCode(CdwPatientContactRelationshipCodes.EMERGENCY);
    relationshipCoding1.setDisplay("Emergency");

    relationship1.setCoding(relationshipCoding1);
    relationship1.setText("Emergency Contact");

    contact1.setRelationship(relationship1);
    contact1.setName("DUCK, DAFFY JOHN");
    contact1.setPhone("9998886666");
    contact1.setStreetAddress1("123 Happy Avenue");
    contact1.setStreetAddress2("456 Smile Drive");
    contact1.setStreetAddress3("789 Laughter Lane");
    contact1.setCity("Happyland");
    contact1.setState("Happylina");
    contact1.setPostalCode("12345");
    contact1.setCountry("USA");
    contacts.getContact().add(contact1);

    CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact contact2 =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact();

    CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship relationship2 =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship();

    CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship.CdwCoding
        relationshipCoding2 =
            new CdwPatient103Root.CdwPatients.CdwPatient.CdwContacts.CdwContact.CdwRelationship
                .CdwCoding();
    relationshipCoding2.setSystem(
        CdwPatientContactRelationshipSystem.HTTP_HL_7_ORG_FHIR_PATIENT_CONTACT_RELATIONSHIP);
    relationshipCoding2.setCode(CdwPatientContactRelationshipCodes.EMERGENCY);
    relationshipCoding2.setDisplay("Emergency");

    relationship2.setCoding(relationshipCoding2);
    relationship2.setText("Emergency Contact");

    contact2.setRelationship(relationship2);
    contact2.setName("ALICE, TEST JANE");
    contact2.setPhone("1112224444");
    contact2.setStreetAddress1("123 Sad Avenue");
    contact2.setStreetAddress2("456 Frown Drive");
    contact2.setStreetAddress3("789 Weeping Lane");
    contact2.setCity("Sadland");
    contact2.setState("Sadlina");
    contact2.setPostalCode("98765");
    contact2.setCountry("USA");
    contacts.getContact().add(contact2);

    return contacts;
  }

  public CdwPatient103Root.CdwPatients.CdwPatient deadPatient() {
    CdwPatient103Root.CdwPatients.CdwPatient patient =
        new CdwPatient103Root.CdwPatients.CdwPatient();
    patient.setDeceasedBoolean(true);
    patient.setDeceasedDateTime(deceasedDateTime());
    return patient;
  }

  XMLGregorianCalendar deceasedDateTime() {
    XMLGregorianCalendar deceasedDate = datatypeFactory.newXMLGregorianCalendar();
    deceasedDate.setYear(2018);
    deceasedDate.setMonth(11);
    deceasedDate.setDay(07);
    return deceasedDate;
  }

  CdwExtensions ethnicityCdwExtensions() {
    CdwExtensions extensions = new CdwExtensions();
    extensions.setUrl("http://test-ethnicity");

    CdwExtensions.CdwExtension extension1 = new CdwExtensions.CdwExtension();
    extension1.setUrl("ombTest");

    CdwExtensions.CdwExtension.CdwValueCoding valueCoding =
        new CdwExtensions.CdwExtension.CdwValueCoding();
    valueCoding.setSystem("http://test-ethnicity");
    valueCoding.setCode("3THN1C1TY");
    valueCoding.setDisplay("testa");
    extension1.setValueCoding(valueCoding);

    extensions.getExtension().add(extension1);

    CdwExtensions.CdwExtension extension2 = new CdwExtensions.CdwExtension();
    extension2.setUrl("text");
    extension2.setValueString("testa");

    extensions.getExtension().add(extension2);
    return extensions;
  }

  CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier identifier() {
    CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier identifier =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier();
    identifier.setUse(CdwIdentifierUseCodes.USUAL);
    CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier.CdwType type =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier.CdwType();
    CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier.CdwType.CdwCoding coding =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier.CdwType.CdwCoding();
    coding.setSystem("http://test-code");
    coding.setCode("C0D3");
    type.getCoding().add(coding);
    identifier.setType(type);
    identifier.setSystem("http://test-system");
    identifier.setValue("123456789");
    CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier.CdwAssigner assigner =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwIdentifier.CdwAssigner();
    assigner.setDisplay("tester-test-index");
    identifier.setAssigner(assigner);
    return identifier;
  }

  public CdwPatient103Root.CdwPatients.CdwPatient.CdwMaritalStatus maritalStatus() {
    CdwPatient103Root.CdwPatients.CdwPatient.CdwMaritalStatus maritalStatus =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwMaritalStatus();
    maritalStatus.setText("testMarriage");
    CdwPatient103Root.CdwPatients.CdwPatient.CdwMaritalStatus.CdwCoding coding =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwMaritalStatus.CdwCoding();
    coding.setSystem(CdwMaritalStatusSystems.HTTP_HL_7_ORG_FHIR_MARITAL_STATUS);
    coding.setCode(CdwMaritalStatusCodes.M);
    coding.setDisplay("Married");
    maritalStatus.getCoding().add(coding);
    return maritalStatus;
  }

  CdwPatient103Root.CdwPatients.CdwPatient.CdwName name() {
    CdwPatient103Root.CdwPatients.CdwPatient.CdwName name =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwName();
    name.setUse("usual");
    name.setText("FOOMAN FOO");
    name.setFamily("FOO");
    name.setGiven("FOOMAN");
    return name;
  }

  CdwExtensions raceCdwExtensions() {
    CdwExtensions CdwExtensions = new CdwExtensions();
    CdwExtensions.setUrl("http://test-race");

    gov.va.dvp.cdw.xsd.model.CdwExtensions.CdwExtension extension1 =
        new CdwExtensions.CdwExtension();
    extension1.setUrl("ombTest");

    gov.va.dvp.cdw.xsd.model.CdwExtensions.CdwExtension.CdwValueCoding valueCoding =
        new gov.va.dvp.cdw.xsd.model.CdwExtensions.CdwExtension.CdwValueCoding();
    valueCoding.setSystem("http://test-race");
    valueCoding.setCode("R4C3");
    valueCoding.setDisplay("tester");
    extension1.setValueCoding(valueCoding);

    CdwExtensions.getExtension().add(extension1);

    gov.va.dvp.cdw.xsd.model.CdwExtensions.CdwExtension extension2 =
        new CdwExtensions.CdwExtension();
    extension2.setUrl("text");
    extension2.setValueString("tester");

    CdwExtensions.getExtension().add(extension2);
    return CdwExtensions;
  }

  CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms telecoms() {
    CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms testTelecoms =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms();

    CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms.CdwTelecom telecom1 =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms.CdwTelecom();
    telecom1.setSystem(CdwContactPointSystemCodes.PHONE);
    telecom1.setValue("9998886666");
    telecom1.setUse(CdwContactPointUseCodes.HOME);
    testTelecoms.getTelecom().add(telecom1);

    CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms.CdwTelecom telecom2 =
        new CdwPatient103Root.CdwPatients.CdwPatient.CdwTelecoms.CdwTelecom();
    telecom2.setSystem(CdwContactPointSystemCodes.PHONE);
    telecom2.setValue("1112224444");
    telecom2.setUse(CdwContactPointUseCodes.WORK);
    testTelecoms.getTelecom().add(telecom2);
    return testTelecoms;
  }
}
