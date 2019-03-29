package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.dataquery.api.resources.DiagnosticReport;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(
  properties = {"spring.jpa.generate-ddl=true", "spring.jpa.hibernate.ddl-auto=create-drop"}
)
public class DiagnosticReportControllerJpaTest {
  @Autowired private TestEntityManager entityManager;

  @Test
  public void doIt() {
    String publicId = "34000989:L";
    String document = "<cdwId>34000989:L</cdwId>";
    DiagnosticReportEntity entity =
        DiagnosticReportEntity.builder().id(100).document(document).identifier(publicId).build();
    entityManager.persistAndFlush(entity);

    IdentityService identityService = mock(IdentityService.class);
    WitnessProtection witnessProtection =
        WitnessProtection.builder().identityService(identityService).build();
    DiagnosticReportController controller =
        new DiagnosticReportController(
            null,
            witnessProtection,
            entityManager.getEntityManager(),
            new DiagnosticReportTransformer(),
            null);

    DiagnosticReport report = controller.read("true", publicId);
    assertThat(report.id()).isEqualTo(publicId);
  }
}
