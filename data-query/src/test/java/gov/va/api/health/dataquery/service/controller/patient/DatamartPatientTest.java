package gov.va.api.health.dataquery.service.controller.patient;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.IdentityService;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public final class DatamartPatientTest {
  @Autowired private TestEntityManager entityManager;

  @Test
  @SneakyThrows
  public void doIt() {
    String icn = "111222333V000999";
    DatamartPatient datamart =
        DatamartPatient.builder().objectType("Patient").objectVersion(1).fullIcn(icn).build();
    PatientSearchEntity search = PatientSearchEntity.builder().icn(icn).build();
    entityManager.persistAndFlush(search);
    PatientEntity entity =
        PatientEntity.builder()
            .icn(icn)
            .payload(JacksonConfig.createMapper().writeValueAsString(datamart))
            .search(search)
            .build();
    entityManager.persistAndFlush(entity);

    IdentityService identityService = mock(IdentityService.class);
    WitnessProtection witnessProtection =
        WitnessProtection.builder().identityService(identityService).build();
    PatientController controller =
        new PatientController(
            null, null, null, witnessProtection, entityManager.getEntityManager());

    Patient patient = controller.read("true", icn);
    assertThat(patient)
        .isEqualTo(
            Patient.builder()
                .id(icn)
                .resourceType("Patient")
                .identifier(
                    asList(
                        Identifier.builder()
                            .use(IdentifierUse.usual)
                            .type(
                                CodeableConcept.builder()
                                    .coding(
                                        asList(
                                            Coding.builder()
                                                .system("http://hl7.org/fhir/v2/0203")
                                                .code("MR")
                                                .build()))
                                    .build())
                            .system("http://va.gov/mvi")
                            .value(icn)
                            .assigner(Reference.builder().display("Master Veteran Index").build())
                            .build()))
                .build());
  }
}
