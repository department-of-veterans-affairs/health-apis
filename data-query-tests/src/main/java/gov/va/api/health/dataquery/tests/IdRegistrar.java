package gov.va.api.health.dataquery.tests;

import gov.va.api.health.sentinel.ReducedSpamLogger;
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

  private TestIds registerCdwIds() {
    TestIds cdwIds = system().cdwIds();
    log.infoOnce("Registration not necessary");
    return cdwIds;
  }
}
