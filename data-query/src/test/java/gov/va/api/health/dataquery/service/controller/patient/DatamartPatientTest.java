package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.argonaut.api.resources.Patient.Gender;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.ids.api.IdentityService;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
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
  public void basic() {
    DatamartData dm = DatamartData.create();
    FhirData fhir = FhirData.from(dm);
    entityManager.persistAndFlush(dm.search());
    entityManager.persistAndFlush(dm.entity());

    PatientController controller = controller();
    Patient patient = controller.read("true", dm.icn());
    assertThat(patient).isEqualTo(fhir.patient());
  }

  public PatientController controller() {
    return new PatientController(
        null,
        null,
        null,
        WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
        entityManager.getEntityManager());
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

    PatientController controller = controller();

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

  @Test
  public void readRaw() {
    DatamartData dm = DatamartData.create();
    entityManager.persistAndFlush(dm.search());
    entityManager.persistAndFlush(dm.entity());
    String json = controller().readRaw(dm.icn());
    assertThat(PatientEntity.builder().payload(json).build().asDatamartPatient())
        .isEqualTo(dm.patient());
  }

  @Builder
  @Value
  private static class DatamartData {
    @Builder.Default String icn = "1011537977V693883";
    @Builder.Default String ssn = "000001234";
    @Builder.Default String name = "TEST,PATIENT ONE";
    @Builder.Default String firstName = "PATIENT ONE";
    @Builder.Default String lastName = "TEST";
    @Builder.Default String birthDateTime = "1925-01-01T00:00:00";

    static DatamartData create() {
      return DatamartData.builder().build();
    }

    @SneakyThrows
    PatientEntity entity() {
      return PatientEntity.builder()
          .icn(icn)
          .payload(JacksonConfig.createMapper().writeValueAsString(patient()))
          .search(search())
          .build();
    }

    DatamartPatient patient() {
      return DatamartPatient.builder()
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
          .maritalStatus(DatamartPatient.MaritalStatus.builder().abbrev("UNK").build())
          .ethnicity(DatamartPatient.Ethnicity.builder().hl7("2135-2").build())
          .race(asList(DatamartPatient.Race.builder().display("asian").build()))
          .telecom(
              asList(
                  DatamartPatient.Telecom.builder()
                      .type("confidential")
                      .phoneNumber("021234567")
                      .build(),
                  DatamartPatient.Telecom.builder()
                      .type("patient cell phone")
                      .phoneNumber("011 9991234567")
                      .build(),
                  DatamartPatient.Telecom.builder()
                      .type("patient residence")
                      .phoneNumber("(0900)000-1234")
                      .workPhoneNumber("(0900)000-1234")
                      .build(),
                  DatamartPatient.Telecom.builder()
                      .type("temporary")
                      .phoneNumber("(02)771-9342")
                      .build()))
          .address(
              asList(
                  DatamartPatient.Address.builder()
                      .type("Temporary")
                      .street1("HOTEL PASAY")
                      .street2("232 KAMAGONG ST")
                      .city("PASAY")
                      .state("*Missing*")
                      .postalCode("01300")
                      .country("PHILIPPINES")
                      .build(),
                  DatamartPatient.Address.builder()
                      .type("Confidential")
                      .street1("1501 ROXAS BLVD")
                      .city("PASAY CITY")
                      .state("*Missing*")
                      .postalCode("01302")
                      .country("PHILIPPINES")
                      .build(),
                  DatamartPatient.Address.builder()
                      .type("Patient")
                      .street1("55555 ROXAS BOULEVARD")
                      .city("PASAY CITY")
                      .state("*Missing*")
                      .postalCode("01302")
                      .country("PHILIPPINES")
                      .build()))
          .build();
    }

    PatientSearchEntity search() {
      return PatientSearchEntity.builder()
          .icn(icn)
          .name(name)
          .firstName(firstName)
          .lastName(lastName)
          .gender("M")
          .birthDateTime(parseInstant(birthDateTime))
          .build();
    }
  }

  @Builder
  @Value
  private static class FhirData {
    @Builder.Default String icn = "1011537977V693883";
    @Builder.Default String name = "TEST,PATIENT ONE";
    @Builder.Default String firstName = "PATIENT ONE";
    @Builder.Default String lastName = "TEST";

    static FhirData from(DatamartData dm) {
      return FhirData.builder()
          .icn(dm.icn())
          .name(dm.name())
          .firstName(dm.firstName())
          .lastName(dm.lastName())
          .build();
    }

    public Patient patient() {
      return Patient.builder()
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
                                          .code("2028-9")
                                          .display("Asian")
                                          .build())
                                  .build(),
                              Extension.builder().url("text").valueString("Asian").build()))
                      .build(),
                  Extension.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
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
                      .build(),
                  Extension.builder()
                      .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
                      .valueCode("M")
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
          .telecom(
              asList(
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("011 9991234567")
                      .use(ContactPoint.ContactPointUse.mobile)
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("09000001234")
                      .use(ContactPoint.ContactPointUse.home)
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("027719342")
                      .use(ContactPoint.ContactPointUse.temp)
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("09000001234")
                      .use(ContactPoint.ContactPointUse.work)
                      .build()))
          .gender(Gender.male)
          .birthDate("1925-01-01")
          .deceasedBoolean(false)
          .address(
              asList(
                  Address.builder()
                      .line(asList("HOTEL PASAY", "232 KAMAGONG ST"))
                      .city("PASAY")
                      .state("*Missing*")
                      .postalCode("01300")
                      .country("PHILIPPINES")
                      .build(),
                  Address.builder()
                      .line(asList("1501 ROXAS BLVD"))
                      .city("PASAY CITY")
                      .state("*Missing*")
                      .postalCode("01302")
                      .country("PHILIPPINES")
                      .build(),
                  Address.builder()
                      .line(asList("55555 ROXAS BOULEVARD"))
                      .city("PASAY CITY")
                      .state("*Missing*")
                      .postalCode("01302")
                      .country("PHILIPPINES")
                      .build()))
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
          .build();
    }
  }
}
