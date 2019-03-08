package gov.va.api.health.dataquery.service.healthcheck;

import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient.MrAndersonServiceException;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@AllArgsConstructor(onConstructor = @__({@Autowired}))
@Component
@Slf4j
public class SteelThreadSystemCheck implements HealthIndicator {
  private final MrAndersonClient client;

  @Value("${health-check.medication-id}")
  private final String id;

  @Override
  @SneakyThrows
  public Health health() {
    if ("skip".equals(id)) {
      return Health.up().withDetail("skipped", true).build();
    }
    try {
      client.search(query());
      return Health.up().build();
    } catch (HttpServerErrorException
        | HttpClientErrorException
        | ResourceAccessException
        | MrAndersonServiceException e) {
      return Health.down()
          .withDetail("exception", e.getClass())
          .withDetail("message", e.getMessage())
          .build();
    } catch (Exception e) {
      log.error("Failed to complete health check.", e);
      throw e;
    }
  }

  private Query<CdwMedication101Root> query() {
    return Query.forType(CdwMedication101Root.class)
        .resource("Medication")
        .profile(Query.Profile.ARGONAUT)
        .version("1.01")
        .parameters(Parameters.forIdentity(id))
        .build();
  }
}
