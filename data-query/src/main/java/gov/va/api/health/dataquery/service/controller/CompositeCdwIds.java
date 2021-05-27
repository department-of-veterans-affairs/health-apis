package gov.va.api.health.dataquery.service.controller;

import gov.va.api.lighthouse.datamart.CompositeCdwId;

/** Utility for working with composite cdw ids. */
public class CompositeCdwIds {
  /**
   * Attempt to convert a cdwId into a CompositeCdwId. If conversion fails, return a NotFound
   * Resource Exception.
   */
  public static CompositeCdwId requireCompositeIdStringFormat(String cdwId) {
    try {
      return CompositeCdwId.fromCdwId(cdwId);
    } catch (IllegalArgumentException e) {
      throw new ResourceExceptions.NotFound(cdwId);
    }
  }
}
