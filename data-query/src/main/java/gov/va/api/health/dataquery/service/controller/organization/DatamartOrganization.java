package gov.va.api.health.dataquery.service.controller.organization;

import java.util.Optional;

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
public final class DatamartOrganization implements HasReplaceableId {
  @Builder.Default private String objectType = "Organization";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  private Optional<String> stationIdentifier;

  private Optional<String> npi;
}

// {
//	  "providerId" : "0040000000000",
//	  "ediId" : "36273",
//	  "agencyId" : "other",
//	  "active" : true,
//	  "type" : {
//	    "system" : "institution",
//	    "code" : "CBOC",
//	    "display" : "COMMUNITY BASED OUTPATIENT CLINIC"
//	  },
//	  "name" : "NEW AMSTERDAM CBOC",
//	  "telecom" : [
//	    {
//	      "system" : "phone",
//	      "value" : "800 555-7710"
//	    },
//	    {
//	      "system" : "phone",
//	      "value" : "800 555-7720"
//	    },
//	    {
//	      "system" : "phone",
//	      "value" : "800-555-7730"
//	    }
//	  ],
//	  "address" : {
//	    "line1" : "10 MONROE AVE, SUITE 6B",
//	    "lne2" : "PO BOX 4160",
//	    "city" : "NEW AMSTERDAM",
//	    "state" : "OH",
//	    "postalCode" : "44444-4160"
//	  },
//	  "partOf" : {
//	    "reference" : "568060:I",
//	    "display" : "NEW AMSTERDAM VAMC"
//	  }
//	}
