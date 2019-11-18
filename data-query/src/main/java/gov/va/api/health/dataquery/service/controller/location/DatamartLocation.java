package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatamartLocation implements HasReplaceableId {
  @Builder.Default private String objectType = "Location";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  //  private Optional<DatamartReference> performer;
  //
  //  /** Lazy initialization with empty. */
  //  public Optional<DatamartReference> encounter() {
  //    if (encounter == null) {
  //      encounter = Optional.empty();
  //    }
  //    return encounter;
  //  }
  //
  //  @Data
  //  @Builder
  //  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  //  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  //  public static class VaccineCode {
  //
  //    private String text;
  //
  //    private String code;
  //  }
}

//	  "objectType" : "Location",
//	  "objectVersion" : "1",
//	  "cdwId" : "1000200441:L",
//	  "status" : "active",
//	  "name" : "TEM MH PSO TRS IND93EH",
//	  "description" : "BLDG 146, RM W02",
//	  "type" : "PSYCHIATRY CLINIC",
//	  "telecom" : "254-743-2867",
//	  "address" : {
//	    "line1" : "1901 VETERANS MEMORIAL DRIVE",
//	    "city" : "TEMPLE",
//	    "state" : "TEXAS",
//	    "postalCode" : 76504
//	  },
//	  "physicalType" : "BLDG 146, RM W02",
//	  "managingOrganization" : {
//	    "reference" : "390026:I",
//	    "display" : "OLIN E. TEAGUE VET CENTER"
//	  }

