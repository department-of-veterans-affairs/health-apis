package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.function.Function;

public class R4Controllers {

  /** . */
  public static <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
      B searchById(String id, Function<String, R> read, Function<R, B> toBundle) {
    R resource;
    try {
      resource = read.apply(id);
    } catch (ResourceExceptions.NotFound e) {
      resource = null;
    }
    return toBundle.apply(resource);
  }
}
