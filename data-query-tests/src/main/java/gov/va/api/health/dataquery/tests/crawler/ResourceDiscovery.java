package gov.va.api.health.dataquery.tests.crawler;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.dstu2.api.resources.Conformance.RestResource;
import gov.va.api.health.fhir.api.FhirVersion;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import gov.va.api.health.r4.api.resources.CapabilityStatement.CapabilityResource;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * This class processes a conformance statement and generates a set of queries for resources as
 * described below.
 *
 * <p>For Patient resources, a read query (.../Patient/1234) is included if Patient.read is
 * supported. A search-by-id (.../Patient?_id=1234) is included is Patient.search is supported.
 *
 * <p>For all other resources, if search by the `patient` query parameter is supported, a
 * search-by-patient (.../Procedure?patient=1234) query is included.
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class ResourceDiscovery {

  @Getter private final Context context;

  /** Set _after_ queries have been determined. */
  private transient FhirVersion fhirVersion;

  /** Indicates if a URL represents a search query. */
  static boolean isSearch(@NonNull String url) {
    return url.contains("?");
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
    if (lastSlashIndex == -1) {
      throw new IllegalArgumentException("Missing / in url: " + url);
    }

    if (isSearch(url)) {
      int questionIndex = url.indexOf("?", lastSlashIndex);
      if (lastSlashIndex + 1 > questionIndex) {
        log.warn("Failed to extract resource from url '{}'.", url);
        return url;
      }
      return url.substring(lastSlashIndex + 1, questionIndex);
    }

    int secondLastSlashIndex = url.substring(0, lastSlashIndex).lastIndexOf("/");
    if (secondLastSlashIndex + 1 >= lastSlashIndex) {
      log.warn("Failed to extract resource from url '{}'.", url);
      return url;
    }
    return url.substring(secondLastSlashIndex + 1, lastSlashIndex);
  }

  /**
   * Return the FHIR version as determined by compiling the queries list. If queries has not been
   * invoked, this method will fail with an IllegalStateException.
   */
  public FhirVersion fhirVersion() {
    checkState(
        fhirVersion != null, "queries must be invoked before FHIR version can be determined");
    return fhirVersion;
  }

  /**
   * Return a list fully qualified queries for resources supported by the conformance statement as
   * described in the class documentation.
   */
  public List<String> queries() {
    return queriesFor(
        RestAssured.given().relaxedHTTPSValidation().baseUri(context().url()).get("metadata"));
  }

  /** This is extracted to make testing easier. */
  List<String> queriesFor(Response metadata) {
    log.info("Discovering {} (status {})", context().url(), metadata.statusCode());
    /* Attempt to determine what version of endpoint is serving metadata. */
    Optional<? extends MetadataSupport> support = Dstu2Support.fromMetadata(metadata);
    if (support.isEmpty()) {
      support = R4Support.fromMetadata(metadata);
    }
    if (support.isEmpty()) {
      throw new UnknownFhirVersion(context().url());
    }
    /* Record the FHIR version so that other may determine how to process these queries. */
    fhirVersion = support.get().fhirVersion();

    List<String> queries =
        Stream.concat(
                support.get().patientSearchableResourceQueries(context()),
                support.get().patientQueries(context()))
            .collect(toList());

    log.info("Discovered {} {} queries", queries.size(), fhirVersion);
    if (log.isInfoEnabled()) {
      queries.forEach(q -> log.info("Found {}", q));
    }
    return queries;
  }

  interface MetadataSupport {

    FhirVersion fhirVersion();

    /** Return a list of queries for Patient resources. */
    default Stream<String> patientQueries(Context context) {
      Optional<SupportedResource> patientMetadata =
          resources().filter(n -> "Patient".equals(n.type())).findFirst();
      if (patientMetadata.isEmpty()) {
        return Stream.empty();
      }
      return Stream.of(
          context.url() + "Patient/" + context.patientId(),
          context.url() + "Patient?_id=" + context.patientId());
    }

    /**
     * Return a list of queries for resources that can be searched by 'patient', e.g.
     * https://awesome.com/api/Procedure?patient=12345.
     */
    default Stream<String> patientSearchableResourceQueries(Context context) {
      return resources()
          .filter(SupportedResource::isSearchableByPatient)
          .map(r -> context.url() + r.type() + "?patient=" + context.patientId());
    }

    Stream<SupportedResource> resources();
  }

  interface SupportedResource {
    default boolean isSearchableByPatient() {
      return searchParameters().anyMatch("patient"::equals);
    }

    Stream<String> searchParameters();

    String type();
  }

  @Value
  public static class Context {
    @NonNull String url;

    @NonNull String patientId;

    @Builder
    private Context(@NonNull String url, @NonNull String patientId) {
      this.url = url.endsWith("/") ? url : url + "/";
      this.patientId = patientId;
    }
  }

  @RequiredArgsConstructor(staticName = "of")
  static class Dstu2Support implements MetadataSupport {
    @NonNull private final Conformance conformanceStatement;

    static Optional<Dstu2Support> fromMetadata(Response metadata) {
      try {
        Conformance conformanceStatement = metadata.as(Conformance.class);
        return Optional.of(Dstu2Support.of(conformanceStatement));
      } catch (Exception e) {
        log.info("DSTU2 support not available: {}", e.getMessage());
        return Optional.empty();
      }
    }

    @Override
    public FhirVersion fhirVersion() {
      return FhirVersion.DSTU2;
    }

    @Override
    public Stream<SupportedResource> resources() {
      if (conformanceStatement == null || conformanceStatement.rest() == null) {
        return Stream.empty();
      }
      return conformanceStatement.rest().stream()
          .flatMap(r -> r.resource().stream())
          .map(Dstu2SupportedResource::new);
    }
  }

  @RequiredArgsConstructor(staticName = "of")
  static class Dstu2SupportedResource implements SupportedResource {
    @NonNull private final RestResource restResource;

    @Override
    public Stream<String> searchParameters() {
      if (restResource == null || restResource.searchParam() == null) {
        return Stream.empty();
      }
      return restResource.searchParam().stream().map(Conformance.SearchParam::name);
    }

    @Override
    public String type() {
      return restResource.type();
    }
  }

  @RequiredArgsConstructor(staticName = "of")
  static class R4Support implements MetadataSupport {
    @NonNull private final CapabilityStatement capabilityStatement;

    static Optional<R4Support> fromMetadata(Response metadata) {
      try {
        CapabilityStatement conformanceStatement = metadata.as(CapabilityStatement.class);
        return Optional.of(R4Support.of(conformanceStatement));
      } catch (Exception e) {
        log.info("R4 support not available: {}", e.getMessage());
        return Optional.empty();
      }
    }

    @Override
    public FhirVersion fhirVersion() {
      return FhirVersion.R4;
    }

    @Override
    public Stream<SupportedResource> resources() {
      if (capabilityStatement == null || capabilityStatement.rest() == null) {
        return Stream.empty();
      }
      return capabilityStatement.rest().stream()
          .flatMap(r -> r.resource().stream())
          .map(R4SupportedResource::new);
    }
  }

  @RequiredArgsConstructor(staticName = "of")
  static class R4SupportedResource implements SupportedResource {
    @NonNull private final CapabilityResource restResource;

    @Override
    public Stream<String> searchParameters() {
      if (restResource == null || restResource.searchParam() == null) {
        return Stream.empty();
      }
      return restResource.searchParam().stream().map(CapabilityStatement.SearchParam::name);
    }

    @Override
    public String type() {
      return restResource.type();
    }
  }

  public static class UnknownFhirVersion extends RuntimeException {
    UnknownFhirVersion(String url) {
      super("Cannot determine FHIR version of metadata at " + url);
    }
  }
}
