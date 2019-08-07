package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntoleranceSamples;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartObservationControllerTest {

  @Autowired
  private ObservationRepository repository;

  private static void setUpIds(IdentityService ids, DatamartAllergyIntolerance dm) {
    when(ids.lookup(DatamartObservationSamples.Fhir.ID)).thenReturn(asList(ResourceIdentity.builder().system("CDW").resource("OBSERVATION").identifier(dm.cdwId()).build()));
  }

  @Test
  public void readRaw() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller = new ObservationController(false, null, null, null, repository, WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    ???;
    setUpIds(ids, dm);
    assertThat(toObject(controller.readRaw(DatamartObservationSamples.Fhir.ID))).isEqualTo(dm);
  }
}
