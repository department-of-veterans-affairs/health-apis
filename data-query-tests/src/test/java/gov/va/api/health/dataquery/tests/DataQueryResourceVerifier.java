package gov.va.api.health.dataquery.tests;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/** This support class can be used to test standard resource queries, such as reads and searches. */
@Slf4j
@UtilityClass
public final class DataQueryResourceVerifier {
  static {
    log.info(
        "Datamart failures enabled: {} "
            + "(Override using -Ddatamart.failures.enabled=<true|false> "
            + "or environment variable DATAMART_FAILURES_ENABLED=<true|false>)",
        datamartFailuresEnabled());
  }

  /**
   * Datamart is not quite stable enough to prohibit builds from passing. Since this feature is
   * toggled off, we'll allow Datamart failures anywhere but locally.
   */
  private static boolean datamartFailuresEnabled() {
    if (Environment.get() == Environment.LOCAL) {
      return true;
    }
    if (isTrue(toBoolean(System.getProperty("datamart.failures.enabled")))) {
      return true;
    }
    if (isTrue(toBoolean(System.getenv("DATAMART_FAILURES_ENABLED")))) {
      return true;
    }
    return false;
  }

  public static ResourceVerifier dstu2() {
    /*
     * As remaining resources are migrated from CDW to Datamart, they may support both at the same
     * time. Once resources are fully migrated over, they can be removed from datamartAndCdwResources.
     */
    return ResourceVerifier.builder()
        .apiPath(SystemDefinitions.systemDefinition().dstu2DataQuery().apiPath())
        .bundleClass(gov.va.api.health.dstu2.api.bundle.AbstractBundle.class)
        .testClient(TestClients.dstu2DataQuery())
        .operationOutcomeClass(gov.va.api.health.dstu2.api.resources.OperationOutcome.class)
        .build();
  }

  public static TestIds ids() {
    return SystemDefinitions.systemDefinition().publicIds();
  }

  public static ResourceVerifier r4() {
    return ResourceVerifier.builder()
        .apiPath(SystemDefinitions.systemDefinition().r4DataQuery().apiPath())
        .bundleClass(gov.va.api.health.r4.api.bundle.AbstractBundle.class)
        .testClient(TestClients.r4DataQuery())
        .operationOutcomeClass(gov.va.api.health.r4.api.resources.OperationOutcome.class)
        .build();
  }

  public static ResourceVerifier stu3() {
    return ResourceVerifier.builder()
        .apiPath(SystemDefinitions.systemDefinition().stu3DataQuery().apiPath())
        .bundleClass(gov.va.api.health.stu3.api.bundle.AbstractBundle.class)
        .testClient(TestClients.stu3DataQuery())
        .operationOutcomeClass(gov.va.api.health.stu3.api.resources.OperationOutcome.class)
        .build();
  }
}
