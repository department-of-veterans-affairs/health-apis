package gov.va.api.health.dataquery.patientregistration;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientRegistration {
  private String icn;
  private List<Access> access;

  @Data
  @Builder
  public static class Access {
    private String application;
    private Instant firstAccessTime;
    private Instant lastAccessTime;
  }
}
