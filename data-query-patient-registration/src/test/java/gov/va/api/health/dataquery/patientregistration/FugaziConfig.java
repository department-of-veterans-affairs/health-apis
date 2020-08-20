package gov.va.api.health.dataquery.patientregistration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FugaziConfig {

  @Bean
  FilterRegistrationBean<PatientRegistrationFilter> patientRegistrationFilter() {
    var registration = new FilterRegistrationBean<PatientRegistrationFilter>();
    registration.setFilter(
        PatientRegistrationFilter.builder()
            .registrar(new NoopPatientRegistrarConfig().noopPatientRegistrar())
            .build());
    registration.addUrlPatterns("/fugazi/Patient/*");
    return registration;
  }
}
