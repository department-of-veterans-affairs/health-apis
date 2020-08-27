package gov.va.api.health.dataquery.service.config;

import gov.va.api.health.dataquery.patientregistration.PatientRegistrar;
import gov.va.api.health.dataquery.patientregistration.PatientRegistrationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"gov.va.api.health.dataquery.patientregistration"})
public class PatientRegistrationConfig {
  @Bean
  FilterRegistrationBean<PatientRegistrationFilter> patientRegistrationFilter(
      @Autowired PatientRegistrar registrar) {
    var registration = new FilterRegistrationBean<PatientRegistrationFilter>();
    registration.setFilter(PatientRegistrationFilter.builder().registrar(registrar).build());
    registration.addUrlPatterns("/dstu2/Patient/*", "/stu3/Patient/*", "/r4/Patient/*");
    registration.addUrlPatterns("/dstu2/Patient", "/stu3/Patient", "/r4/Patient");
    return registration;
  }
}
