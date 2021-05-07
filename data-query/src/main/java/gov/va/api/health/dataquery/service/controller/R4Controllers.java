package gov.va.api.health.dataquery.service.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.ids.client.IdEncoder;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.lighthouse.vulcan.mappings.ReferenceParameter;
import java.util.List;
import java.util.function.Function;
import org.springframework.util.MultiValueMap;

public class R4Controllers {

  /** Determine if the ReferenceParameter for a search by "patient" is supported. */
  public static boolean referencePatientIsSupported(
      ReferenceParameter reference, LinkProperties linkProperties) {
    // Only support R4 Patient Read urls
    if (reference.url().isPresent()) {
      var allowedUrl = linkProperties.r4().readUrl("Patient", reference.publicId());
      return reference.url().get().equals(allowedUrl);
    }
    return "Patient".equals(reference.type());
  }

  public static String referencePatientValues(ReferenceParameter reference) {
    return reference.publicId();
  }

  /** Perform a search by ID using a resources "read" function. */
  public static <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
      B searchById(
          MultiValueMap<String, String> parameters,
          Function<String, R> read,
          BundleBuilder<R, E, B> toBundle) {
    R resource;
    try {
      resource = read.apply(Parameters.identifierOf(parameters));
    } catch (ResourceExceptions.NotFound | IdEncoder.BadId e) {
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
