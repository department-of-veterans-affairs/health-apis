package gov.va.api.health.dataquery.service.controller;

import javax.ws.rs.BadRequestException;
import org.junit.Test;

public class PageAndCountValidatorTest {

  @Test(expected = BadRequestException.class)
  public void countEqualToZeroThrowsBadRequest() {
    PageAndCountValidator.validateCountBounds(0, 50);
  }

  @Test(expected = BadRequestException.class)
  public void countGreaterThanMaxRecordCountThrowsBadRequest() {
    PageAndCountValidator.validateCountBounds(500000, 50);
  }

  @Test(expected = BadRequestException.class)
  public void countLessThanZeroThrowsBadRequest() {
    PageAndCountValidator.validateCountBounds(-1, 50);
  }

  @Test
  public void goodCountIsValidated() {
    PageAndCountValidator.validateCountBounds(50, 50);
  }
}
