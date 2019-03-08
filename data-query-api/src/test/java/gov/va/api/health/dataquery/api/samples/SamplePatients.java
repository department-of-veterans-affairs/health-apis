package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.datatypes.Attachment;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.resources.Patient;
import gov.va.api.health.dataquery.api.resources.Patient.Communication;
import gov.va.api.health.dataquery.api.resources.Patient.Contact;
import gov.va.api.health.dataquery.api.resources.Patient.PatientLink;
import java.util.Arrays;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

/**
 * This class provides data structures that are populated with dummy values, suitable for testing
 * serialization.
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public class SamplePatients {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public Communication communication() {
    return Communication.builder()
        .id("8888")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .language(language())
        .preferred(false)
        .build();
  }

  Contact contact() {
    return Contact.builder()
        .id("0000")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .relationship(singletonList(relationship()))
        .name(humanName())
        .telecom(contactPointList())
        .address(address())
        .gender(Patient.Gender.unknown)
        .organization(reference())
        .period(period())
        .build();
  }

  public CodeableConcept language() {
    return CodeableConcept.builder().coding(singletonList(coding())).text("HelloText").build();
  }

  public Patient.PatientLink link() {
    return PatientLink.builder()
        .id("7777")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .other(reference())
        .type("HelloType")
        .build();
  }

  public CodeableConcept maritalStatus() {
    return CodeableConcept.builder().coding(singletonList(coding())).text("HelloText").build();
  }

  public Patient patient() {
    return Patient.builder()
        .id("1234")
        .resourceType("Patient")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello Language")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(Arrays.asList(extension(), extension()))
        .modifierExtension(
            Arrays.asList(extension(), extensionWithQuantity(), extensionWithRatio()))
        .identifier(singletonList(identifier()))
        .active(true)
        .name(singletonList(humanName()))
        .telecom(contactPointList())
        .gender(Patient.Gender.unknown)
        .birthDate("2000-01-01")
        .deceasedBoolean(false)
        .address(singletonList(address()))
        .maritalStatus(maritalStatus())
        .multipleBirthBoolean(false)
        .photo(singletonList(photo()))
        .contact(singletonList(contact()))
        .communication(singletonList(communication()))
        .careProvider(singletonList(reference()))
        .managingOrganization(reference())
        .link(singletonList(link()))
        .build();
  }

  public Attachment photo() {
    return Attachment.builder()
        .contentType("HelloType")
        .language("HelloLanguage")
        .data("SSBqdXN0IGF0ZSBhIHBlYW51dAo=")
        .url("http://HelloUrl.com")
        .size(1)
        .hash("SSBqdXN0IGF0ZSBhIHBlYW51dAo=")
        .title("HelloTitle")
        .creation("2000-01-01T00:00:00-00:00")
        .build();
  }

  public CodeableConcept relationship() {
    return CodeableConcept.builder().coding(singletonList(coding())).text("HelloText").build();
  }
}
