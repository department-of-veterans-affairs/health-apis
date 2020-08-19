package gov.va.api.health.dataquery.patientregistration;

import gov.va.api.health.dataquery.patientregistration.PatientRegistration.Access;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DynamoPatientRegistrar implements PatientRegistrar {

  @Override
  @Async
  public Future<PatientRegistration> register(String icn) {
    log.info("Registering {}", icn);
    var registration =
        PatientRegistration.builder()
            .icn(icn)
            .access(
                List.of(
                    Access.builder()
                        .application("fugazi") // TODO inject name
                        .firstAccessTime(Instant.MIN)
                        .lastAccessTime(Instant.now())
                        .build()))
            .build();
    registration.icn(icn);
    return new AsyncResult<>(registration);
  }
}
