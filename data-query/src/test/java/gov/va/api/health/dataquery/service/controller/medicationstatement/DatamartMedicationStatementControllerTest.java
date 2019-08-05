package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatementTransformerTest.Datamart;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartMedicationStatementControllerTest {

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private MedicationStatementRepository repository;

  @Autowired private TestEntityManager entityManager;

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

  public void mockMedicationStatementIdentity(String publicId, String cdwId) {
    when(ids.lookup(publicId))
        .thenReturn(
            List.of(
                ResourceIdentity.builder()
                    .system("CDW")
                    .resource("CONDITION")
                    .identifier(cdwId)
                    .build()));
  }

  @Test
  public void readRaw() {
    DatamartMedicationStatement dm = Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("x", dm.cdwId());
    String json = controller().readRaw("x");
    assertThat(toObject(json)).isEqualTo(dm);
  }

  @Test
  public void read() {
    DatamartMedicationStatement dm = Datamart.create().medicationStatement();
    repository.save(asEntity(dm));
    mockMedicationStatementIdentity("x", dm.cdwId());
    MedicationStatement actual = controller().read("true", "x");
    assertThat(actual).isEqualTo(DatamartMedicationStatementSamples.Fhir.create().medicationStatement("x"));
  }

  @SneakyThrows
  private DatamartMedicationStatement toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedicationStatement.class);
  }
}
