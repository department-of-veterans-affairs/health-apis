package gov.va.api.health.dataquery.service.controller.location;

import gov.va.api.health.stu3.api.resources.Location;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.Stu3IncludesIcnMajig;

import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class Stu3LocationIncludesIcnMajig extends AbstractIncludesIcnMajig {
  public Stu3LocationIncludesIcnMajig() {
    super(
        new Stu3IncludesIcnMajig<>(
            Location.class, Location.Bundle.class, (body) -> Stream.empty()));
  }
}
