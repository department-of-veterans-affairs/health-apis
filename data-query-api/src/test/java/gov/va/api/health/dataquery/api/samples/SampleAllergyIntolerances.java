package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Category;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Certainty;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Criticality;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Reaction;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Severity;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Status;
import gov.va.api.health.dataquery.api.resources.AllergyIntolerance.Type;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public class SampleAllergyIntolerances {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public AllergyIntolerance allergyIntolerance() {
    return AllergyIntolerance.builder()
        .id("2222")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello Language")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .identifier(singletonList(identifier()))
        .onset("2015-04-15T04:00:00Z")
        .recordedDate("2015-04-15T04:00:00Z")
        .recorder(reference())
        .patient(reference())
        .reporter(reference())
        .substance(codeableConcept())
        .status(Status.active)
        .criticality(Criticality.CRITL)
        .type(Type.allergy)
        .category(Category.food)
        .lastOccurence("2015-04-15T04:00:00Z")
        .note(annotation())
        .reaction(singletonList(reaction()))
        .build();
  }

  public Reaction reaction() {
    return Reaction.builder()
        .id("1111")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .substance(codeableConcept())
        .certainty(Certainty.unlikely)
        .manifestation(singletonList(codeableConcept()))
        .description("Test Reaction Description")
        .onset("2015-04-15T04:00:00Z")
        .severity(Severity.severe)
        .exposureRoute(codeableConcept())
        .note(annotation())
        .build();
  }
}
