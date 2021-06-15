package gov.va.api.health.dataquery.service.controller;

import gov.va.api.lighthouse.datamart.CompositeCdwId;
import java.util.Optional;
import lombok.NonNull;

/** Utility for working with composite cdw ids. */
public class CompositeCdwIds {
  /** Create composite ID, suppressing exceptions. */
  public static Optional<CompositeCdwId> optionalFromCdwId(@NonNull String cdwId) {
    try {
      return Optional.ofNullable(CompositeCdwId.fromCdwId(cdwId));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  /** Convert string to CompositeCdwId, throwing NotFound Resource Exception on failure. */
  public static CompositeCdwId requireCompositeIdStringFormat(String cdwId) {
    try {
      return CompositeCdwId.fromCdwId(cdwId);
    } catch (IllegalArgumentException e) {
      throw new ResourceExceptions.NotFound(cdwId);
    }
  }
}
