package gov.va.api.health.dataquery.service.config;

import gov.va.api.lighthouse.talos.PathRewriteFilter;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Slf4j
@Configuration
public class PathRewriteConfig {

  static Set<String> leadingPaths() {
    return Set.of("/data-query/clinical/", "/data-query/", "/services/fhir/v0/", "/fhir/v0/");
  }

  @Bean
  FilterRegistrationBean<PathRewriteFilter> pathRewriteFilter() {
    var registration = new FilterRegistrationBean<PathRewriteFilter>();
    registration.setOrder(Ordered.LOWEST_PRECEDENCE);
    PathRewriteFilter filter =
        PathRewriteFilter.builder().removeLeadingPath(leadingPaths()).build();
    registration.setFilter(filter);
    registration.addUrlPatterns(filter.removeLeadingPathsAsUrlPatterns());
    log.info("PathRewriteFilter enabled with priority {}", registration.getOrder());
    return registration;
  }
}
