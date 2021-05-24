package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner.Telecom.System;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner.Telecom.Use;
import gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.practitionerrole.PractitionerRoleSamples.R4;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4PractitionerRoleTransformerTest {
  @Test
  public void empty() {
    assertThat(
            R4PractitionerRoleTransformer.builder()
                .datamart(DatamartPractitioner.builder().build())
                .build()
                .toFhir())
        .isEqualTo(PractitionerRole.builder().resourceType("PractitionerRole").build());
  }

  @Test
  public void otherEmpty() {
    assertThat(
            R4PractitionerRoleTransformer.specialty(
                Optional.of(DatamartPractitioner.PractitionerRole.builder().build())))
        .isNull();

    DatamartPractitioner.PractitionerRole.Specialty emptySpecialty =
        DatamartPractitioner.PractitionerRole.Specialty.builder().x12Code(Optional.of(" ")).build();
    assertThat(R4PractitionerRoleTransformer.specialty(emptySpecialty)).isNull();

    assertThat(
            R4PractitionerRoleTransformer.period(
                Optional.of(DatamartPractitioner.PractitionerRole.builder().build())))
        .isNull();

    assertThat(
            R4PractitionerRoleTransformer.healthcareService(
                Optional.of(
                    DatamartPractitioner.PractitionerRole.builder()
                        .healthCareService(Optional.of(" "))
                        .build())))
        .isNull();
  }

  @Test
  public void specialty() {
    // x12 code used first, then vaCode, then specialty code
    assertThat(
            R4PractitionerRoleTransformer.specialty(
                Optional.of(
                    DatamartPractitioner.PractitionerRole.builder()
                        .specialty(
                            asList(
                                // use x12
                                DatamartPractitioner.PractitionerRole.Specialty.builder()
                                    .vaCode(Optional.of("v1"))
                                    .x12Code(Optional.of("x1"))
                                    .specialtyCode(Optional.of("s1"))
                                    .build(),
                                // use va
                                DatamartPractitioner.PractitionerRole.Specialty.builder()
                                    .vaCode(Optional.of("v2"))
                                    .specialtyCode(Optional.of("s2"))
                                    .build(),
                                // use specialty
                                DatamartPractitioner.PractitionerRole.Specialty.builder()
                                    .specialtyCode(Optional.of("s3"))
                                    .build()))
                        .build())))
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
  public void telecom() {
    List<DatamartPractitioner.Telecom> phoneAndEmail =
        List.of(
            DatamartPractitioner.Telecom.builder()
                .system(System.phone)
                .value("333-333-3333")
                .use(Use.work)
                .build(),
            DatamartPractitioner.Telecom.builder()
                .system(System.email)
                .value("foo@example.com")
                .use(Use.work)
                .build(),
            DatamartPractitioner.Telecom.builder()
                .system(System.fax)
                .value("444-444-4444")
                .use(Use.work)
                .build(),
            DatamartPractitioner.Telecom.builder()
                .system(System.pager)
                .value("5-555")
                .use(Use.work)
                .build());

    assertThat(R4PractitionerRoleTransformer.telecoms(phoneAndEmail))
        .isEqualTo(
            List.of(
                ContactPoint.builder()
                    .system(ContactPointSystem.phone)
                    .value("333-333-3333")
                    .build(),
                ContactPoint.builder()
                    .system(ContactPointSystem.email)
                    .value("foo@example.com")
                    .build(),
                ContactPoint.builder().system(ContactPointSystem.fax).value("444-444-4444").build(),
                ContactPoint.builder().system(ContactPointSystem.pager).value("5-555").build()));

    List<DatamartPractitioner.Telecom> nullSystem =
        List.of(DatamartPractitioner.Telecom.builder().system(null).value("333-333-3333").build());
    assertThat(R4PractitionerRoleTransformer.telecoms(nullSystem))
        .isEqualTo(List.of(ContactPoint.builder().system(null).value("333-333-3333").build()));
  }

  @Test
  public void toFhir() {
    assertThat(
            R4PractitionerRoleTransformer.builder()
                .datamart(Datamart.create().practitioner("999", "998", "997"))
                .build()
                .toFhir())
        .isEqualTo(R4.create().practitionerRole("999", "997", "998"));
  }
}
