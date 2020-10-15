package gov.va.api.health.dataquery.tests;

import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.sentinel.ReducedSpamLogger;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.slf4j.LoggerFactory;

/** ID Registrar will register CDW IDs with the Identity Service then publish the public UUIDs. */
@Value
@AllArgsConstructor(staticName = "of")
public final class IdRegistrar {
  private static final ReducedSpamLogger log =
      ReducedSpamLogger.builder().logger(LoggerFactory.getLogger(IdRegistrar.class)).build();

  SystemDefinition system;

  @Getter(lazy = true)
  TestIds registeredIds = registerCdwIds();

  private ResourceIdentity id(String type, String id) {
    return ResourceIdentity.builder().system("CDW").resource(type).identifier(id).build();
  }

  private TestIds registerCdwIds() {
    TestIds cdwIds = system().cdwIds();
    if (cdwIds.publicIds()) {
      log.infoOnce("Registration not necessary");
      return cdwIds;
    }

    ResourceIdentity allergyIntolerance = id("ALLERGY_INTOLERANCE", cdwIds.allergyIntolerance());
    ResourceIdentity condition = id("CONDITION", cdwIds.condition());
    ResourceIdentity diagnosticReport = id("DIAGNOSTIC_REPORT", cdwIds.diagnosticReport());
    ResourceIdentity immunization = id("IMMUNIZATION", cdwIds.immunization());
    ResourceIdentity location = id("LOCATION", cdwIds.location());
    ResourceIdentity medication = id("MEDICATION", cdwIds.medication());
    ResourceIdentity medicationOrder = id("MEDICATION_ORDER", cdwIds.medicationOrder());
    ResourceIdentity medicationStatement = id("MEDICATION_STATEMENT", cdwIds.medicationStatement());
    ResourceIdentity observation = id("OBSERVATION", cdwIds.observation());
    ResourceIdentity organizataion = id("ORGANIZATION", cdwIds.organization());
    ResourceIdentity patient = id("PATIENT", cdwIds.patient());
    ResourceIdentity practitioner = id("PRACTITIONER", cdwIds.practitioner());
    ResourceIdentity procedure = id("PROCEDURE", cdwIds.procedure());

    List<ResourceIdentity> identities =
        Arrays.asList(
            allergyIntolerance,
            condition,
            diagnosticReport,
            immunization,
            location,
            patient,
            medication,
            medicationOrder,
            medicationStatement,
            observation,
            organizataion,
            practitioner,
            procedure);
    log.info("Registering {}", identities);
    return null;
  }
}
