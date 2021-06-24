package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4PractitionerRoleTransformerTest {
  private static R4PractitionerRoleTransformer transformer(DatamartPractitionerRole dm) {
    return R4PractitionerRoleTransformer.builder().datamart(dm).build();
  }

  @Test
  void empty() {
    assertThat(
            R4PractitionerRoleTransformer.builder()
                .datamart(DatamartPractitionerRole.builder().build())
                .build()
                .toFhir())
        .isEqualTo(PractitionerRole.builder().build());
  }

  @Test
  void otherEmpty() {
    assertThat(transformer(DatamartPractitionerRole.builder().build()).specialties()).isNull();
    DatamartPractitionerRole.Specialty emptySpecialty =
        DatamartPractitionerRole.Specialty.builder().x12Code(Optional.of(" ")).build();

    assertThat(R4PractitionerRoleTransformer.specialty(emptySpecialty)).isNull();

    assertThat(
            transformer(
                    DatamartPractitionerRole.builder().healthCareService(Optional.of(" ")).build())
                .healthcareService())

        .isNull();
  }

  @Test
  void specialty() {
    // x12 code used first, then vaCode, then specialty code
    assertThat(
            transformer(
                    DatamartPractitionerRole.builder()

                        .specialty(
                            asList( // use x12
                                DatamartPractitionerRole.Specialty.builder()

                                    .vaCode(Optional.of("v1"))
                                    .x12Code(Optional.of("x1"))
                                    .specialtyCode(Optional.of("s1"))
                                    .build(), // use va
                                DatamartPractitionerRole.Specialty.builder()

                                    .vaCode(Optional.of("v2"))
                                    .specialtyCode(Optional.of("s2"))
                                    .build(), // use specialty
                                DatamartPractitionerRole.Specialty.builder()

                                    .specialtyCode(Optional.of("s3"))
                                    .build()))
                        .build())
                .specialties())
        .isEqualTo(
            List.of(
                CodeableConcept.builder()
                    .coding(
                        List.of(
                            Coding.builder()
                                .system("http://nucc.org/provider-taxonomy")
                                .code("x1")
                                .build()))
                    .build(),
                CodeableConcept.builder()
                    .coding(
                        List.of(
                            Coding.builder()
                                .system("http://nucc.org/provider-taxonomy")
                                .code("v2")
                                .build()))
                    .build(),
                CodeableConcept.builder()
                    .coding(
                        List.of(
                            Coding.builder()
                                .system("http://nucc.org/provider-taxonomy")
                                .code("s3")
                                .build()))
                    .build()));
  }

  @Test
  void telecom() {
    List<DatamartPractitionerRole.Telecom> phoneAndEmail =
        List.of(
            DatamartPractitionerRole.Telecom.builder()
                .system(DatamartPractitionerRole.Telecom.System.phone)
                .value("333-333-3333")
                .use(DatamartPractitionerRole.Telecom.Use.work)
                .build(),
            DatamartPractitionerRole.Telecom.builder()
                .system(DatamartPractitionerRole.Telecom.System.email)
                .value("foo@example.com")
                .use(DatamartPractitionerRole.Telecom.Use.work)
                .build(),
            DatamartPractitionerRole.Telecom.builder()
                .system(DatamartPractitionerRole.Telecom.System.fax)
                .value("444-444-4444")
                .use(DatamartPractitionerRole.Telecom.Use.work)
                .build(),
            DatamartPractitionerRole.Telecom.builder()
                .system(DatamartPractitionerRole.Telecom.System.pager)
                .value("5-555")
                .use(DatamartPractitionerRole.Telecom.Use.work)
                .build());
    assertThat(
            transformer(DatamartPractitionerRole.builder().telecom(phoneAndEmail).build())
                .telecoms())
        .isEqualTo(
            List.of(
                ContactPoint.builder()
                    .system(ContactPoint.ContactPointSystem.phone)
                    .value("333-333-3333")
                    .build(),
                ContactPoint.builder()
                    .system(ContactPoint.ContactPointSystem.email)
                    .value("foo@example.com")
                    .build(),
                ContactPoint.builder()
                    .system(ContactPoint.ContactPointSystem.fax)
                    .value("444-444-4444")
                    .build(),
                ContactPoint.builder()
                    .system(ContactPoint.ContactPointSystem.pager)
                    .value("5-555")
                    .build()));
    assertThat(
            transformer(
                    DatamartPractitionerRole.builder()
                        .telecom(
                            List.of(
                                DatamartPractitionerRole.Telecom.builder()
                                    .system(null)
                                    .value("333-333-3333")
                                    .build()))
                        .build())
                .telecoms())
        .isEqualTo(List.of(ContactPoint.builder().system(null).value("333-333-3333").build()));
  }

  @Test
  void toFhir() {
    assertThat(
            R4PractitionerRoleTransformer.builder()
                .datamart(
                    PractitionerRoleSamples.Datamart.create()
                        .practitionerRole("111:P", "222:S", "333:I", "444:L"))
                .build()
                .toFhir())
        .isEqualTo(
            PractitionerRoleSamples.R4
                .create()
                .practitionerRole("111:P", "222:S", "333:I", "444:L"));
  }
}