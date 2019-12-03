package gov.va.api.health.dataquery.tools;

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
import gov.va.api.health.dataquery.service.controller.patient.PatientEntity;
import gov.va.api.health.dataquery.service.controller.patient.PatientSearchEntity;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureEntity;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
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
  private static final List<Class<?>> MANAGED_CLASSES =
      Arrays.asList(
          AllergyIntoleranceEntity.class,
          ConditionEntity.class,
          DiagnosticReportCrossEntity.class,
          DiagnosticReportsEntity.class,
          ImmunizationEntity.class,
          LocationEntity.class,
          MedicationOrderEntity.class,
          MedicationEntity.class,
          MedicationStatementEntity.class,
          ObservationEntity.class,
          PatientEntity.class,
          PatientSearchEntity.class,
          ProcedureEntity.class
          //
          );

  EntityManager h2;
  EntityManager mitre;

  public DatamartExporter(String configFile, String outputFile) {
    mitre = new ExternalDb(configFile, MANAGED_CLASSES).get();
    h2 = new LocalH2(outputFile, MANAGED_CLASSES).get();
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

  public void export() {
    h2.getTransaction().begin();
    MANAGED_CLASSES.stream().forEach(this::steal);
    h2.getTransaction().commit();
    mitre.close();
    h2.close();
  }

  private <T> void steal(Class<T> type) {
    log.info("Stealing {}", type);
    mitre
        .createQuery("select e from " + type.getSimpleName() + " e", type)
        .getResultStream()
        .forEach(
            e -> {
              mitre.detach(e);
              log.info("{}", e);
              h2.persist(e);
            });
  }
}
