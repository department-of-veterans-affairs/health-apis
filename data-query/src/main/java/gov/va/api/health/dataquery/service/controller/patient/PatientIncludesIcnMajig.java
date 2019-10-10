package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.argonaut.api.resources.Patient.Bundle;
import gov.va.api.health.argonaut.api.resources.Patient.Entry;
import gov.va.api.health.dataquery.service.controller.AbstractIncludesIcnMajig;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class PatientIncludesIcnMajig extends AbstractIncludesIcnMajig<Patient, Entry, Bundle> {

  public PatientIncludesIcnMajig() {
    super(Patient.class, Bundle.class, (body) -> Stream.of(body.id()));
  }
}
