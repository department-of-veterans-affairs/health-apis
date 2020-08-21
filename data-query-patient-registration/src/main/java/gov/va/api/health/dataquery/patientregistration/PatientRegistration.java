package gov.va.api.health.dataquery.patientregistration;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientRegistration {
  private String icn;
  private String application;
  private Instant firstAccessTime;
  private Instant lastAccessTime;
}
