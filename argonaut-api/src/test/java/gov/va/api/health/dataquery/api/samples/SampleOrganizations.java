package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.resources.Organization;
import gov.va.api.health.dataquery.api.resources.Organization.OrganizationContact;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@NoArgsConstructor(staticName = "get")
public class SampleOrganizations {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public Organization organization() {
    return Organization.builder()
        .resourceType("Organization")
        .id("2222")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello language")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .identifier(singletonList(identifier()))
        .active(true)
        .type(codeableConcept())
        .name("Hello Name")
        .telecom(singletonList(contactPoint()))
        .address(singletonList(address()))
        .partOf(reference())
        .contact(
            singletonList(
                OrganizationContact.builder()
                    .purpose(codeableConcept())
                    .name(humanName())
                    .telecom(singletonList(contactPoint()))
                    .address(address())
                    .build()))
        .build();
  }
}
