package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.service.controller.ResourceExceptions.BadSearchParameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PageAndCountValidatorTest {

  @Test
  public void countEqualToZeroThrowsBadRequest() {
    Assertions.assertThrows(
        BadSearchParameter.class, () -> PageAndCountValidator.validateCountBounds(0, 50));
  }

  @Test
  public void countGreaterThanMaxRecordCountThrowsBadRequest() {
    Assertions.assertThrows(
        BadSearchParameter.class, () -> PageAndCountValidator.validateCountBounds(500000, 50));
  }

  @Test
  public void countLessThanZeroThrowsBadRequest() {
    Assertions.assertThrows(
        BadSearchParameter.class, () -> PageAndCountValidator.validateCountBounds(-1, 50));
  }

  @Test
  public void goodCountIsValidated() {
    PageAndCountValidator.validateCountBounds(50, 50);
  }
}
