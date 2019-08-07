package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.argonaut.api.resources.Observation;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartObservationSamples {
  @AllArgsConstructor(staticName = "create")
  static class Datamart {
    public DatamartObservation observation() {
      return DatamartObservation.builder().build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {
    static final String ID = "2b45ed16-3d77-45b0-b540-928605528ef0";

    public Observation observation() {
      return Observation.builder().resourceType("Observation").id(ID).build();
    }
  }
}
