package gov.va.api.health.dataquery.patientregistration;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/** Details about a patient's registration. */
@Data
@Builder
public class PatientRegistration {
  private String icn;
  private String application;
  private Instant firstAccessTime;
  private Instant lastAccessTime;
}
