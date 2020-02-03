package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartEntity;
import lombok.Value;

/**
 * A limited view of the Patient object that will just contain the payload data to be transformed to
 * a DatamartPatient.
 */
@Value
public class PatientPayloadDto {
  String payload;

  DatamartPatient asDatamartPatient() {
    return DatamartEntity.deserializeDatamart(payload, DatamartPatient.class);
  }
}
