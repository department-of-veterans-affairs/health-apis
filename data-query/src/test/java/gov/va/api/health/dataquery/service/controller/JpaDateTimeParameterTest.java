package gov.va.api.health.dataquery.service.controller;

import java.util.List;
import javax.persistence.TypedQuery;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JpaDateTimeParameterTest {

  @Mock TypedQuery query;

  @Test
  public void addQueryParametersForEach() {
    JpaDateTimeParameter.addQueryParametersForEach(query, null);
    List<String> dates = List.of("GE1990-10", "GT1900-10", "LT1900-10");
    JpaDateTimeParameter.addQueryParametersForEach(query, dates);
  }

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }
}
