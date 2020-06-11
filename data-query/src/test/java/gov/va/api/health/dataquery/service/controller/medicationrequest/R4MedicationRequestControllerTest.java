package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderSamples;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink;
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
public class R4MedicationRequestControllerTest {
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

  R4MedicationRequestController controller() {
    return new R4MedicationRequestController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://abed.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockMedicationOrderIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder()
            .identifier(cdwId)
            .resource("MEDICATION_ORDER")
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
              .resource("MEDICATION_ORDER")
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
    mockMedicationOrderIdentity("1", dm.cdwId());
    MedicationRequest actual = controller().read("1");
    assertThat(json(actual))
        .isEqualTo(
            json(MedicationRequestSamples.R4.create().medicationRequestFromMedicationOrder("1")));
  }

  @Test
  public void readRaw() {
    DatamartMedicationOrder dm = MedicationOrderSamples.Datamart.create().medicationOrder();
    MedicationOrderEntity entity = asEntity(dm);
    repository.save(entity);
    mockMedicationOrderIdentity("1", dm.cdwId());
    String actual = controller().readRaw("1", response);
    assertThat(toObject(actual)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockMedicationOrderIdentity("1", "1");
    controller().readRaw("1", response);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1", response);
  }

  @Test
  public void searchById() {
    DatamartMedicationOrder dm = MedicationOrderSamples.Datamart.create().medicationOrder();
    repository.save(asEntity(dm));
    mockMedicationOrderIdentity("1", dm.cdwId());
    MedicationRequest.Bundle actual = controller().searchById("1", 1, 1);
    MedicationRequest medicationRequest =
        MedicationRequestSamples.R4
            .create()
            .medicationRequestFromMedicationOrder("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                MedicationRequestSamples.R4.asBundle(
                    "http://abed.com/cool",
                    List.of(medicationRequest),
                    1,
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.first,
                        "http://abed.com/cool/MedicationRequest?identifier=1",
                        1,
                        1),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.self,
                        "http://abed.com/cool/MedicationRequest?identifier=1",
                        1,
                        1),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.last,
                        "http://abed.com/cool/MedicationRequest?identifier=1",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartMedicationOrder dm = MedicationOrderSamples.Datamart.create().medicationOrder();
    repository.save(asEntity(dm));
    mockMedicationOrderIdentity("1", dm.cdwId());
    MedicationRequest.Bundle actual = controller().searchByIdentifier("1", 1, 1);
    validateSearchByIdResult(dm, actual);
  }

  @Test
  public void searchByPatient() {
    Multimap<String, MedicationRequest> medicationRequestByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 10)))
        .isEqualTo(
            json(
                MedicationRequestSamples.R4.asBundle(
                    "http://abed.com/cool",
                    medicationRequestByPatient.get("p0"),
                    medicationRequestByPatient.get("p0").size(),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.first,
                        "http://abed.com/cool/MedicationRequest?patient=p0",
                        1,
                        10),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.self,
                        "http://abed.com/cool/MedicationRequest?patient=p0",
                        1,
                        10),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.last,
                        "http://abed.com/cool/MedicationRequest?patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientAndIntent() {
    Multimap<String, MedicationRequest> medicationRequestByPatientAndIntent = populateData();
    assertThat(json(controller().searchByPatientAndIntent("p0", "order", 1, 10)))
        .isEqualTo(
            json(
                MedicationRequestSamples.R4.asBundle(
                    "http://abed.com/cool",
                    medicationRequestByPatientAndIntent.get("p0"),
                    medicationRequestByPatientAndIntent.get("p0").size(),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.first,
                        "http://abed.com/cool/MedicationRequest?intent=order&patient=p0",
                        1,
                        10),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.self,
                        "http://abed.com/cool/MedicationRequest?intent=order&patient=p0",
                        1,
                        10),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.last,
                        "http://abed.com/cool/MedicationRequest?intent=order&patient=p0",
                        1,
                        10))));
  }

  @SneakyThrows
  private DatamartMedicationOrder toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedicationOrder.class);
  }

  private void validateSearchByIdResult(
      DatamartMedicationOrder dm, MedicationRequest.Bundle actual) {
    MedicationRequest medicationOrder =
        MedicationRequestSamples.R4
            .create()
            .medicationRequestFromMedicationOrder("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                MedicationRequestSamples.R4.asBundle(
                    "http://abed.com/cool",
                    List.of(medicationOrder),
                    1,
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.first,
                        "http://abed.com/cool/MedicationRequest?identifier=1",
                        1,
                        1),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.self,
                        "http://abed.com/cool/MedicationRequest?identifier=1",
                        1,
                        1),
                    MedicationRequestSamples.R4.link(
                        BundleLink.LinkRelation.last,
                        "http://abed.com/cool/MedicationRequest?identifier=1",
                        1,
                        1))));
  }
}
