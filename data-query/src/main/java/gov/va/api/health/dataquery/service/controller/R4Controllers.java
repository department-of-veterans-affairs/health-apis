package gov.va.api.health.dataquery.service.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.List;
import java.util.function.Function;
import org.springframework.util.MultiValueMap;

public class R4Controllers {

  /** . */
  public static <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
      B searchById(
          MultiValueMap<String, String> parameters,
          Function<String, R> read,
          BundleBuilder<R, E, B> toBundle) {
    R resource;
    try {
      resource = read.apply(Parameters.identifierOf(parameters));
    } catch (ResourceExceptions.NotFound e) {
      resource = null;
    }
    List<R> entries =
        resource == null
                || Parameters.pageOf(parameters) != 1
                || Parameters.countOf(parameters) <= 0
            ? emptyList()
            : asList(resource);
    int numberOfEntries = resource == null ? 0 : 1;
    return toBundle.bundle(parameters, entries, numberOfEntries);
  }

  @FunctionalInterface
  public interface BundleBuilder<
      R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>> {
    B bundle(MultiValueMap<String, String> parameters, List<R> records, int totalRecords);
  }
}
