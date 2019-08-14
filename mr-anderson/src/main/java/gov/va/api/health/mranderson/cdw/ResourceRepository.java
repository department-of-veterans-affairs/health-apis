package gov.va.api.health.mranderson.cdw;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.stereotype.Component;

/** The Resource repository processes queries and returns raw resource data. */
@Component
@Loggable
public interface ResourceRepository {

  /** Return raw XML for the query. */
  String execute(Query query);
}
