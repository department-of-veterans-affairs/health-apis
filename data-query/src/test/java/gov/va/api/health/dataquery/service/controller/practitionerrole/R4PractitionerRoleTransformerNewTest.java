package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.resources.PractitionerRole;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class R4PractitionerRoleTransformerNewTest {
  @Test
  public void completePractitionerRole() {
    var specialtyValue =
        DatamartPractitionerRole.Specialty.builder()
            .vaCode(Optional.of("v1"))
            .x12Code(Optional.of("x1"))
            .specialtyCode(Optional.of("s1"))
            .providerType(Optional.of("provider"))
            .classification(Optional.of("class"))
            .areaOfSpecialization(Optional.of("area"))
            .build();
    var specialtyList = List.of(specialtyValue);
    var locationList =
        List.of(
            DatamartReference.builder().type(Optional.of("Location")).build(),
            DatamartReference.builder()
                .type(Optional.of("Location"))
                .display(Optional.of("Display"))
                .build());
    var localDate = LocalDate.now();
    var period =
        DatamartPractitionerRole.Period.builder()
            .start(Optional.of(localDate))
            .end(Optional.of(localDate.plusDays(1)))
            .build();
    var complete =
        DatamartPractitionerRole.builder()
            .cdwId("123")
            .npi(Optional.of("npi"))
            .managingOrganization(Optional.of(DatamartReference.builder().build()))
            .role(Optional.of(DatamartCoding.builder().build()))
            .specialty(specialtyList)
            .period(Optional.of(period))
            .location(locationList)
            .healthCareService(Optional.of("Some Service"))
            .build();
    assertThat(complete).isNotNull();
    assertThat(complete.managingOrganization()).isNotNull();
    assertThat(complete.period().get()).isEqualTo(period);
    assertThat(complete.period().get().start()).isEqualTo(Optional.of(localDate));
    assertThat(complete.period().get().end()).isEqualTo(Optional.of(localDate.plusDays(1)));
    assertThat(complete.cdwId()).isEqualTo("123");
    assertThat(complete.npi().get()).isEqualTo("npi");
    assertThat(complete.specialty()).isEqualTo(specialtyList);
    assertThat(complete.role()).isEqualTo(Optional.of(DatamartCoding.builder().build()));
    var specialty = specialtyList.get(0);
    assertThat(specialty.areaOfSpecialization()).isEqualTo(Optional.of("area"));
    assertThat(specialty.classification()).isEqualTo(Optional.of("class"));
    assertThat(specialty.providerType()).isEqualTo(Optional.of("provider"));
    assertThat(specialty.specialtyCode()).isEqualTo(Optional.of("s1"));
    assertThat(specialty.vaCode()).isEqualTo(Optional.of("v1"));
    assertThat(specialty.x12Code()).isEqualTo(Optional.of("x1"));
    assertThat(complete.location()).isEqualTo(locationList);
    assertThat(complete.healthCareService().get()).isEqualTo("Some Service");
  }

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
  public void nullValuesHandled() {
    var practitionerRoleWithNulls = DatamartPractitionerRole.builder().build();
    assertThat(practitionerRoleWithNulls).isNotNull();
    assertThat(practitionerRoleWithNulls.managingOrganization()).isEqualTo(Optional.empty());
    assertThat(practitionerRoleWithNulls.period()).isEqualTo(Optional.empty());
    assertThat(practitionerRoleWithNulls.cdwId()).isNull();
    assertThat(practitionerRoleWithNulls.npi()).isEqualTo(Optional.empty());
    assertThat(practitionerRoleWithNulls.specialty()).isEqualTo(new ArrayList<>());
    assertThat(practitionerRoleWithNulls.role()).isEqualTo(Optional.empty());
    assertThat(practitionerRoleWithNulls.specialty()).isEqualTo(new ArrayList<>());
    var period = Optional.of(DatamartPractitionerRole.Period.builder().build());
    practitionerRoleWithNulls.period(period);
    assertThat(practitionerRoleWithNulls).isNotNull();
    assertThat(practitionerRoleWithNulls.period().get().start()).isEqualTo(Optional.empty());
    assertThat(practitionerRoleWithNulls.period().get().end()).isEqualTo(Optional.empty());
    var specialtyList = List.of(DatamartPractitionerRole.Specialty.builder().build());
    practitionerRoleWithNulls.specialty(specialtyList);
    var specialty = practitionerRoleWithNulls.specialty().get(0);
    assertThat(practitionerRoleWithNulls).isNotNull();
    assertThat(specialty.areaOfSpecialization()).isEqualTo(Optional.empty());
    assertThat(specialty.classification()).isEqualTo(Optional.empty());
    assertThat(specialty.providerType()).isEqualTo(Optional.empty());
    assertThat(specialty.specialtyCode()).isEqualTo(Optional.empty());
    assertThat(specialty.vaCode()).isEqualTo(Optional.empty());
    assertThat(specialty.x12Code()).isEqualTo(Optional.empty());
  }

  @Test
  public void otherEmpty() {
    assertThat(DatamartPractitionerRole.builder().build()).isNotNull();
    assertThat(DatamartPractitionerRole.builder().healthCareService(Optional.of(" ")).build())
        .isNotNull();
  }

  @Test
  public void specialty() {
    var pr =
        DatamartPractitionerRole.builder()
            .specialty(
                asList(
                    DatamartPractitionerRole.Specialty.builder()
                        .vaCode(Optional.of("v1"))
                        .x12Code(Optional.of("x1"))
                        .specialtyCode(Optional.of("s1"))
                        .build(),
                    DatamartPractitionerRole.Specialty.builder()
                        .vaCode(Optional.of("v2"))
                        .specialtyCode(Optional.of("s2"))
                        .build(),
                    DatamartPractitionerRole.Specialty.builder()
                        .specialtyCode(Optional.of("s3"))
                        .build()))
            .build();
    assertThat(pr).isNotNull();
  }

  @Test
  public void telecom() {
    List<DatamartPractitioner.Telecom> phoneAndEmail =
        List.of(
            DatamartPractitioner.Telecom.builder()
                .system(DatamartPractitioner.Telecom.System.phone)
                .value("333-333-3333")
                .use(DatamartPractitioner.Telecom.Use.work)
                .build(),
            DatamartPractitioner.Telecom.builder()
                .system(DatamartPractitioner.Telecom.System.email)
                .value("foo@example.com")
                .use(DatamartPractitioner.Telecom.Use.work)
                .build(),
            DatamartPractitioner.Telecom.builder()
                .system(DatamartPractitioner.Telecom.System.fax)
                .value("444-444-4444")
                .use(DatamartPractitioner.Telecom.Use.work)
                .build(),
            DatamartPractitioner.Telecom.builder()
                .system(DatamartPractitioner.Telecom.System.pager)
                .value("5-555")
                .use(DatamartPractitioner.Telecom.Use.work)
                .build());
    assertThat(R4PractitionerRoleTransformer.telecoms(phoneAndEmail))
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
    List<DatamartPractitioner.Telecom> nullSystem =
        List.of(DatamartPractitioner.Telecom.builder().system(null).value("333-333-3333").build());
    assertThat(R4PractitionerRoleTransformer.telecoms(nullSystem))
        .isEqualTo(List.of(ContactPoint.builder().system(null).value("333-333-3333").build()));
  }

  @Test
  public void toFhir() {
    assertThat(
            R4PractitionerRoleTransformer.builder()
                .datamart(
                    PractitionerRoleSamples.Datamart.create().practitioner("999", "998", "997"))
                .build()
                .toFhir())
        .isEqualTo(PractitionerRoleSamples.R4.create().practitionerRole("999", "997", "998"));
  }
}
