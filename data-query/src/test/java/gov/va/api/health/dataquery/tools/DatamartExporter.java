package gov.va.api.health.dataquery.tools;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.allergyintolerance.AllergyIntoleranceEntity;
import gov.va.api.health.dataquery.service.controller.condition.ConditionEntity;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportCrossEntity;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportsEntity;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationEntity;
import gov.va.api.health.dataquery.service.controller.location.LocationEntity;
import gov.va.api.health.dataquery.service.controller.medication.MedicationEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementEntity;
import gov.va.api.health.dataquery.service.controller.observation.ObservationEntity;
import gov.va.api.health.dataquery.service.controller.organization.OrganizationEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientSearchEntity;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureEntity;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * This application will copy data out of the Mitre database into a local H2 database. It expects
 * that you will have ./config/lab.properties with Mitre database credentials using standard Spring
 * properties.
 *
 * <p>This test will re-write src/test/resources/mitre, the local H2 mitre database used by
 * integration tests.
 */
@Slf4j
public class DatamartExporter {

  /** Add classes to this list to copy them from Mitre to H2 */
  private static final List<ExportCriteria> EXPORT_CRITERIA =
      Arrays.asList(
          ExportForPatientCriteria.of(AllergyIntoleranceEntity.class),
          ExportForPatientCriteria.of(ConditionEntity.class),
          ExportForPatientCriteria.of(DiagnosticReportCrossEntity.class),
          ExportForPatientCriteria.of(DiagnosticReportsEntity.class),
          ExportForPatientCriteria.of(ImmunizationEntity.class),
          ExportAllCriteria.of(LocationEntity.class),
          ExportForPatientCriteria.of(MedicationOrderEntity.class),
          ExportAllCriteria.of(MedicationEntity.class),
          ExportForPatientCriteria.of(MedicationStatementEntity.class),
          ExportForPatientCriteria.of(ObservationEntity.class),
          ExportAllCriteria.of(OrganizationEntity.class),
          ExportForPatientCriteria.of(PatientEntity.class),
          ExportForPatientCriteria.of(PatientSearchEntity.class),
          ExportAllCriteria.of(PractitionerEntity.class),
          ExportForPatientCriteria.of(ProcedureEntity.class));

  EntityManager h2;

  EntityManager mitre;

  public DatamartExporter(String configFile, String outputFile) {
    mitre = new ExternalDb(configFile, managedClasses()).get();
    h2 = new LocalH2(outputFile, managedClasses()).get();
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      log.error("DatamartExporter <application.properties> <h2-database>");
      throw new RuntimeException("Missing arguments");
    }
    String configFile = args[0];
    String outputFile = args[1];
    new DatamartExporter(configFile, outputFile).export();
    log.info("All done");
    System.exit(0);
  }

  private static List<Class<?>> managedClasses() {
    return EXPORT_CRITERIA.stream().map(ExportCriteria::type).collect(Collectors.toList());
  }

  public void export() {
    EXPORT_CRITERIA.stream().forEach(this::steal);
    mitre.close();
    h2.close();
  }

  private void steal(ExportCriteria criteria) {
    log.info("Stealing {}", criteria.type());
    h2.getTransaction().begin();
    criteria
        .queries()
        .forEach(
            query -> {
              mitre
                  .createQuery(query, criteria.type())
                  .getResultStream()
                  .forEach(
                      e -> {
                        mitre.detach(e);
                        log.info("{}", e);
                        h2.persist(e);
                      });
            });
    h2.getTransaction().commit();
  }

  private interface ExportCriteria {

    Stream<String> queries();

    Class<?> type();
  }

  @Value
  @AllArgsConstructor(staticName = "of")
  private static class ExportAllCriteria implements ExportCriteria {

    Class<?> type;

    @Override
    public Stream<String> queries() {
      return Stream.of("select e from " + type.getSimpleName() + " e");
    }
  }

  @Value
  @AllArgsConstructor(staticName = "of")
  private static class ExportForPatientCriteria implements ExportCriteria {

    Class<?> type;

    @Override
    public Stream<String> queries() {
      String patientsCsv = System.getProperty("exportPatients");
      assertThat(patientsCsv)
          .withFailMessage("System property %s must be specified.", "exportPatients")
          .isNotBlank();
      return Stream.of(patientsCsv.split(" *, *"))
          .map(icn -> "select e from " + type.getSimpleName() + " e where e.icn = '" + icn + "'");
    }
  }
}
