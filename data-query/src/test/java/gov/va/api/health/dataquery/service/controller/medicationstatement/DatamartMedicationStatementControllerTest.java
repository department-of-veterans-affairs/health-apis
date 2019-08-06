package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;

import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatementSamples.Fhir;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
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
public class DatamartMedicationStatementControllerTest {
  @Autowired private TestEntityManager entityManager;
  @Autowired private MedicationStatementRepository repository;
  private IdentityService ids = mock(IdentityService.class);


  @SneakyThrows
  private MedicationStatementEntity asEntity(DatamartMedicationStatement dm) {
    return MedicationStatementEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  MedicationStatementController controller() {
    return new MedicationStatementController(
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

  public void mockMedicationStatementIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("MEDICATION_STATEMENT").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  @Test
  public void readRaw() {
    DatamartMedicationStatement dm = DatamartMedicationStatementSamples.Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("1", dm.cdwId());
    String json = controller().readRaw("1");
    assertThat(toObject(json)).isEqualTo(dm);
  }

  @Test
  public void read() {
    DatamartMedicationStatement dm = DatamartMedicationStatementSamples.Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("1", dm.cdwId());
    MedicationStatement actual = controller().read("true", "1");
    assertThat(json(actual)).isEqualTo(json(Fhir.create().medicationStatement("1")));
  }

  @SneakyThrows
  private DatamartMedicationStatement toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedicationStatement.class);
  }
}
