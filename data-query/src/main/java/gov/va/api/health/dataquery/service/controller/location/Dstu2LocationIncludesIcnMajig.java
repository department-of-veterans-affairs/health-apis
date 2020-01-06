package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.resources.Location;
import lombok.experimental.Delegate;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;

import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class Dstu2LocationIncludesIcnMajig {
  @Delegate
  private final IncludesIcnMajig delegate =
      IncludesIcnMajig.builder()
          .type(Location.class)
          .bundleType(Location.Bundle.class)
          .extractResources(
              bundle -> ((Location.Bundle) bundle).entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.empty())
          .build();
}
