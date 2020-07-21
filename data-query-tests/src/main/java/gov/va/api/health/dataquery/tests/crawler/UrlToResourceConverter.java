package gov.va.api.health.dataquery.tests.crawler;

import gov.va.api.health.fhir.api.FhirVersion;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

/**
 * Converts an Argonaut URL into a class type that represents the result of the query. Examples:
 *
 * <pre>{@code
 * https://argonaut.com/api/Patient/123 -> Patient.class
 * https://apis.va.gov/services/argonaut/v0/Patient?_id=123 -> Patient.Bundle.class
 * https://apis.va.gov/services/argonaut/v0/Procedure?patient=123 -> Procedure.Bundle.class
 * }</pre>
 */
@AllArgsConstructor
@Builder
public class UrlToResourceConverter implements Function<String, Class<?>> {

  final Class<?> bundleClass;

  final List<Package> resourcePackages;

  /** Configure an instance based on the FHIR version. */
  public static UrlToResourceConverter forFhirVersion(@NonNull FhirVersion fhirVersion) {
    switch (fhirVersion) {
      case DSTU2:
        return UrlToResourceConverter.builder()
            .bundleClass(gov.va.api.health.dstu2.api.bundle.AbstractBundle.class)
            .resourcePackages(
                List.of(
                    gov.va.api.health.argonaut.api.resources.Patient.class.getPackage(),
                    gov.va.api.health.dstu2.api.resources.Resource.class.getPackage()))
            .build();
      case STU3:
        return UrlToResourceConverter.builder()
            .bundleClass(gov.va.api.health.stu3.api.bundle.AbstractBundle.class)
            .resourcePackages(
                List.of(
                    gov.va.api.health.stu3.api.resources.Location.class.getPackage(),
                    gov.va.api.health.stu3.api.resources.Resource.class.getPackage()))
            .build();
      case R4:
        return UrlToResourceConverter.builder()
            .bundleClass(gov.va.api.health.r4.api.bundle.AbstractBundle.class)
            .resourcePackages(
                List.of(
                    gov.va.api.health.uscorer4.api.resources.Patient.class.getPackage(),
                    gov.va.api.health.r4.api.resources.Resource.class.getPackage()))
            .build();
      default:
        throw new IllegalArgumentException("Do not understand FHIR version:" + fhirVersion);
    }
  }

  @Override
  public Class<?> apply(@NonNull String argonautUrl) {
    URL url = asUrl(argonautUrl);
    String[] pathParts = pathOf(url);

    /*
     * The resource will be the last or second to last element in the path. Depending on whether
     * this is a read or search.
     *
     * If it is the last element in the path, then this is a search.
     *
     * If it is the second-to-last, then this is a read.
     */
    Class<?> resourceClass = findClass(pathParts[pathParts.length - 1]);

    if (resourceClass != null) {
      return bundleOf(resourceClass);
    }

    if (pathParts.length >= 2) {
      resourceClass = findClass(pathParts[pathParts.length - 2]);
      if (resourceClass != null) {
        return resourceClass;
      }
    }
    throw new DoNotUnderstandUrl(argonautUrl);
  }

  /** Convert the full URL into something we know is correctly formed. */
  private URL asUrl(@NonNull String argonautUrl) {
    URL url;
    try {
      url = new URL(argonautUrl);
    } catch (MalformedURLException e) {
      throw new DoNotUnderstandUrl(argonautUrl);
    }
    return url;
  }

  /**
   * Search the resource class for a nested bundle class. This is expecting the standard pattern
   * where the bundle is a static inner class of the resource.
   */
  private Class<?> bundleOf(Class<?> resourceClass) {
    for (Class<?> maybeBundle : resourceClass.getDeclaredClasses()) {
      if (bundleClass.isAssignableFrom(maybeBundle)) {
        return maybeBundle;
      }
    }
    throw new IllegalStateException("Not bundle class found for " + resourceClass);
  }

  /**
   * Attempt to find a resource class for the given resource name. This will attempt to use the
   * simple name, e.g. Patient and turn it into a class assuming that all resources are packaged
   * next to Resource.
   */
  private Class<?> findClass(String resourceName) {
    for (Package resourcePackage : resourcePackages) {
      String className = resourcePackage.getName() + "." + resourceName;
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e) {
        // try a different package
      }
    }
    return null;
  }

  /** Tear the URL into parts of the path, dying if it cannot be split up. */
  private String[] pathOf(URL url) {
    String path = url.getPath();
    if (path.isEmpty()) {
      throw new DoNotUnderstandUrl(url.toString());
    }
    return path.split("/");
  }

  /**
   * This indicates that the URL cannot be understood and converted into a resource class or bundle.
   */
  public static class DoNotUnderstandUrl extends RuntimeException {
    DoNotUnderstandUrl(String message) {
      super(message);
    }
  }
}
