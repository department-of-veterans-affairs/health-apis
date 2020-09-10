package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.datamart.HasPayload;
import lombok.SneakyThrows;
import lombok.Value;

/**
 * A limited view of the Patient object that will just contain the payload data to be transformed to
 * a DatamartPatient.
 */
@Value
public class PatientPayloadDto implements HasPayload<DatamartPatient> {
  String payload;

  @SneakyThrows
  @Deprecated
  public DatamartPatient asDatamartPatient() {
    return JacksonConfig.createMapper().readValue(payload, DatamartPatient.class);
  }

  @Override
  public Class<DatamartPatient> payloadType() {
    return DatamartPatient.class;
  }
}
