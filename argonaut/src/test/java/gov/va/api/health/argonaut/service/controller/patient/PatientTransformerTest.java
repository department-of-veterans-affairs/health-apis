package gov.va.api.health.argonaut.service.controller.patient;

import gov.va.api.health.argonaut.api.*;
import gov.va.api.health.argonaut.api.samples.SamplePatients;
import gov.va.api.health.argonaut.service.samples.SampleCdwPatients;
import gov.va.api.health.argonaut.service.controller.Transformers;
import org.junit.Test;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PatientTransformerTest {
  private SampleCdwPatients cdw = new SampleCdwPatients();
  private SamplePatients expectedPatient = SamplePatients.get();

  @Test
  public void addressReturnsNullForNull() {
    assertThat(transformer().address(null)).isNull();
  }

  @Test
  public void addressTransformsToLine() {
    List<String> testLine =
        transformer().addressLine(cdw.alivePatient().getAddresses().getAddress().get(0));
    List<String> expectedLine = expectedPatient.alivePatient().address().get(0).line();
    assertThat(testLine).isEqualTo(expectedLine);
  }

  @Test
  public void addressesTransformsToAddressList() {
    List<Address> testAddresses = transformer().addresses(cdw.alivePatient().getAddresses());
    List<Address> expectedAddresses = expectedPatient.alivePatient().address();
    assertThat(testAddresses).isEqualTo(expectedAddresses);
  }

  @Test
  public void addressessReturnsEmptyListForNull() {
    assertThat(transformer().addresses(null)).isEmpty();
  }

  @Test
  public void argoBirthsex() {
    Optional<Extension> testArgoBirthsex =
        transformer().argoBirthSex(cdw.alivePatient().getArgoBirthsex());
    Extension expexctedArgoBirthsex = expectedPatient.alivePatient().extension().get(2);
    assertThat(testArgoBirthsex.get()).isEqualTo(expexctedArgoBirthsex);
  }

  @Test
  public void argoBirthsexReturnsEmptyForNull() {
    assertThat(transformer().argoBirthSex(null)).isEmpty();
  }

  @Test
  public void argoEthnicityReturnsEmptyForNull() {
    assertThat(transformer().argoEthnicity(Collections.emptyList())).isEmpty();
  }

  @Test
  public void argoEthnicityTransformsToExtensionList() {
    List<Extension> testArgoEthnicity =
        transformer()
            .argonautExtensions(cdw.alivePatient().getArgoEthnicity().get(0).getExtension());
    List<Extension> expectedArgoEthnicity =
        expectedPatient.alivePatient().extension().get(1).extension();
    assertThat(testArgoEthnicity).isEqualTo(expectedArgoEthnicity);
  }

  @Test
  public void argoEthnicityTransformsToOptionalExtensionList() {
    Optional<Extension> testArgoEthnicity =
        transformer().argoEthnicity(cdw.alivePatient().getArgoEthnicity());
    Extension expectedArgoEthnicity = expectedPatient.alivePatient().extension().get(1);
    assertThat(testArgoEthnicity.get()).isEqualTo(expectedArgoEthnicity);
  }

  @Test
  public void argoPatientCdwExtensionsMissingTransformsToEmptyExtensionList() {
    List<Extension> testPatientCdwExtensions =
        transformer().extensions(Optional.empty(), Optional.empty(), Optional.empty());
    List<Extension> expectedPatientCdwExtensions = new LinkedList<>();
    assertThat(testPatientCdwExtensions).isEqualTo(expectedPatientCdwExtensions);
  }

  @Test
  public void argoPatientCdwExtensionsTransformToExtensionList() {
    List<Extension> testPatientCdwExtensions =
        transformer()
            .extensions(
                transformer().argoRace(cdw.alivePatient().getArgoRace()),
                transformer().argoEthnicity(cdw.alivePatient().getArgoEthnicity()),
                transformer().argoBirthSex(cdw.alivePatient().getArgoBirthsex()));
    List<Extension> expectedPatientCdwExtensions = expectedPatient.alivePatient().extension();
    assertThat(testPatientCdwExtensions).isEqualTo(expectedPatientCdwExtensions);
  }

  @Test
  public void argoRaceExtensionTransformsToExtensionList() {
    List<Extension> testRaceExtension =
        transformer().argonautExtensions(cdw.alivePatient().getArgoRace().get(0).getExtension());
    List<Extension> expectedRaceExtension =
        expectedPatient.alivePatient().extension().get(0).extension();
    assertThat(testRaceExtension).isEqualTo(expectedRaceExtension);
  }

  @Test
  public void argoRaceReturnsEmptyForNull() {
    assertThat(transformer().argoRace(Collections.emptyList())).isEmpty();
  }

  @Test
  public void argoRaceTransformsToOptionalExtensionList() {
    Optional<Extension> testArgoRace = transformer().argoRace(cdw.alivePatient().getArgoRace());
    Extension expectedArgoRace = expectedPatient.alivePatient().extension().get(0);
    assertThat(testArgoRace.get()).isEqualTo(expectedArgoRace);
  }

  @Test
  public void birthDateTransformsToSimpleDateString() {
    String testSimpleDate = Transformers.asDateString(cdw.alivePatient().getBirthDate());
    String expectedSimpleDate = expectedPatient.alivePatient().birthDate();
    assertThat(testSimpleDate).isEqualTo(expectedSimpleDate);
  }

  @Test
  public void contactRelationshipTransformsToCodeableConceptList() {
    List<CodeableConcept> testRelationships =
        transformer()
            .contactRelationship(
                cdw.alivePatient().getContacts().getContact().get(0).getRelationship());
    List<CodeableConcept> expectedRelationships =
        expectedPatient.alivePatient().contact().get(0).relationship();
    assertThat(testRelationships).isEqualTo(expectedRelationships);
  }

  @Test
  public void contactReturnsEmptyListForNull() {
    assertThat(transformer().contact(null)).isEmpty();
  }

  @Test
  public void contactStringNameTransformsToHumanName() {
    PatientTransformer patientTransformer = transformer();
    HumanName testName =
        patientTransformer.humanName(
            cdw.alivePatient().getContacts().getContact().get(0).getName());
    HumanName expectedName = expectedPatient.alivePatient().contact().get(0).name();
    assertThat(testName).isEqualTo(expectedName);
  }

  @Test
  public void contactTelecomTranformsToContactPointList() {
    List<ContactPoint> testTelecoms =
        transformer().contact(cdw.alivePatient().getContacts().getContact().get(0));
    List<ContactPoint> expectedTelecoms = expectedPatient.contact().get(0).telecom();
    assertThat(testTelecoms).isEqualTo(expectedTelecoms);
  }

  @Test
  public void contactTransformsToAddress() {
    Address testAddress =
        transformer().address(cdw.alivePatient().getContacts().getContact().get(0));
    Address expectedAddress = expectedPatient.alivePatient().contact().get(0).address();
    assertThat(testAddress).isEqualTo(expectedAddress);
  }

  @Test
  public void contactTransformsToStringLine() {
    List<String> testLine =
        transformer().addressLine(cdw.alivePatient().getContacts().getContact().get(0));
    List<String> expectedLine = expectedPatient.alivePatient().contact().get(0).address().line();
    assertThat(testLine).isEqualTo(expectedLine);
  }

  @Test
  public void contactsReturnsNullForNull() {
    assertThat(transformer().contacts(null)).isNull();
  }

  @Test
  public void contactsTransformsToContactList() {
    List<Contact> testContacts = transformer().contacts(cdw.alivePatient().getContacts());
    List<Contact> expectedContacts = expectedPatient.contact();
    assertThat(testContacts).isEqualTo(expectedContacts);
  }

  @Test
  public void deceasedDateTimeMissingReturnsNull() {
    String testDateTime = Transformers.asDateTimeString(cdw.alivePatient().getDeceasedDateTime());
    assertThat(testDateTime).isNull();
  }

  @Test
  public void deceasedDateTimeTransformsToString() {
    String testDateTime = Transformers.asDateTimeString(cdw.deadPatient().getDeceasedDateTime());
    String expectedDateTime = expectedPatient.deceasedPatient().deceasedDateTime();
    assertThat(testDateTime).isEqualTo(expectedDateTime);
  }

  @Test
  public void extensionValueCodingTransformsToCoding() {
    Coding testCoding =
        transformer()
            .valueCoding(
                cdw.alivePatient().getArgoRace().get(0).getExtension().get(0).getValueCoding());
    Coding expectedCoding =
        expectedPatient.alivePatient().extension().get(0).extension().get(0).valueCoding();
    assertThat(testCoding).isEqualTo(expectedCoding);
  }

  @Test
  public void humanNameReturnsNullForNull() {
    assertThat(transformer().humanName(null)).isNull();
  }

  @Test
  public void identifierAssignerTransformsToReference() {
    Reference testReference =
        transformer().identifierAssigner(cdw.alivePatient().getIdentifier().get(0).getAssigner());
    Reference expectedReference = expectedPatient.alivePatient().identifier().get(0).assigner();
    assertThat(testReference).isEqualTo(expectedReference);
  }

  @Test
  public void identifierTransformsToIdentifierUse() {
    Identifier.IdentifierUse testIdentifierUse =
        transformer().identifierUse(cdw.alivePatient().getIdentifier().get(0));
    Identifier.IdentifierUse expectedIdentifierUse =
        expectedPatient.alivePatient().identifier().get(0).use();
    assertThat(testIdentifierUse).isEqualTo(expectedIdentifierUse);
  }

  @Test
  public void identifierTypeCodingListTransformsToCodingList() {
    List<Coding> testCodings =
        transformer()
            .identifierTypeCodings(cdw.alivePatient().getIdentifier().get(0).getType().getCoding());
    List<Coding> expectedCodings = expectedPatient.identifier().get(0).type().coding();
    assertThat(testCodings).isEqualTo(expectedCodings);
  }

  @Test
  public void identifierTypeTransfromsToCodeableConcept() {
    CodeableConcept testCodeableConcept =
        transformer().identifierType(cdw.alivePatient().getIdentifier().get(0).getType());
    CodeableConcept expectedCodeableConcept =
        expectedPatient.alivePatient().identifier().get(0).type();
    assertThat(testCodeableConcept).isEqualTo(expectedCodeableConcept);
  }

  @Test
  public void identifiersTransformsToIdentifiersList() {
    List<Identifier> testIdentifiers =
        transformer().identifiers(cdw.alivePatient().getIdentifier());
    List<Identifier> expectedIdentifiers = expectedPatient.alivePatient().identifier();
    assertThat(testIdentifiers).isEqualTo(expectedIdentifiers);
  }

  @Test
  public void maritalStatusCodingListTransformsToCodingList() {
    List<Coding> testCodingList =
        transformer().maritalStatusCoding(cdw.alivePatient().getMaritalStatus().getCoding());
    List<Coding> expectedCodingList = expectedPatient.alivePatient().maritalStatus().coding();
    assertThat(testCodingList).isEqualTo(expectedCodingList);
  }

  @Test
  public void maritalStatusReturnsNullForNull() {
    assertThat(transformer().maritalStatus(null)).isNull();
  }

  @Test
  public void maritalStatusTransformsToCodeableConcept() {
    CodeableConcept testCdwMaritalStatus = transformer().maritalStatus(cdw.maritalStatus());
    CodeableConcept expectedCdwMaritalStatus = expectedPatient.alivePatient().maritalStatus();
    assertThat(testCdwMaritalStatus).isEqualTo(expectedCdwMaritalStatus);
  }

  @Test
  public void namesReturnsEmptyForNull() {
    assertThat(transformer().names(null)).isEmpty();
  }

  @Test
  public void patient103TransformsToModelPatient() {
    gov.va.api.health.argonaut.api.Patient test = transformer().apply(cdw.alivePatient());
    gov.va.api.health.argonaut.api.Patient expected = expectedPatient.alivePatient();
    assertThat(test).isEqualTo(expected);
  }

  @Test
  public void patientStringTransformsToHumanName() {
    List<HumanName> testPatientName = transformer().names(cdw.alivePatient().getName());
    List<HumanName> expectedPatientName = expectedPatient.alivePatient().name();
    assertThat(testPatientName).isEqualTo(expectedPatientName);
  }

  @Test
  public void patientTelecomsTransformsToContactPointList() {
    List<ContactPoint> testTelecoms = transformer().telecoms(cdw.alivePatient().getTelecoms());
    List<ContactPoint> expectedTelecoms = expectedPatient.telecom();
    assertThat(testTelecoms).isEqualTo(expectedTelecoms);
  }

  @Test
  public void relationshipCodingTransformsToCodingList() {
    List<Coding> testCodings =
        transformer()
            .contactRelationshipCoding(
                cdw.alivePatient().getContacts().getContact().get(0).getRelationship().getCoding());
    List<Coding> expectedCodings = expectedPatient.contact().get(0).relationship().get(0).coding();
    assertThat(testCodings).isEqualTo(expectedCodings);
  }

  @Test
  public void telecomsReturnsEmptyForNull() {
    assertThat(transformer().telecoms(null)).isEmpty();
  }

  private PatientTransformer transformer() {
    return new PatientTransformer();
  }
}
