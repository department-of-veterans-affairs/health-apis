package gov.va.api.health.dataquery.service.controller.medicationdispense;

import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import org.junit.Test;

public class Dstu2MedicationDispenseControllerTest {

  Dstu2MedicationDispenseController controller() {
    return new Dstu2MedicationDispenseController();
  }

  @Test(expected = ResourceExceptions.NotImplemented.class)
  public void read() {
    controller().read("id");
  }

  @Test(expected = ResourceExceptions.NotImplemented.class)
  public void searchById() {
    controller().searchById("id", 1, 1);
  }

  @Test(expected = ResourceExceptions.NotImplemented.class)
  public void searchByIdentifier() {
    controller().searchByIdentifier("id", 1, 1);
  }

  @Test(expected = ResourceExceptions.NotImplemented.class)
  public void searchByPatient() {
    controller().searchByPatient("patient", 1, 1);
  }

  @Test(expected = ResourceExceptions.NotImplemented.class)
  public void searchByPatientAndStatus() {
    controller().searchByPatientAndStatus("patient", "status", 1, 1);
  }

  @Test(expected = ResourceExceptions.NotImplemented.class)
  public void searchByPatientAndType() {
    controller().searchByPatientAndType("patient", "type", 1, 1);
  }
}
