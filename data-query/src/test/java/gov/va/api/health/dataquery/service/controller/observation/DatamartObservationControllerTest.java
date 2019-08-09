package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartObservationControllerTest {

  @Autowired private ObservationRepository repository;

  @SneakyThrows
  private static ObservationEntity asEntity(DatamartObservation dm) {
    return ObservationEntity.builder()
        .id(dm.cdwId())
        .icn(dm.subject().get().reference().get())
        .category(dm.category().toString())
        .code(dm.code().get().coding().get().code().get())
        .epochTime(dm.effectiveDateTime().get().toEpochMilli())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  private static void setUpIds(IdentityService ids, DatamartObservation dm) {
    when(ids.lookup(DatamartObservationSamples.Fhir.ID))
        .thenReturn(
            asList(
                ResourceIdentity.builder()
                    .system("CDW")
                    .resource("OBSERVATION")
                    .identifier(dm.cdwId())
                    .build()));
    when(ids.register(Mockito.any()))
        .thenReturn(
            asList(
                Registration.builder()
                    .uuid(DatamartObservationSamples.Fhir.ID)
                    .resourceIdentity(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("OBSERVATION")
                            .identifier(dm.cdwId())
                            .build())
                    .build(),
                Registration.builder()
                    .uuid(DatamartObservationSamples.Fhir.SUBJECT_ID)
                    .resourceIdentity(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("PATIENT")
                            .identifier(dm.subject().get().reference().get())
                            .build())
                    .build(),
                Registration.builder()
                    .uuid(DatamartObservationSamples.Fhir.ENCOUNTER_ID)
                    .resourceIdentity(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("ENCOUNTER")
                            .identifier(dm.encounter().get().reference().get())
                            .build())
                    .build(),
                Registration.builder()
                    .uuid(DatamartObservationSamples.Fhir.PERFORMER_ID_1)
                    .resourceIdentity(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("PRACTITIONER")
                            .identifier(dm.performer().get(0).reference().get())
                            .build())
                    .build(),
                Registration.builder()
                    .uuid(DatamartObservationSamples.Fhir.PERFORMER_ID_2)
                    .resourceIdentity(
                        ResourceIdentity.builder()
                            .system("CDW")
                            .resource("ORGANIZATION")
                            .identifier(dm.performer().get(1).reference().get())
                            .build())
                    .build()));
  }

  @SneakyThrows
  private static DatamartObservation toObject(String payload) {
    return JacksonConfig.createMapper().readValue(payload, DatamartObservation.class);
  }

  @Test
  public void read() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            true,
            null,
            null,
            null,
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    Observation result = controller.read("", DatamartObservationSamples.Fhir.ID);
    assertThat(result).isEqualTo(DatamartObservationSamples.Fhir.create().observation());
  }

  @Test
  public void readRaw() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            false,
            null,
            null,
            null,
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    assertThat(toObject(controller.readRaw(DatamartObservationSamples.Fhir.ID))).isEqualTo(dm);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRaw_unknown() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            false,
            null,
            null,
            null,
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    controller.readRaw("55555");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void read_unknown() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            true,
            null,
            null,
            null,
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    controller.read("", "55555");
  }

  @Test
  public void searchById() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            false,
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    assertThat(controller.searchById("true", DatamartObservationSamples.Fhir.ID, 2, 1).entry())
        .isEmpty();
    assertThat(controller.searchById("true", DatamartObservationSamples.Fhir.ID, 1, 0).entry())
        .isEmpty();
    assertThat(
            Iterables.getOnlyElement(
                    controller.searchById("true", DatamartObservationSamples.Fhir.ID, 1, 1).entry())
                .resource())
        .isEqualTo(DatamartObservationSamples.Fhir.create().observation());
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void searchById_unknown() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            false,
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    controller.searchById("true", "55555", 1, 15);
  }

  @Test
  public void searchByIdentifier() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            false,
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    assertThat(
            controller.searchByIdentifier("true", DatamartObservationSamples.Fhir.ID, 2, 1).entry())
        .isEmpty();
    assertThat(
            controller.searchByIdentifier("true", DatamartObservationSamples.Fhir.ID, 1, 0).entry())
        .isEmpty();
    assertThat(
            Iterables.getOnlyElement(
                    controller
                        .searchByIdentifier("true", DatamartObservationSamples.Fhir.ID, 1, 1)
                        .entry())
                .resource())
        .isEqualTo(DatamartObservationSamples.Fhir.create().observation());
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void searchByIdentifier_unknown() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            false,
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    controller.searchByIdentifier("true", "55555", 1, 1);
  }

  @Test
  public void searchByPatient() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            true,
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    assertThat(controller.searchByPatient("", "55555", 1, 1).entry()).isEmpty();
    assertThat(controller.searchByPatient("", "1002003004V666666", 2, 1).entry()).isEmpty();
    assertThat(
            Iterables.getOnlyElement(
                    controller.searchByPatient("", "1002003004V666666", 1, 1).entry())
                .resource())
        .isEqualTo(DatamartObservationSamples.Fhir.create().observation());
  }

  @Test
  public void searchByPatientAndCategory() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            true,
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    assertThat(
            controller
                .searchByPatientAndCategory(
                    "", "55555", "laboratory", new String[] {"eq2012-12-24"}, 1, 1)
                .entry())
        .isEmpty();
    assertThat(
            controller
                .searchByPatientAndCategory(
                    "", "1002003004V666666", "imaging", new String[] {"eq2012-12-24"}, 1, 1)
                .entry())
        .isEmpty();
    assertThat(
            controller
                .searchByPatientAndCategory(
                    "", "1002003004V666666", "laboratory", new String[] {"eq2012-12-24"}, 2, 1)
                .entry())
        .isEmpty();
    assertThat(
            Iterables.getOnlyElement(
                    controller
                        .searchByPatientAndCategory(
                            "", "1002003004V666666", "laboratory", null, 1, 1)
                        .entry())
                .resource())
        .isEqualTo(DatamartObservationSamples.Fhir.create().observation());
    assertThat(
            Iterables.getOnlyElement(
                    controller
                        .searchByPatientAndCategory(
                            "",
                            "1002003004V666666",
                            "laboratory",
                            new String[] {"2012-12-24T14:12:00Z"},
                            1,
                            1)
                        .entry())
                .resource())
        .isEqualTo(DatamartObservationSamples.Fhir.create().observation());
    assertThat(
            Iterables.getOnlyElement(
                    controller
                        .searchByPatientAndCategory(
                            "",
                            "1002003004V666666",
                            "laboratory",
                            new String[] {"gt2012-12-23", "lt2012-12-25"},
                            1,
                            1)
                        .entry())
                .resource())
        .isEqualTo(DatamartObservationSamples.Fhir.create().observation());
  }

  @Test
  public void searchByPatientAndCode() {
    IdentityService ids = mock(IdentityService.class);
    ObservationController controller =
        new ObservationController(
            true,
            null,
            null,
            new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
            repository,
            WitnessProtection.builder().identityService(ids).build());
    DatamartObservation dm = DatamartObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    setUpIds(ids, dm);
    assertThat(controller.searchByPatientAndCode("", "1002003004V666666", "1989-3", 2, 1).entry())
        .isEmpty();
    assertThat(controller.searchByPatientAndCode("", "55555", "1989-3", 1, 1).entry()).isEmpty();
    assertThat(controller.searchByPatientAndCode("", "1002003004V666666", "55555", 1, 1).entry())
        .isEmpty();
    assertThat(
            Iterables.getOnlyElement(
                    controller
                        .searchByPatientAndCode("", "1002003004V666666", "1989-3", 1, 1)
                        .entry())
                .resource())
        .isEqualTo(DatamartObservationSamples.Fhir.create().observation());
    assertThat(
            Iterables.getOnlyElement(
                    controller
                        .searchByPatientAndCode("", "1002003004V666666", "8480-6, 1989-3", 1, 1)
                        .entry())
                .resource())
        .isEqualTo(DatamartObservationSamples.Fhir.create().observation());
    assertThat(
            Iterables.getOnlyElement(
                    controller
                        .searchByPatientAndCode("", "1002003004V666666", " 1989-3, 1989-3", 1, 1)
                        .entry())
                .resource())
        .isEqualTo(DatamartObservationSamples.Fhir.create().observation());
  }
}
