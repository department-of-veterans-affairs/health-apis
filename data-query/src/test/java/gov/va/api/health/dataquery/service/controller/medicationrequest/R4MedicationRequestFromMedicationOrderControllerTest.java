package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderSamples;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class R4MedicationRequestFromMedicationOrderControllerTest {
  HttpServletResponse response;

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private MedicationRequestFromMedicationOrderRepository repository;

  @Before
  public void _init() {
    response = mock(HttpServletResponse.class);
  }

  @SneakyThrows
  private MedicationOrderEntity asEntity(DatamartMedicationOrder dm) {
    return MedicationOrderEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  R4MedicationRequestFromMedicationOrderController controller() {
    return new R4MedicationRequestFromMedicationOrderController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockMedicationRequestIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder()
            .identifier(cdwId)
            .resource("MEDICATION_REQUEST")
            .system("CDW")
            .build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  private Multimap<String, MedicationRequest> populateData() {
    var fhir = MedicationRequestSamples.R4.create();
    var datamart = MedicationOrderSamples.Datamart.create();
    var medicationRequestByPatient = LinkedHashMultimap.<String, MedicationRequest>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      var patientId = "p" + i % 2;
      var cdwId = "" + i;
      var publicId = "90" + i;
      var dm = datamart.medicationOrder(cdwId, patientId);
      repository.save(asEntity(dm));
      var medicationRequest = fhir.medicationRequestFromMedicationOrder(publicId, patientId);
      medicationRequestByPatient.put(patientId, medicationRequest);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .identifier(cdwId)
              .resource("MEDICATION_REQUEST")
              .system("CDW")
              .build();
      Registration registration =
          Registration.builder()
              .uuid(publicId)
              .resourceIdentities(List.of(resourceIdentity))
              .build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(any())).thenReturn(registrations);
    return medicationRequestByPatient;
  }

  @Test
  public void read() {
    DatamartMedicationOrder dm = MedicationOrderSamples.Datamart.create().medicationOrder();
    repository.save(asEntity(dm));
    mockMedicationRequestIdentity("1", dm.cdwId());
    MedicationRequest actual = controller().read("1");
//    String blah =
//        json(MedicationRequestSamples.R4.create().medicationRequestFromMedicationOrder("1"));
    assertThat(json(actual))
        .isEqualTo(
            json(MedicationRequestSamples.R4.create().medicationRequestFromMedicationOrder("1")));
  }
}
