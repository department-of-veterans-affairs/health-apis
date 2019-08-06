package gov.va.api.health.dataquery.service.controller.immunization;

import static gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunizationSamples.Fhir.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.Immunization.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunizationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunizationSamples.Fhir;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartImmunizationControllerTest {
  private IdentityService ids = mock(IdentityService.class);
  @Autowired private ImmunizationRepository repository;
  @Autowired private TestEntityManager entityManager;

  @SneakyThrows
  private ImmunizationEntity asEntity(DatamartImmunization dm) {
    return ImmunizationEntity.builder()
        .cdwId(new BigInteger(dm.cdwId()))
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  ImmunizationController controller() {
    return new ImmunizationController(
        true,
        null,
        null,
        new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockImmunizationIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("IMMUNIZATION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  private Multimap<String, Immunization> populateData() {
    var fhir = Fhir.create();
    var datamart = Datamart.create();
    var immunizationByPatient = LinkedHashMultimap.<String, Immunization>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String patientId = "p" + i % 2;
      String cdwId = "" + i;
      String publicId = "90" + i;
      var dm = datamart.immunization(cdwId, patientId);
      repository.save(asEntity(dm));
      var immunization = fhir.immunization(publicId, patientId);
      immunizationByPatient.put(patientId, immunization);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .system("CDW")
              .resource("IMMUNIZATION")
              .identifier(cdwId)
              .build();
      Registration registration =
          Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return immunizationByPatient;
  }

  @Test
  public void read() {
    DatamartImmunization dm = Datamart.create().immunization();
    repository.save(asEntity(dm));
    mockImmunizationIdentity("1", dm.cdwId());
    Immunization actual = controller().read("true", "1");
    assertThat(json(actual))
        .isEqualTo(json(Fhir.create().immunization("1", dm.patient().reference().get())));
  }

  @Test
  public void readRaw() {
    DatamartImmunization dm = Datamart.create().immunization();
    repository.save(asEntity(dm));
    mockImmunizationIdentity("1", dm.cdwId());
    String json = controller().readRaw("1");
    assertThat(toObject(json)).isEqualTo(dm);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockImmunizationIdentity("1", "1");
    controller().readRaw("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1");
  }

  @Test
  public void searchById() {
    DatamartImmunization dm = Datamart.create().immunization();
    repository.save(asEntity(dm));
    mockImmunizationIdentity("1", dm.cdwId());
    Bundle actual = controller().searchById("true", "1", 1, 1);
    Immunization immunization = Fhir.create().immunization("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(immunization),
                    link(
                        LinkRelation.first,
                        "http://fonzy.com/cool/Immunization?identifier=1",
                        1,
                        1),
                    link(
                        LinkRelation.self, "http://fonzy.com/cool/Immunization?identifier=1", 1, 1),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Immunization?identifier=1",
                        1,
                        1))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, Immunization> immunizationByPatient = populateData();
    assertThat(json(controller().searchByPatient("true", "p0", 1, 10)))
        .isEqualTo(
            json(
                Fhir.asBundle(
                    "http://fonzy.com/cool",
                    immunizationByPatient.get("p0"),
                    link(
                        LinkRelation.first, "http://fonzy.com/cool/Immunization?patient=p0", 1, 10),
                    link(LinkRelation.self, "http://fonzy.com/cool/Immunization?patient=p0", 1, 10),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Immunization?patient=p0",
                        1,
                        10))));
  }

  @SneakyThrows
  private DatamartImmunization toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartImmunization.class);
  }
}
