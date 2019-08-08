package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class DatamartObservationTransformer {
  @NonNull final DatamartObservation datamart;

  private static Observation.Status status(DatamartObservation.Status status) {
    if (status == null) {
      return null;
    }
    return EnumSearcher.of(Observation.Status.class).find(status.toString());
  }

  Observation toFhir() {
    //    .category(category(cdw.Category()))
    //    .code(code(cdw.Code()))
    //    .subject(reference(cdw.Subject()))
    //    .encounter(reference(cdw.Encounter()))
    //    .effectiveDateTime(asDateTimeString(cdw.EffectiveDateTime()))
    //    .issued(asDateTimeString(cdw.Issued()))
    //    .performer(performers(cdw.Performers()))
    //    .valueQuantity(valueQuantity(cdw.ValueQuantity()))
    //    .valueCodeableConcept(valueCodeableConcept(cdw.ValueCodeableConcept()))
    //    .interpretation(interpretation(cdw.Interpretation()))
    //    .comments(cdw.Comments())
    //    .referenceRange(referenceRanges(cdw.ReferenceRanges(), cdw.Category()))
    //    .component(components(cdw.Components()))

    /*
     * Specimen reference is omitted since we do not support the a specimen resource and
     * do not want dead links
     */
    return Observation.builder()
        .resourceType("Observation")
        .id(datamart.cdwId())
        .status(status(datamart.status()))
        .build();
  }
}
