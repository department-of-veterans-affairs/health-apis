package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.dataquery.service.controller.datamart.HasPayload;
import lombok.Value;

/** A limited view of the Observation object that will just contain the payload data. */
@Value
public class ObservationPayloadDto implements HasPayload<DatamartObservation> {
  String payload;

  @Override
  public Class<DatamartObservation> payloadType() {
    return DatamartObservation.class;
  }
}
