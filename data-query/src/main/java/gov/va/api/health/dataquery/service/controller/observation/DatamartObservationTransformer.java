package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.argonaut.api.resources.Observation;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class DatamartObservationTransformer {
  @NonNull final DatamartObservation datamart;

  Observation toFhir() {
    //    .status(status(cdw.getStatus()))
    //    .category(category(cdw.getCategory()))
    //    .code(code(cdw.getCode()))
    //    .subject(reference(cdw.getSubject()))
    //    .encounter(reference(cdw.getEncounter()))
    //    .effectiveDateTime(asDateTimeString(cdw.getEffectiveDateTime()))
    //    .issued(asDateTimeString(cdw.getIssued()))
    //    .performer(performers(cdw.getPerformers()))
    //    .valueQuantity(valueQuantity(cdw.getValueQuantity()))
    //    .valueCodeableConcept(valueCodeableConcept(cdw.getValueCodeableConcept()))
    //    .interpretation(interpretation(cdw.getInterpretation()))
    //    .comments(cdw.getComments())
    //    .referenceRange(referenceRanges(cdw.getReferenceRanges(), cdw.getCategory()))
    //    .component(components(cdw.getComponents()))

    /*
     * Specimen reference is omitted since we do not support the a specimen resource and
     * do not want dead links
     */
    return Observation.builder().resourceType("Observation").id(datamart.cdwId()).build();
  }
}
