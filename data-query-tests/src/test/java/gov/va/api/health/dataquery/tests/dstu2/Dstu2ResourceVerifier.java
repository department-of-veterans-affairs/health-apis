package gov.va.api.health.dataquery.tests.dstu2;

import com.google.common.collect.ImmutableSet;
import gov.va.api.health.dataquery.tests.AbstractResourceVerifier;
import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.dataquery.tests.TestClients;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.resources.Location;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Practitioner;
import gov.va.api.health.sentinel.TestClient;
import java.util.Set;
import lombok.Getter;

public final class Dstu2ResourceVerifier extends AbstractResourceVerifier {
  private static final Dstu2ResourceVerifier INSTANCE = new Dstu2ResourceVerifier();

  @Getter
  private final String apiPath = SystemDefinitions.systemDefinition().dstu2DataQuery().apiPath();

  @Getter private final Class<?> bundleClass = AbstractBundle.class;

  /**
   * As remaining resources are migrated from CDW to Datamart, they may support both at the same
   * time. Once resources are fully migrated over, they can be removed from this list.
   */
  @Getter
  private final Set<Class<?>> datamartAndCdwResources =
      ImmutableSet.of(Location.class, Practitioner.class);

  @Getter private final TestClient dataQuery = TestClients.dstu2DataQuery();

  @Getter private final Class<?> operationOutcomeClass = OperationOutcome.class;

  private Dstu2ResourceVerifier() {
    super();
  }

  public static Dstu2ResourceVerifier get() {
    return INSTANCE;
  }
}
