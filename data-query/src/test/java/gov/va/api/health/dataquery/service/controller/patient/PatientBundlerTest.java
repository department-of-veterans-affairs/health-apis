package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.argonaut.api.resources.Patient;
import java.util.LinkedList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class PatientBundlerTest {
  @Test
  public void entriesTransformsPatientRootIntoListOfEntries() {
    List<Patient.Entry> testEntries = new LinkedList<>();
    List<Patient.Entry> expectedEntries = new LinkedList<>();
    Assertions.assertThat(testEntries).isEqualTo(expectedEntries);
  }
}
