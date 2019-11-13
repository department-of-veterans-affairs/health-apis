package gov.va.api.health.dataquery.service.controller.practitioner;

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
public class DatamartPractitioner implements HasReplaceableId {
  @Builder.Default private String objectType = "Practitioner";
  @Builder.Default private int objectVersion = 1;

  private String cdwId;
  
  private String npi;
  
  private Boolean active;
}

//	  "name": {
//	    "family": "LASTNAME",
//	    "given": "FIRSTNAME A.",
//	    "prefix": "DR.",
//	    "suffix": "PHD"
//	  },
//	  "telecom": [{
//	      "system": "phone",
//	      "value": "555-555-1137",
//	      "use": "work"
//	    }, {
//	      "system": "phone",
//	      "value": "555-4055",
//	      "use": "home"
//	    }, {
//	      "system": "pager",
//	      "value": "5-541",
//	      "use": "mobile"
//	    }
//	  ],
//	  "address": [{
//	      "temp": false,
//	      "line1": "555 E 5TH ST",
//	      "line2": "SUITE B",
//	      "line3": null,
//	      "city": "CHEYENNE",
//	      "state": "WYOMING",
//	      "postalCode": "82001"
//	    }
//	  ],
//	  "gender": "female",
//	  "birthDate": "1965-03-16",
//	  "practitionerRole": {
//	    "managingOrganization": {
//	      "reference": "561596:I",
//	      "display": "CHEYENNE VA MEDICAL"
//	    },
//	    "role": {
//	      "system": "rpcmm",
//	      "code": "37",
//	      "display": "PSYCHOLOGIST"
//	    },
//	    "specialty": [{
//	        "providerType": "Physicians (M.D. and D.O.)",
//	        "classification": "Physician\/Osteopath",
//	        "areaOfSpecialization": "Internal Medicine",
//	        "vaCode": "V111500",
//	        "x12Code": null
//	      }, {
//	        "providerType": "Physicians (M.D. and D.O.)",
//	        "classification": "Physician\/Osteopath",
//	        "areaOfSpecialization": "General Practice",
//	        "vaCode": "V111000",
//	        "x12Code": null
//	      }, {
//	        "providerType": "Physicians (M.D. and D.O.)",
//	        "classification": "Physician\/Osteopath",
//	        "areaOfSpecialization": "Family Practice",
//	        "vaCode": "V110900",
//	        "x12Code": null
//	      }, {
//	        "providerType": "Allopathic & Osteopathic Physicians",
//	        "classification": "Family Medicine",
//	        "areaOfSpecialization": null,
//	        "vaCode": "V180700",
//	        "x12Code": "207Q00000X"
//	      }
//	    ],
//	    "period": {
//	      "start": "1988-08-19",
//	      "end": null
//	    },
//	    "location": [{
//	        "reference": "43817:L",
//	        "display": "CHEY MEDICAL"
//	      }, {
//	        "reference": "43829:L",
//	        "display": "ZZCHY LASTNAME MEDICAL"
//	      }, {
//	        "reference": "43841:L",
//	        "display": "ZZCHY WID BACK"
//	      }
//	    ],
//	    "healthCareService": "MEDICAL SERVICE"
//	  }
