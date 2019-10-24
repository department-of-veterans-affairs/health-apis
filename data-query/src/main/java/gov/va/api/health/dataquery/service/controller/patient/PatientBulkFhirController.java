package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import gov.va.api.health.dataquery.service.controller.PageAndCountValidator;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Slf4j
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
  value = {"/internal/bulk/Patient"},
  produces = {"application/json", "application/fhir+json", "application/json+fhir"}
)
public class PatientBulkFhirController {

  private PatientRepository repository;

  private int maxRecordsPerPage;

  /** All args constructor. */
  public PatientBulkFhirController(
      @Value("${bulk.patient.maxRecordsPerPage}") int maxRecordsPerPage,
      @Autowired PatientRepository repository) {
    this.maxRecordsPerPage = maxRecordsPerPage;
    this.repository = repository;
  }

  private List<Patient> export(int page, int count) {
    Page<PatientEntity> entities = repository.findAll(page(page, count));
    return entities
        .stream()
        .map(PatientEntity::asDatamartPatient)
        .map(dm -> DatamartPatientTransformer.builder().datamart(dm).build().toFhir())
        .collect(Collectors.toList());
  }

  private PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count);
  }

  /** Count by icn. */
  @GetMapping("/count")
  public BulkFhirCount patientCount() {
    return BulkFhirCount.builder()
        .resourceType("Patient")
        .count(repository.count())
        .maxRecordsPerPage(maxRecordsPerPage)
        .build();
  }

  /** Export patient records via Page/Count. */
  @GetMapping()
  public List<Patient> patientExport(
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    PageAndCountValidator.validateCountBounds(count, maxRecordsPerPage);
    return export(page, count);
  }
}
