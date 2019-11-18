package gov.va.api.health.dataquery.service.controller.location;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.Test;

public class DatamartLocationTest {
  private static DatamartLocation sample() {
    return DatamartLocation.builder().cdwId("561596:I").build();
  }

  @SneakyThrows
  private void assertReadable(String json) {
    DatamartLocation dm =
        createMapper().readValue(getClass().getResourceAsStream(json), DatamartLocation.class);
    assertThat(dm).isEqualTo(sample());

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

  }

  @Test
  public void lazy() {
    DatamartLocation dm = DatamartLocation.builder().build();
    //    assertThat(dm.agencyId()).isEqualTo(empty());
    //    assertThat(dm.ediId()).isEqualTo(empty());
    //    assertThat(dm.npi()).isEqualTo(empty());
    //    assertThat(dm.partOf()).isEqualTo(empty());
    //    assertThat(dm.providerId()).isEqualTo(empty());
    //    assertThat(dm.stationIdentifier()).isEqualTo(empty());
    //    assertThat(dm.telecom()).isEmpty();
    //    assertThat(dm.type()).isEqualTo(empty());
  }

  @Test
  public void unmarshalSample() {
    assertReadable("datamart-location.json");
  }
}
