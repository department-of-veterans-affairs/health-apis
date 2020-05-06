package gov.va.api.health.dataquery.service.controller.medicationorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationOrder.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderSamples.Dstu2;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class Dstu2MedicationOrderControllerTest {
  HttpServletResponse response;

  @Autowired private MedicationOrderRepository repository;

  private IdentityService ids = mock(IdentityService.class);

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

  Dstu2MedicationOrderController controller() {
    return new Dstu2MedicationOrderController(
        new Dstu2Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
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
    when(ids.register(any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  private Multimap<String, MedicationOrder> populateData() {
    var fhir = MedicationOrderSamples.Dstu2.create();
    var datamart = MedicationOrderSamples.Datamart.create();
    var medicationOrderByPatient = LinkedHashMultimap.<String, MedicationOrder>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      var patientId = "p" + i % 2;
      var cdwId = "" + i;
      var publicId = "90" + i;
      var dm = datamart.medicationOrder(cdwId, patientId);
      repository.save(asEntity(dm));
      var medicationOrder = fhir.medicationOrder(publicId, patientId);
      medicationOrderByPatient.put(patientId, medicationOrder);
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
    return medicationOrderByPatient;
  }

  @Test
  public void read() {
    DatamartMedicationOrder dm = MedicationOrderSamples.Datamart.create().medicationOrder();
    repository.save(asEntity(dm));
    mockMedicationOrderIdentity("1", dm.cdwId());
    MedicationOrder actual = controller().read("1");
    assertThat(json(actual))
        .isEqualTo(json(MedicationOrderSamples.Dstu2.create().medicationOrder("1")));
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
    MedicationOrder.Bundle actual = controller().searchById("1", 1, 1);
    MedicationOrder medicationOrder =
        MedicationOrderSamples.Dstu2.create().medicationOrder("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                MedicationOrderSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(medicationOrder),
                    1,
                    MedicationOrderSamples.Dstu2.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/MedicationOrder?identifier=1",
                        1,
                        1),
                    MedicationOrderSamples.Dstu2.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/MedicationOrder?identifier=1",
                        1,
                        1),
                    MedicationOrderSamples.Dstu2.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/MedicationOrder?identifier=1",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartMedicationOrder dm = MedicationOrderSamples.Datamart.create().medicationOrder();
    repository.save(asEntity(dm));
    mockMedicationOrderIdentity("1", dm.cdwId());
    Bundle actual = controller().searchByIdentifier("1", 1, 1);
    validateSearchByIdResult(dm, actual);
  }

  @Test
  public void searchByPatient() {
    Multimap<String, MedicationOrder> medicationOrderByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 10)))
        .isEqualTo(
            json(
                MedicationOrderSamples.Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    medicationOrderByPatient.get("p0"),
                    medicationOrderByPatient.get("p0").size(),
                    MedicationOrderSamples.Dstu2.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/MedicationOrder?patient=p0",
                        1,
                        10),
                    MedicationOrderSamples.Dstu2.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/MedicationOrder?patient=p0",
                        1,
                        10),
                    MedicationOrderSamples.Dstu2.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/MedicationOrder?patient=p0",
                        1,
                        10))));
  }

  @SneakyThrows
  private DatamartMedicationOrder toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedicationOrder.class);
  }

  @Test
  public void validate() {
    DatamartMedicationOrder dm = MedicationOrderSamples.Datamart.create().medicationOrder();
    MedicationOrder medicationOrder =
        MedicationOrderSamples.Dstu2.create().medicationOrder("1", dm.patient().reference().get());
    assertThat(
            controller()
                .validate(
                    MedicationOrderSamples.Dstu2.asBundle(
                        "http://fonzy.com/cool",
                        List.of(medicationOrder),
                        1,
                        MedicationOrderSamples.Dstu2.link(
                            LinkRelation.first,
                            "http://fonzy.com/cool/MedicationOrder?identifier=1",
                            1,
                            1),
                        MedicationOrderSamples.Dstu2.link(
                            LinkRelation.self,
                            "http://fonzy.com/cool/MedicationOrder?identifier=1",
                            1,
                            1),
                        MedicationOrderSamples.Dstu2.link(
                            LinkRelation.last,
                            "http://fonzy.com/cool/MedicationOrder?identifier=1",
                            1,
                            1))))
        .isEqualTo(Dstu2Validator.ok());
  }

  private void validateSearchByIdResult(DatamartMedicationOrder dm, Bundle actual) {
    MedicationOrder medicationOrder =
        Dstu2.create().medicationOrder("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(medicationOrder),
                    1,
                    Dstu2.link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/MedicationOrder?identifier=1",
                        1,
                        1),
                    Dstu2.link(
                        LinkRelation.self,
                        "http://fonzy.com/cool/MedicationOrder?identifier=1",
                        1,
                        1),
                    Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/MedicationOrder?identifier=1",
                        1,
                        1))));
  }
}
