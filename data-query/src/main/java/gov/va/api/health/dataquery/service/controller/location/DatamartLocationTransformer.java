package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.dstu2.api.resources.Location;
import lombok.Builder;

@Builder
final class DatamartLocationTransformer {
  private final DatamartLocation datamart;

  /** Convert the datamart structure to FHIR compliant structure. */
  public Location toFhir() {
    //     return Condition.builder()
    //         .resourceType("Condition")
    //         .abatementDateTime(asDateTimeString(datamart.abatementDateTime()))
    //         .asserter(asReference(datamart.asserter()))
    //         .category(category(datamart.category()))
    //         .id(datamart.cdwId())
    //         .clinicalStatus(clinicalStatusCode(datamart.clinicalStatus()))
    //         .code(bestCode())
    //         .dateRecorded(asDateString(datamart.dateRecorded()))
    //         .encounter(asReference(datamart.encounter()))
    //         .onsetDateTime(asDateTimeString(datamart.onsetDateTime()))
    //         .patient(asReference(datamart.patient()))
    //         .verificationStatus(VerificationStatusCode.unknown)
    //         .build();

    return null;
  }
}
