package gov.va.api.health.dataquery.service.controller.patient;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
  value = {"/internal/bulk/Patient/count"},
  produces = {"application/json"}
)
public class PatientBulkFhirController {
  private EntityManager entityManager;
  private int maxPageSize;

  /** All args constructor. */
  public PatientBulkFhirController(
      @Value("${bulk.patient.maxPageSize}") int maxPageSize,
      @Autowired EntityManager entityManager) {
    this.maxPageSize = maxPageSize;
    this.entityManager = entityManager;
  }

  /** Count by icn. */
  @GetMapping
  public BulkFhirCount patientCount() {
    TypedQuery<Long> query =
        entityManager.createQuery("Select count(p.icn) from PatientEntity p", Long.class);
    int count = query.getSingleResult().intValue();
    return BulkFhirCount.builder()
        .resourceType("Patient")
        .count(count)
        .maxPageSize(maxPageSize)
        .build();
  }
}
