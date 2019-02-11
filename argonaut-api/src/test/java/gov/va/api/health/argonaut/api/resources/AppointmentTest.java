package gov.va.api.health.argonaut.api.resources;

import static gov.va.api.health.argonaut.api.RoundTrip.assertRoundTrip;

import gov.va.api.health.argonaut.api.bundle.AbstractBundle;
import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.samples.SampleAppointments;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class AppointmentTest {

  private final SampleAppointments data = SampleAppointments.get();

  @Test
  public void appointment() {
    assertRoundTrip(data.appointment());
  }

  @Test
  public void bundlerCanBuildAppointmentBundles() {
    Appointment.Entry entry =
        Appointment.Entry.builder()
            .extension(Collections.singletonList(data.extension()))
            .fullUrl("http://Appointment.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(BundleLink.LinkRelation.self)
                        .url(("http://Appointment.com/1"))
                        .build()))
            .resource(data.appointment())
            .search(data.search())
            .request(data.request())
            .response(data.response())
            .build();
    Appointment.Bundle bundle =
        Appointment.Bundle.builder()
            .entry(Collections.singletonList(entry))
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(BundleLink.LinkRelation.self)
                        .url(("http://Appointment.com/2"))
                        .build()))
            .type(AbstractBundle.BundleType.searchset)
            .build();
    assertRoundTrip(bundle);
  }
}
