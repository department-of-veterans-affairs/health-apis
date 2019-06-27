package gov.va.api.health.dataquery.service.controller.patient;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.argonaut.api.resources.Patient.Gender;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Transformers;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient.Ethnicity;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient.MaritalStatus;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient.Race;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.IdentityService;
import java.time.ZoneOffset;
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
  public void basic() {
    String icn = "1011537977V693883";
    String ssn = "000001234";
    String name = "TEST,PATIENT ONE";
    String firstName = "PATIENT ONE";
    String lastName = "TEST";
    String birthDateTime = "1925-01-01T00:00:00";
    PatientSearchEntity search =
        PatientSearchEntity.builder()
            .icn(icn)
            .name(name)
            .firstName(firstName)
            .lastName(lastName)
            .birthDateTime(Transformers.parseLocalDateTime(birthDateTime).toInstant(ZoneOffset.UTC))
            .gender("M")
            .build();
    entityManager.persistAndFlush(search);

    PatientEntity entity =
        PatientEntity.builder()
            .icn(icn)
            .payload(
                JacksonConfig.createMapper()
                    .writeValueAsString(
                        DatamartPatient.builder()
                            .objectType("Patient")
                            .objectVersion(1)
                            .fullIcn(icn)
                            .ssn(ssn)
                            .name(name)
                            .firstName(firstName)
                            .lastName(lastName)
                            .birthDateTime(birthDateTime)
                            .deceased("N")
                            .gender("M")
                            .maritalStatus(
                                MaritalStatus.builder()
                                    .display("UNKNOWN")
                                    .abbrev("UNK")
                                    .code("U")
                                    .build())
                            .ethnicity(
                                Ethnicity.builder()
                                    .display("HISPANIC OR LATINO")
                                    .abbrev("H")
                                    .hl7("2135-2")
                                    .build())
                            .race(asList(Race.builder().display("ASIAN").abbrev("A").build()))
                            .build()))
            .search(search)
            .build();
    entityManager.persistAndFlush(entity);

    PatientController controller =
        new PatientController(
            null,
            null,
            null,
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());

    Patient patient = controller.read("true", icn);
    // System.out.println(JacksonConfig.createMapper().writeValueAsString(patient));
    assertThat(patient)
        .isEqualTo(
            Patient.builder()
                .id(icn)
                .resourceType("Patient")
                .extension(
                    asList(
                        Extension.builder()
                            .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
                            .extension(
                                asList(
                                    Extension.builder()
                                        .url("ombCategory")
                                        .valueCoding(
                                            Coding.builder()
                                                .system("http://hl7.org/fhir/v3/Race")
                                                .code("A")
                                                .display("ASIAN")
                                                .build())
                                        .build(),
                                    Extension.builder().url("text").valueString("ASIAN").build()))
                            .build(),
                        Extension.builder()
                            .url(
                                "http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
                            .extension(
                                asList(
                                    Extension.builder()
                                        .url("ombCategory")
                                        .valueCoding(
                                            Coding.builder()
                                                .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                                                .code("2135-2")
                                                .display("Hispanic or Latino")
                                                .build())
                                        .build(),
                                    Extension.builder()
                                        .url("text")
                                        .valueString("Hispanic or Latino")
                                        .build()))
                            .build()))
                .identifier(
                    asList(
                        Identifier.builder()
                            .use(Identifier.IdentifierUse.usual)
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
                            .build(),
                        Identifier.builder()
                            .use(Identifier.IdentifierUse.official)
                            .type(
                                CodeableConcept.builder()
                                    .coding(
                                        asList(
                                            Coding.builder()
                                                .system("http://hl7.org/fhir/v2/0203")
                                                .code("SB")
                                                .build()))
                                    .build())
                            .system("http://hl7.org/fhir/sid/us-ssn")
                            .value("000001234")
                            .assigner(
                                Reference.builder()
                                    .display("United States Social Security Number")
                                    .build())
                            .build()))
                .name(
                    asList(
                        HumanName.builder()
                            .use(HumanName.NameUse.usual)
                            .text(name)
                            .family(asList(lastName))
                            .given(asList(firstName))
                            .build()))
                .gender(Gender.male)
                .birthDate("1925-01-01")
                .deceasedBoolean(false)
                .maritalStatus(
                    CodeableConcept.builder()
                        .coding(
                            asList(
                                Coding.builder()
                                    .system("http://hl7.org/fhir/v3/NullFlavor")
                                    .code("UNK")
                                    .display("unknown")
                                    .build()))
                        .build())
                .build());
  }

  @Test
  @SneakyThrows
  public void empty() {
    String icn = "1011537977V693883";
    PatientSearchEntity search = PatientSearchEntity.builder().icn(icn).build();
    entityManager.persistAndFlush(search);

    PatientEntity entity =
        PatientEntity.builder()
            .icn(icn)
            .payload(
                JacksonConfig.createMapper()
                    .writeValueAsString(DatamartPatient.builder().fullIcn(icn).build()))
            .search(search)
            .build();
    entityManager.persistAndFlush(entity);

    PatientController controller =
        new PatientController(
            null,
            null,
            null,
            WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
            entityManager.getEntityManager());

    Patient patient = controller.read("true", icn);
    assertThat(patient)
        .isEqualTo(
            Patient.builder()
                .id(icn)
                .resourceType("Patient")
                .identifier(
                    asList(
                        Identifier.builder()
                            .use(Identifier.IdentifierUse.usual)
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
