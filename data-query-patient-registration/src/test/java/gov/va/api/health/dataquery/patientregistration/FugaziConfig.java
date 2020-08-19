package gov.va.api.health.dataquery.patientregistration;

import gov.va.api.health.dataquery.patientregistration.PatientRegistration.Access;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Future;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncResult;

@Configuration
public class FugaziConfig {

  @Bean
  FilterRegistrationBean<PatientRegistrationFilter> patientRegistrationFilter() {
    var registration = new FilterRegistrationBean<PatientRegistrationFilter>();
    registration.setFilter(
        PatientRegistrationFilter.builder().registrar(new FugaziPatientRegistrar()).build());
    registration.addUrlPatterns("/fugazi/Patient/*");
    return registration;
  }

  private static class FugaziPatientRegistrar implements PatientRegistrar {

    @Override
    public Future<PatientRegistration> register(String icn) {
      return new AsyncResult<>(
          PatientRegistration.builder()
              .icn(icn)
              .access(
                  List.of(
                      Access.builder()
                          .application("fugazi")
                          .firstAccessTime(Instant.MIN)
                          .lastAccessTime(Instant.now())
                          .build()))
              .build());
    }
  }
}
