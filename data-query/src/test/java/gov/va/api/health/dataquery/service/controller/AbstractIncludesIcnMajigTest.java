package gov.va.api.health.dataquery.service.controller;

import static junit.framework.TestCase.fail;

import java.security.InvalidParameterException;
import org.junit.Test;

public class AbstractIncludesIcnMajigTest {

  @Test
  public void supportedAcceptsResourceOrResourceBundle() {
    fail();
  }

  @Test
  public void icnHeaderIsPresentForResource() {
    fail();
  }

  @Test
  public void icnHeaderIsPresentForResourceBundle() {
    fail();
  }

  @Test(expected = InvalidParameterException.class)
  public void supportsAcceptsAnUnknownType() {
    fail();
  }

}
