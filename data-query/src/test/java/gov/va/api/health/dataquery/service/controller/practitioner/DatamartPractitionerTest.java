package gov.va.api.health.dataquery.service.controller.practitioner;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Test;

import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner.Name;

public class DatamartPractitionerTest {
  @SneakyThrows
  public void assertReadable(String json) {
    DatamartPractitioner dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartPractitioner.class);
    assertThat(dm).isEqualTo(sample());
  }

  private DatamartPractitioner sample() {
    return DatamartPractitioner.builder()
        .cdwId("416704")
        .npi(Optional.of("1932127842"))
        .active(true)
        .name(
            Name.builder()
                .family("LASTNAME")
                .given("FIRSTNAME A.")
                .prefix(Optional.of("DR."))
                .suffix(Optional.of("PHD"))
                .build())
        .build();
  }

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
  //	}

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    assertReadable("datamart-practitioner.json");
  }
}
