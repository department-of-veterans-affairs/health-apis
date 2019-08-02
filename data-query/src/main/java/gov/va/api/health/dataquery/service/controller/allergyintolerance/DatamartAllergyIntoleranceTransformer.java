package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class DatamartAllergyIntoleranceTransformer {
  @NonNull final DatamartAllergyIntolerance datamart;

  AllergyIntolerance toFhir() {
    //  .recordedDate(asDateTimeString(datamart.getRecordedDate()))
    //  .recorder(reference(datamart.getRecorder()))
    //  .substance(substance(datamart.getSubstance()))
    //  .patient(reference(datamart.getPatient()))
    //  .status(status(datamart.getStatus()))
    //  .criticality(criticality(datamart.getCriticality()))
    //  .type(type(datamart.getType()))
    //  .category(category(datamart.getCategory()))
    //  .note(note(datamart.getNotes()))
    //  .reaction(reaction(datamart.getReactions()))

    return AllergyIntolerance.builder()
        .id(datamart.cdwId())
        .resourceType("AllergyIntolerance")
        .build();
  }
}
