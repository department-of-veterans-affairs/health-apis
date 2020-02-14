package gov.va.api.health.dataquery.service.config;

import com.google.common.base.Splitter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("DefaultAnnotationParam")
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("included-references")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenceSerializerProperties {

  private boolean location;

  private boolean organization;

  private boolean practitioner;

  /**
   * Return boolean property from property file for the listed resources. Return true by default.
   */
  public boolean checkForResource(String resourceName) {
    switch (resourceName) {
      case "Location":
        return location;
      case "Organization":
        return organization;
      case "Practitioner":
        return practitioner;
      default:
        return true;
    }
  }

  /**
   * Return true if the given reference is well formed, AllergyIntolerance/1234 or
   * .../AllergyIntolerance/1234 and it is set to true in the properties file. Return true for all
   * malformed references.
   */
  boolean isEnabled(gov.va.api.health.dstu2.api.elements.Reference reference) {
    String resourceName = resourceName(reference);
    if (resourceName == null) {
      return true;
    }
    return checkForResource(resourceName);
  }

  /**
   * Return true if the given reference is well formed, AllergyIntolerance/1234 or
   * .../AllergyIntolerance/1234 and it is set to true in the properties file. Return true for all
   * malformed references.
   */
  boolean isEnabled(gov.va.api.health.stu3.api.elements.Reference reference) {
    String resourceName = resourceName(reference);
    if (resourceName == null) {
      return true;
    }
    return checkForResource(resourceName);
  }

  /** Get the resource name of a reference if it is well formed, else return null. */
  private String resourceName(gov.va.api.health.dstu2.api.elements.Reference reference) {
    if (reference == null) {
      return null;
    }
    return resourceName(reference.reference());
  }

  private String resourceName(gov.va.api.health.stu3.api.elements.Reference reference) {
    if (reference == null) {
      return null;
    }
    return resourceName(reference.reference());
  }

  private String resourceName(String reference) {
    if (StringUtils.isBlank(reference)) {
      return null;
    }
    List<String> splitReference = Splitter.on('/').splitToList(reference);
    if (splitReference.size() <= 1) {
      return null;
    }
    String resourceName = splitReference.get(splitReference.size() - 2);
    if (StringUtils.isBlank(resourceName)) {
      return null;
    }
    if (!StringUtils.isAllUpperCase(resourceName.substring(0, 1))) {
      return null;
    }
    return resourceName;
  }
}
