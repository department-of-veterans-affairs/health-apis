package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.stu3.api.resources.Location;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class LocationStu3Transformer {
  @NonNull private final DatamartLocation datamart;

  /** Convert the datamart structure to FHIR compliant structure. */
  public Location toFhir() {
    //   "status": "active",
    //   "name": "TEM MH PSO TRS IND93EH",
    //   "description": "BLDG 146, RM W02",
    //   "type": "PSYCHIATRY CLINIC",
    //   "telecom": "254-743-2867",
    //   "address": {
    //     "line1": "1901 VETERANS MEMORIAL DRIVE",
    //     "city": "TEMPLE",
    //     "state": "TEXAS",
    //     "postalCode": 76504
    //   },
    //   "physicalType": "BLDG 146, RM W02",
    //   "managingOrganization": {
    //     "reference": "390026:I",
    //     "display": "OLIN E. TEAGUE VET CENTER"
    //   }

    return Location.builder().resourceType("Location").id(datamart.cdwId()).build();
  }
}
