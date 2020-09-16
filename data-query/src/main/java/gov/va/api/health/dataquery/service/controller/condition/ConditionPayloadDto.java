package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.dataquery.service.controller.datamart.HasPayload;
import lombok.Value;

@Value
public class ConditionPayloadDto implements HasPayload<DatamartCondition> {
  String payload;

  @Override
  public Class<DatamartCondition> payloadType() {
    return DatamartCondition.class;
  }
}
