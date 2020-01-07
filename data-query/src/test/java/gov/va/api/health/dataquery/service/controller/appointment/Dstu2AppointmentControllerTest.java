package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotImplemented;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("WeakerAccess")
public class Dstu2AppointmentControllerTest {

  Dstu2AppointmentController controller;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller = new Dstu2AppointmentController();
  }

  @Test(expected = NotImplemented.class)
  public void readNotImplemented() {
    controller.read("x");
  }

  @Test(expected = NotImplemented.class)
  public void searchByIdNotImplemented() {
    controller.searchById("x", 1, 1);
  }

  @Test(expected = NotImplemented.class)
  public void searchByIdentifierNotImplemented() {
    controller.searchByIdentifier("x", 1, 1);
  }

  @Test(expected = NotImplemented.class)
  public void searchByPatientNotImplemented() {
    controller.searchByPatient("x", 1, 1);
  }
}
