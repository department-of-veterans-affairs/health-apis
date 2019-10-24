package gov.va.api.health.dataquery.service.controller;

import javax.ws.rs.BadRequestException;

/** Container class for sharing useful Page and Count validation methods. */
public class PageAndCountValidator {

  /** Validate Count bounds else throw BadRequestException. */
  public static void validateCountBounds(int count, long maxRecordCount) {
    if (count <= 0 || count >= maxRecordCount) {
      throw new BadRequestException("Count: [" + count + "] exceeds the minimum/maximum bounds.");
    }
  }
}
