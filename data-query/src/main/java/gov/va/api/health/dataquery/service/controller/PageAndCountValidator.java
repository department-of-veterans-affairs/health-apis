package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.service.controller.ResourceExceptions.BadSearchParameter;

/** Container class for sharing useful Page and Count validation methods. */
public class PageAndCountValidator {

  /** Validate Count bounds else throw BadRequestException. */
  public static void validateCountBounds(int count, long maxRecordPerPage) {
    if (count <= 0 || count > maxRecordPerPage) {
      throw new BadSearchParameter("Count: [" + count + "] exceeds the minimum/maximum bounds.");
    }
  }
}
