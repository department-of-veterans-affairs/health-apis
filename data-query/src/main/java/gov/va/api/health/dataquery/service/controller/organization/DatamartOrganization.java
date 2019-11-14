package gov.va.api.health.dataquery.service.controller.organization;

import java.util.Optional;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
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
  /** Lazy initialization. */
  public Optional<String> stationIdentifier() {
    if (stationIdentifier == null) {
      stationIdentifier = Optional.empty();
    }
    return stationIdentifier;
  }

  private Optional<String> npi;
  /** Lazy initialization. */
  public Optional<String> npi() {
    if (npi == null) {
      npi = Optional.empty();
    }
    return npi;
  }

  private Optional<String> providerId;
  /** Lazy initialization. */
  public Optional<String> providerId() {
    if (providerId == null) {
      providerId = Optional.empty();
    }
    return providerId;
  }

  private Optional<String> ediId;
  /** Lazy initialization. */
  public Optional<String> ediId() {
    if (ediId == null) {
      ediId = Optional.empty();
    }
    return ediId;
  }

  private Optional<String> agencyId;
  /** Lazy initialization. */
  public Optional<String> agencyId() {
    if (agencyId == null) {
      agencyId = Optional.empty();
    }
    return agencyId;
  }

  private Boolean active;

  private Optional<DatamartCoding> type;
  /** Lazy initialization. */
  public Optional<DatamartCoding> type() {
    if (type == null) {
      type = Optional.empty();
    }
    return type;
  }

  private String name;
}

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
