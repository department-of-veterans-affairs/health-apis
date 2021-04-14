package gov.va.api.health.dataquery.service.config;

import gov.va.api.health.dataquery.patientregistration.PatientRegistrar;
import gov.va.api.health.dataquery.patientregistration.PatientRegistrationFilter;
import java.util.List;
import java.util.stream.Collectors;
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
    List<String> urlPatternsWithLeadingPaths =
        registration.getUrlPatterns().stream()
            .flatMap(
                pattern ->
                    PathRewriteConfig.leadingPaths().stream()
                        .map(
                            path ->
                                (path.endsWith("/") ? path.substring(0, path.length() - 1) : path)
                                    + pattern))
            .collect(Collectors.toList());
    urlPatternsWithLeadingPaths.forEach(registration::addUrlPatterns);
    return registration;
  }
}
