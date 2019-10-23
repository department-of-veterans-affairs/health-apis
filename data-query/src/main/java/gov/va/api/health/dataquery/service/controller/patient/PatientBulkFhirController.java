package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
  value = {"/internal/bulk/Patient"},
  produces = {"application/json", "application/fhir+json", "application/json+fhir"}
)
public class PatientBulkFhirController {
  private PatientRepository repository;
  private int maxPageSize;

  /** All args constructor. */
  public PatientBulkFhirController(
      @Value("${bulk.patient.maxPageSize}") int maxPageSize,
      @Autowired PatientRepository repository) {
    this.maxPageSize = maxPageSize;
    this.repository = repository;
  }

  /** Count by icn. */
  @GetMapping("/count")
  public BulkFhirCount patientCount() {
    return BulkFhirCount.builder()
        .resourceType("Patient")
        .count(repository.count())
        .maxPageSize(maxPageSize)
        .build();
  }
}
