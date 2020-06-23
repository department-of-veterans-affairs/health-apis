package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.patient.PatientSamples.Datamart;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.uscorer4.api.resources.Patient;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SuppressWarnings("WeakerAccess")
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
public class R4PatientControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private PatientRepositoryV2 repository;

  @Autowired private TestEntityManager testEntityManager;

  @SneakyThrows
  private PatientEntityV2 asPatientEntityV2(DatamartPatient dm) {
    return PatientEntityV2.builder()
        .icn(dm.fullIcn())
        .fullName(dm.name())
        .lastName(dm.lastName())
        .firstName(dm.firstName())
        .gender(dm.gender())
        .birthDate(parseInstant(dm.birthDateTime()))
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  R4PatientController controller() {
    return new R4PatientController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
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
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  @Test
  public void read() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    Patient actual = controller().read("x");
    assertThat(actual).isEqualTo(PatientSamples.R4.create().patient("x"));
  }

  @Test
  public void readRaw() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    String json = controller().readRaw("x", response);
    assertThat(PatientEntityV2.builder().payload(json).build().asDatamartPatient()).isEqualTo(dm);
    verify(response, Mockito.times(1)).addHeader("X-VA-INCLUDES-ICN", "x");
  }

  @Test
  public void searchByBirthdateAndFamily() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    Patient.Bundle patient =
        controller().searchByBirthdateAndFamily(new String[] {"ge1924-12-31"}, "Wolff180", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.R4.create().patient("x")));
  }

  @Test
  public void searchByFamilyAndGender() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    Patient.Bundle patient = controller().searchByFamilyAndGender("Wolff180", "male", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.R4.create().patient("x")));
  }

  @Test
  public void searchByFamilyAndGenderWithCountZero() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    Patient.Bundle patient = controller().searchByFamilyAndGender("Wolff180", "male", 1, 0);
    assertThat(patient.entry()).isEqualTo(Collections.emptyList());
  }

  @Test
  public void searchByFamilyAndWithNullGender() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    assertThrows(
        IllegalArgumentException.class,
        () -> controller().searchByFamilyAndGender("Wolff180", "null", 1, 0));
  }

  @Test
  public void searchById() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    Patient.Bundle patient = controller().searchById("x", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.R4.create().patient("x")));
  }

  @Test
  public void searchByIdentifier() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    Patient.Bundle patient = controller().searchByIdentifier("x", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.R4.create().patient("x")));
  }

  @Test
  public void searchByName() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    Patient.Bundle patient = controller().searchByName("Mr. Tobias236 Wolff180", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.R4.create().patient("x")));
  }

  @Test
  public void searchByNameAndBirthdate() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    Patient.Bundle patient =
        controller()
            .searchByNameAndBirthdate(
                "Mr. Tobias236 Wolff180", new String[] {"ge1924-12-31"}, 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.R4.create().patient("x")));
  }

  @Test
  public void searchByNameAndGender() {
    DatamartPatient dm = Datamart.create().patient("x");
    testEntityManager.persistAndFlush(asPatientEntityV2(dm));
    Patient.Bundle patient =
        controller().searchByNameAndGender("Mr. Tobias236 Wolff180", "male", 1, 1);
    assertThat(json(Iterables.getOnlyElement(patient.entry()).resource()))
        .isEqualTo(json(PatientSamples.R4.create().patient("x")));
  }
}
