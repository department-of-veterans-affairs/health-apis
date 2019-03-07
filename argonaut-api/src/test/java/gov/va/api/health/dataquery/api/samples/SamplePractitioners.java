package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.resources.Practitioner;
import gov.va.api.health.dataquery.api.resources.Practitioner.Gender;
import gov.va.api.health.dataquery.api.resources.Practitioner.PractitionerRole;
import gov.va.api.health.dataquery.api.resources.Practitioner.Qualification;
import java.util.Collections;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@NoArgsConstructor(staticName = "get")
public class SamplePractitioners {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public Practitioner practitioner() {
    return Practitioner.builder()
        .resourceType("Practitioner")
        .id("2222")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello Language")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .identifier(singletonList(identifier()))
        .active(true)
        .name(dataTypes.humanName())
        .telecom(Collections.singletonList(dataTypes.contactPoint()))
        .address(Collections.singletonList(dataTypes.address()))
        .gender(Gender.male)
        .birthDate("2000-10-01")
        .photo(Collections.singletonList(dataTypes.attachment()))
        .practitionerRole(
            Collections.singletonList(
                PractitionerRole.builder()
                    .managingOrganization(dataTypes.reference())
                    .role(dataTypes.codeableConcept())
                    .specialty(dataTypes.codeableConceptList())
                    .period(dataTypes.period())
                    .location(dataTypes.referenceList())
                    .healthcareService(dataTypes.referenceList())
                    .build()))
        .qualification(
            Collections.singletonList(
                Qualification.builder()
                    .identifier(Collections.singletonList(dataTypes.identifier()))
                    .code(dataTypes.codeableConcept())
                    .period(dataTypes.period())
                    .issuer(dataTypes.reference())
                    .build()))
        .communication(dataTypes.codeableConceptList())
        .build();
  }
}
