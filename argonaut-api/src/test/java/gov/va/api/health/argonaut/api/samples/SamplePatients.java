package gov.va.api.health.argonaut.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.argonaut.api.*;
import java.util.*;
import lombok.NoArgsConstructor;

/**
 * This class provides data structures that are populated with dummy values, suitable for testing
 * serialization.
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public class SamplePatients {

  private SampleDataTypes dataTypes = SampleDataTypes.get();

  List<Address> address() {
    List<Address> addresses = new LinkedList<>();
    addresses.add(Address.builder().state("Missing*").build());
    List<String> line1 = new LinkedList<>();
    line1.add("1234 Test Road");
    line1.add("Testland");
    line1.add("Test POSTGRES");
    addresses.add(
        Address.builder()
            .line(line1)
            .city("Testville")
            .state("Testlina")
            .postalCode("12345")
            .build());
    List<String> line2 = new LinkedList<>();
    line2.add("9876 Fake Lane");
    addresses.add(
        Address.builder()
            .line(line2)
            .city("Fooville")
            .state("Foolina")
            .postalCode("98765")
            .build());
    return addresses;
  }

  public gov.va.api.health.argonaut.api.Patient alivePatient() {
    return gov.va.api.health.argonaut.api.Patient.builder()
        .resourceType("Patient")
        .id("123456789")
        .extension(argoCdwExtensions())
        .identifier(identifier())
        .name(name())
        .telecom(telecom())
        .gender(gov.va.api.health.argonaut.api.Patient.Gender.male)
        .birthDate("2018-11-06")
        .deceasedBoolean(false)
        .address(address())
        .maritalStatus(maritalStatus())
        .contact(contact())
        .build();
  }

  List<Extension> argoCdwExtensions() {
    List<Extension> CdwExtensions = new ArrayList<>(3);

    List<Extension> raceCdwExtensions = new LinkedList<>();
    raceCdwExtensions.add(
        Extension.builder()
            .url("ombTest")
            .valueCoding(
                Coding.builder().system("http://test-race").code("R4C3").display("tester").build())
            .build());
    raceCdwExtensions.add(Extension.builder().url("text").valueString("tester").build());

    List<Extension> ethnicityCdwExtensions = new LinkedList<>();
    ethnicityCdwExtensions.add(
        Extension.builder()
            .url("ombTest")
            .valueCoding(
                Coding.builder()
                    .system("http://test-ethnicity")
                    .code("3THN1C1TY")
                    .display("testa")
                    .build())
            .build());
    ethnicityCdwExtensions.add(Extension.builder().url("text").valueString("testa").build());

    CdwExtensions.add(
        Extension.builder().url("http://test-race").extension(raceCdwExtensions).build());
    CdwExtensions.add(
        Extension.builder().url("http://test-ethnicity").extension(ethnicityCdwExtensions).build());
    CdwExtensions.add(Extension.builder().url("http://test-birthsex").valueCode("M").build());
    return CdwExtensions;
  }

  public Communication communication() {
    return Communication.builder()
        .id("8888")
        .extension(singletonList(dataTypes.extension()))
        .modifierExtension(singletonList(dataTypes.extension()))
        .language(language())
        .preferred(false)
        .build();
  }

  public List<Contact> contact() {
    List<Contact> contacts = new LinkedList<>();

    List<String> line1 = new LinkedList<>();
    line1.add("123 Happy Avenue");
    line1.add("456 Smile Drive");
    line1.add("789 Laughter Lane");
    contacts.add(
        Contact.builder()
            .relationship(relationship())
            .name(HumanName.builder().text("DUCK, DAFFY JOHN").build())
            .telecom(
                singletonList(
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.phone)
                        .value("9998886666")
                        .build()))
            .address(
                Address.builder()
                    .line(line1)
                    .city("Happyland")
                    .state("Happylina")
                    .postalCode("12345")
                    .country("USA")
                    .build())
            .build());

    List<String> line2 = new LinkedList<>();
    line2.add("123 Sad Avenue");
    line2.add("456 Frown Drive");
    line2.add("789 Weeping Lane");
    contacts.add(
        Contact.builder()
            .relationship(relationship())
            .name(HumanName.builder().text("ALICE, TEST JANE").build())
            .telecom(
                singletonList(
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.phone)
                        .value("1112224444")
                        .build()))
            .address(
                Address.builder()
                    .line(line2)
                    .city("Sadland")
                    .state("Sadlina")
                    .postalCode("98765")
                    .country("USA")
                    .build())
            .build());

    return contacts;
  }

  public gov.va.api.health.argonaut.api.Patient deceasedPatient() {
    return gov.va.api.health.argonaut.api.Patient.builder()
        .id("123456789")
        .extension(argoCdwExtensions())
        .identifier(identifier())
        .name(name())
        .telecom(telecom())
        .gender(gov.va.api.health.argonaut.api.Patient.Gender.male)
        .birthDate("2018-11-06")
        .deceasedBoolean(true)
        .deceasedDateTime("2018-11-07")
        .address(address())
        .maritalStatus(maritalStatus())
        .contact(contact())
        .build();
  }

  public List<Identifier> identifier() {
    List<Identifier> identifiers = new LinkedList<>();
    identifiers.add(
        Identifier.builder()
            .use(Identifier.IdentifierUse.usual)
            .type(
                CodeableConcept.builder()
                    .coding(
                        singletonList(
                            Coding.builder().system("http://test-code").code("C0D3").build()))
                    .build())
            .system("http://test-system")
            .value("123456789")
            .assigner(Reference.builder().display("tester-test-index").build())
            .build());

    return identifiers;
  }

  public CodeableConcept language() {
    return CodeableConcept.builder()
        .coding(singletonList(dataTypes.coding()))
        .text("HelloText")
        .build();
  }

  CodeableConcept maritalStatus() {
    return CodeableConcept.builder()
        .coding(
            singletonList(
                Coding.builder()
                    .system("http://hl7.org/fhir/marital-status")
                    .code("M")
                    .display("Married")
                    .build()))
        .text("testMarriage")
        .build();
  }

  List<HumanName> name() {
    return singletonList(
        HumanName.builder()
            .use(HumanName.NameUse.usual)
            .text("FOOMAN FOO")
            .family(singletonList("FOO"))
            .given(singletonList("FOOMAN"))
            .build());
  }

  List<CodeableConcept> relationship() {
    return singletonList(
        CodeableConcept.builder()
            .coding(
                singletonList(
                    Coding.builder()
                        .system("http://hl7.org/fhir/patient-contact-relationship")
                        .code("emergency")
                        .display("Emergency")
                        .build()))
            .text("Emergency Contact")
            .build());
  }

  public List<ContactPoint> telecom() {
    List<ContactPoint> telecoms = new LinkedList<>();
    telecoms.add(
        ContactPoint.builder()
            .system(ContactPoint.ContactPointSystem.phone)
            .value("9998886666")
            .use(ContactPoint.ContactPointUse.home)
            .build());
    telecoms.add(
        ContactPoint.builder()
            .system(ContactPoint.ContactPointSystem.phone)
            .value("1112224444")
            .use(ContactPoint.ContactPointUse.work)
            .build());
    return telecoms;
  }
}
