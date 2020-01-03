package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.parseInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.patient.PatientSamples.Datamart;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@SuppressWarnings("WeakerAccess")
@DataJpaTest
@RunWith(SpringRunner.class)
public class Dstu2PatientControllerTest {

  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private PatientSearchRepository repository;

  @Autowired private TestEntityManager testEntityManager;

  @SneakyThrows
  private PatientEntity asPatientEntity(DatamartPatient dm) {
    return PatientEntity.builder()
        .icn(dm.fullIcn())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @SneakyThrows
  private PatientSearchEntity asPatientSearchEntity(DatamartPatient dm) {
    return PatientSearchEntity.builder()
        .icn(dm.fullIcn())
        .name(dm.name())
        .firstName(dm.firstName())
        .lastName(dm.lastName())
        .gender(dm.gender())
        .birthDateTime(parseInstant(dm.birthDateTime()))
        .patient(asPatientEntity(dm))
        .build();
  }

  Dstu2PatientController controller() {
    return new Dstu2PatientController(
        new Dstu2Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockPatientIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("PATIENT").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  @Test
  public void read() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    Patient actual = controller().read("x");
    assertThat(actual).isEqualTo(PatientSamples.Dstu2.create().patient("x"));
  }

  @Test
  public void readRaw() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    String json = controller().readRaw("x", response);
    assertThat(PatientEntity.builder().payload(json).build().asDatamartPatient()).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", "x");
  }

  @Test
  public void searchByFamilyAndGender() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    Patient.Bundle patient = controller().searchByFamilyAndGender("Wolff180", "male", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.Dstu2.create().patient("x")));
  }

  @Test
  public void searchByFamilyAndGenderWithCountZero() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    Patient.Bundle patient = controller().searchByFamilyAndGender("Wolff180", "male", 1, 0);
    assertThat(patient.entry()).isEqualTo(Collections.emptyList());
  }

  @Test(expected = IllegalArgumentException.class)
  public void searchByFamilyAndGenderWithNullCdw() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    controller().searchByFamilyAndGender("Wolff180", "null", 1, 0);
  }

  @Test
  public void searchByGivenAndGender() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    Patient.Bundle patient = controller().searchByGivenAndGender("Tobias236", "male", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.Dstu2.create().patient("x")));
  }

  @Test
  public void searchById() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    Patient.Bundle patient = controller().searchById("x", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.Dstu2.create().patient("x")));
  }

  @Test
  public void searchByIdentifier() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    Patient.Bundle patient = controller().searchByIdentifier("x", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.Dstu2.create().patient("x")));
  }

  @Test
  public void searchByNameAndBirthdate() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    Patient.Bundle patient =
        controller()
            .searchByNameAndBirthdate(
                "Mr. Tobias236 Wolff180", new String[] {"ge1924-12-31"}, 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.Dstu2.create().patient("x")));
  }

  @Test
  public void searchByNameAndGender() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntity(dm));
    testEntityManager.persistAndFlush(asPatientSearchEntity(dm));
    Patient.Bundle patient =
        controller().searchByNameAndGender("Mr. Tobias236 Wolff180", "male", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.Dstu2.create().patient("x")));
  }
}
