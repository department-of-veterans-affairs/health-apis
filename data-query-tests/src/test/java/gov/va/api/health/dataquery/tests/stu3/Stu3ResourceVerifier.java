package gov.va.api.health.dataquery.tests.stu3;

import gov.va.api.health.dataquery.tests.AbstractResourceVerifier;
import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.sentinel.TestClient;
import gov.va.api.health.stu3.api.bundle.AbstractBundle;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import java.util.Collections;
import java.util.Set;
import lombok.Getter;

public final class Stu3ResourceVerifier extends AbstractResourceVerifier {
  private static final Stu3ResourceVerifier INSTANCE = new Stu3ResourceVerifier();

  @Getter
  private final String apiPath = SystemDefinitions.systemDefinition().stu3DataQuery().apiPath();

  @Getter private final Class<?> bundleClass = AbstractBundle.class;

  /** STU-3 resources are Datamart-only. */
  @Getter private final Set<Class<?>> datamartAndCdwResources = Collections.emptySet();

  @Getter private final TestClient dataQuery = TestClients.stu3DataQuery();

  @Getter private final Class<?> operationOutcomeClass = OperationOutcome.class;

  private Stu3ResourceVerifier() {
    super();
  }

  public static Stu3ResourceVerifier get() {
    return INSTANCE;
  }
}
