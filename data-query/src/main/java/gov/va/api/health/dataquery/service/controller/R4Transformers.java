package gov.va.api.health.dataquery.service.controller;

import com.google.common.base.Splitter;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.r4.api.elements.Reference;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class R4Transformers {
  /** Convert the reference (if specified) to a Datamart reference. */
  public static DatamartReference asDatamartReference(Reference maybeReference) {
    if (maybeReference == null || StringUtils.isBlank(maybeReference.reference())) {
      return null;
    }
    List<String> splitReference = Splitter.on('/').splitToList(maybeReference.reference());
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
    String resourceId = splitReference.get(splitReference.size() - 1);
    if (StringUtils.isBlank(resourceId)) {
      return null;
    }
    return DatamartReference.builder()
        .display(Optional.ofNullable(maybeReference.display()))
        .reference(Optional.of(resourceId))
        .type(Optional.of(resourceName))
        .build();
  }

  /** Convert the datamart reference (if specified) to a FHIR reference. */
  public static Reference asReference(Optional<DatamartReference> maybeReference) {
    if (maybeReference == null || maybeReference.isEmpty()) {
      return null;
    }
    return asReference(maybeReference.get());
  }

  /** Convert the datamart reference (if specified) to a FHIR reference. */
  public static Reference asReference(DatamartReference maybeReference) {
    if (maybeReference == null) {
      return null;
    }
    Optional<String> path = maybeReference.asRelativePath();
    if (maybeReference.display().isEmpty() && path.isEmpty()) {
      return null;
    }
    return Reference.builder()
        .display(maybeReference.display().orElse(null))
        .reference(path.orElse(null))
        .build();
  }

  /** Get the reference id from the given reference. */
  public static String asReferenceId(Reference maybeReference) {
    DatamartReference maybeDatamart = asDatamartReference(maybeReference);
    if (maybeDatamart == null) {
      return null;
    }
    return maybeDatamart.reference().get();
  }
}
