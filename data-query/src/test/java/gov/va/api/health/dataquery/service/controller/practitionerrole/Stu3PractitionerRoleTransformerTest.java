package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class Stu3PractitionerRoleTransformerTest {
  @Test
  public void empty() {
    assertThat(
            Stu3PractitionerRoleTransformer.builder()
                .datamart(DatamartPractitioner.builder().build())
                .build()
                .toFhir())
        .isEqualTo(PractitionerRole.builder().resourceType("PractitionerRole").build());

    assertThat(
            Stu3PractitionerRoleTransformer.specialty(
                Optional.of(DatamartPractitioner.PractitionerRole.builder().build())))
        .isNull();

    assertThat(Stu3PractitionerRoleTransformer.specialty(" ")).isNull();

    assertThat(
            Stu3PractitionerRoleTransformer.period(
                Optional.of(DatamartPractitioner.PractitionerRole.builder().build())))
        .isNull();

    assertThat(
            Stu3PractitionerRoleTransformer.healthCareService(
                Optional.of(
                    DatamartPractitioner.PractitionerRole.builder()
                        .healthCareService(Optional.of(" "))
                        .build())))
        .isNull();
  }

  @Test
  public void specialty() {
    // x12 code used first
    assertThat(
            Stu3PractitionerRoleTransformer.specialty(
                Optional.of(
                    DatamartPractitioner.PractitionerRole.builder()
                        .specialty(
                            asList(
                                DatamartPractitioner.PractitionerRole.Specialty.builder()
                                    .vaCode(Optional.of("v1"))
                                    .specialtyCode(Optional.of("s1"))
                                    .build(),
                                DatamartPractitioner.PractitionerRole.Specialty.builder()
                                    .x12Code(Optional.of("x2"))
                                    .vaCode(Optional.of("v2"))
                                    .specialtyCode(Optional.of("s2"))
                                    .build()))
                        .build())))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .system("http://nucc.org/provider-taxonomy")
                            .code("x2")
                            .build()))
                .build());

    // if no x12 code, use va code
    assertThat(
            Stu3PractitionerRoleTransformer.specialty(
                Optional.of(
                    DatamartPractitioner.PractitionerRole.builder()
                        .specialty(
                            asList(
                                DatamartPractitioner.PractitionerRole.Specialty.builder()
                                    .specialtyCode(Optional.of("s1"))
                                    .build(),
                                DatamartPractitioner.PractitionerRole.Specialty.builder()
                                    .vaCode(Optional.of("v2"))
                                    .specialtyCode(Optional.of("s2"))
                                    .build()))
                        .build())))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .system("http://nucc.org/provider-taxonomy")
                            .code("v2")
                            .build()))
                .build());

    // if no x12 code or va code, use specialty code
    assertThat(
            Stu3PractitionerRoleTransformer.specialty(
                Optional.of(
                    DatamartPractitioner.PractitionerRole.builder()
                        .specialty(
                            asList(
                                DatamartPractitioner.PractitionerRole.Specialty.builder().build(),
                                DatamartPractitioner.PractitionerRole.Specialty.builder()
                                    .specialtyCode(Optional.of("s2"))
                                    .build()))
                        .build())))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .system("http://nucc.org/provider-taxonomy")
                            .code("s2")
                            .build()))
                .build());
  }
}
