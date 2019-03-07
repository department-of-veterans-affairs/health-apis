package gov.va.api.health.sentinel.crawler;

import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.api.resources.Conformance;
import gov.va.api.health.dataquery.api.resources.Conformance.ResourceInteractionCode;
import gov.va.api.health.dataquery.api.resources.Conformance.RestResource;
import io.restassured.RestAssured;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * This class processes a conformance statment and generates a set of queries for resources as
 * described below.
 *
 * <p>For Patient resources, a read query (.../Patient/1234) is included if Patient.read is
 * supported. A search-by-id (.../Patient?_id=1234) is included is Patient.search is supported.
 *
 * <p>For all other resources, if search by the `patient` query parameter is supported, a
 * search-by-patient (.../Procedure?patient=1234) query is included.
 */
@Value
@Slf4j
public class ResourceDiscovery {
  String url;

  String patientId;

  /** The 'url' parameter will be modified if necessary to include a trailing /. */
  @Builder
  private ResourceDiscovery(@NonNull String url, @NonNull String patientId) {
    this.url = url.endsWith("/") ? url : url + "/";
    this.patientId = patientId;
  }

  /** Indicates if a URL represents a search query. */
  static boolean isSearch(@NonNull String url) {
    return url.contains("?");
  }

  private static <T> Stream<T> nullableListToStream(List<T> list) {
    return list == null ? Stream.empty() : list.stream();
  }

  /**
   * Extract resource from URL.
   *
   * <p>For example, resource is 'Condition' for:
   *
   * <ul>
   *   <li>https://foo.gov/services/argo/v0/Condition?patient=12345
   *   <li>https://foo.gov/api/Condition/12345
   * </ul>
   */
  static String resource(@NonNull String url) {
    int lastSlashIndex = url.lastIndexOf("/");
    if (isSearch(url)) {
      int questionIndex = url.indexOf("?", lastSlashIndex);
      if (lastSlashIndex + 1 > questionIndex) {
        log.warn("Failed to extract resource from url '{}'.", url);
        return url;
      }
      return url.substring(lastSlashIndex + 1, questionIndex);
    } else {
      int secondLastSlashIndex = url.substring(0, lastSlashIndex).lastIndexOf("/");
      if (secondLastSlashIndex + 1 > lastSlashIndex) {
        log.warn("Failed to extract resource from url '{}'.", url);
        return url;
      }
      return url.substring(secondLastSlashIndex + 1, lastSlashIndex);
    }
  }

  /**
   * Process the conformance statement looking for resources exposed via a FHIR-compliant REST
   * endpoint.
   *
   * <p>This method is made package protected to enable easier unit testing without the need of
   * mocking live HTTP endpoints.
   */
  List<RestResource> extractRestResources(Conformance conformanceStatement) {
    if (conformanceStatement == null || conformanceStatement.rest() == null) {
      return emptyList();
    }
    return conformanceStatement
        .rest()
        .stream()
        .flatMap(r -> r.resource().stream())
        .collect(Collectors.toList());
  }

  private boolean isSearchableByPatient(RestResource supportedResources) {
    if (supportedResources == null || supportedResources.searchParam() == null) {
      return false;
    }
    return supportedResources.searchParam().stream().anyMatch(o -> o.name().equals("patient"));
  }

  List<String> patientQueries(List<RestResource> restResources) {
    Optional<RestResource> patientMetadata =
        restResources.stream().filter(n -> "Patient".equals(n.type())).findFirst();
    if (!patientMetadata.isPresent()) {
      return emptyList();
    }
    List<String> patientQueries = new ArrayList<>();
    boolean isReadable =
        patientMetadata
            .get()
            .interaction()
            .stream()
            .anyMatch(p -> ResourceInteractionCode.read.equals(p.code()));
    if (isReadable) {
      patientQueries.add(url + "Patient/" + patientId);
    }
    boolean isSearchable =
        nullableListToStream(patientMetadata.get().searchParam())
            .anyMatch(s -> "_id".equals(s.name()));
    if (isSearchable) {
      patientQueries.add((url + "Patient?_id=" + patientId));
    }
    return patientQueries;
  }

  /**
   * Return a list of queries for resources that can be searched by 'patient', e.g.
   * https://awesome.com/api/Procedure?patient=12345.
   */
  List<String> patientSearchableResourceQueries(@NonNull List<RestResource> restResources) {
    return restResources
        .stream()
        .filter(this::isSearchableByPatient)
        .map(p -> url + p.type() + "?patient=" + patientId)
        .collect(Collectors.toList());
  }

  /**
   * Return a list fully qualified queries for resources supported by the conformance statement as
   * described in the class documentation.
   */
  public List<String> queries() {
    log.info("Discovering {}", url);
    Conformance conformanceStatement =
        RestAssured.given()
            .relaxedHTTPSValidation()
            .baseUri(url)
            .get("metadata")
            .as(Conformance.class);
    List<RestResource> restResources = extractRestResources(conformanceStatement);
    List<String> queries = new LinkedList<>();
    queries.addAll(patientSearchableResourceQueries(restResources));
    queries.addAll(patientQueries(restResources));
    log.info("Discovered {} queries", queries.size());
    if (log.isInfoEnabled()) {
      queries.forEach(q -> log.info("Found {}", q));
    }
    return queries;
  }
}
