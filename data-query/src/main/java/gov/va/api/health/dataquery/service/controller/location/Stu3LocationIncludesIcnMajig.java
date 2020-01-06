package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.stu3.api.bundle.AbstractEntry;
import gov.va.api.health.stu3.api.resources.Location;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class Stu3LocationIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final IncludesIcnMajig<Location, Location.Bundle> delegate =
      IncludesIcnMajig.<Location, Location.Bundle>builder()
          .type(Location.class)
          .bundleType(Location.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.empty())
          .build();
}
