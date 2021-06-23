package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatamartPractitionerRoleTest {
  @Test
  @SneakyThrows
  void assertReadable() {
    DatamartPractitionerRole dm =
        createMapper()
            .readValue(
                getClass().getResourceAsStream("datamart-practitioner-role.json"),
                DatamartPractitionerRole.class);
    assertThat(dm)
        .isEqualTo(
            PractitionerRoleSamples.Datamart.create()
                .practitionerRole("12345:P", "12345:S", "12345:I", "12345:L"));
  }

  @Test
  void nullValuesHandled() {
    var practitionerRoleWithNulls = DatamartPractitionerRole.builder().build();
    assertThat(practitionerRoleWithNulls).isNotNull();
    assertThat(practitionerRoleWithNulls.cdwId()).isNull();
    assertThat(practitionerRoleWithNulls.npi()).isEqualTo(Optional.empty());
    assertThat(practitionerRoleWithNulls.specialty()).isEqualTo(new ArrayList<>());
    assertThat(practitionerRoleWithNulls.role()).isEqualTo(List.of());
    assertThat(practitionerRoleWithNulls.healthCareService()).isEqualTo(Optional.empty());
    assertThat(practitionerRoleWithNulls.location()).isEqualTo(List.of());
    assertThat(practitionerRoleWithNulls.managingOrganization()).isEqualTo(Optional.empty());
    assertThat(practitionerRoleWithNulls.specialty()).isEqualTo(new ArrayList<>());
    assertThat(practitionerRoleWithNulls.practitioner()).isEqualTo(Optional.empty());
    practitionerRoleWithNulls.specialty(
        List.of(DatamartPractitionerRole.Specialty.builder().build()));
    var specialty = Iterables.getOnlyElement(practitionerRoleWithNulls.specialty());
    assertThat(practitionerRoleWithNulls).isNotNull();
    assertThat(specialty.areaOfSpecialization()).isEqualTo(Optional.empty());
    assertThat(specialty.classification()).isEqualTo(Optional.empty());
    assertThat(specialty.providerType()).isEqualTo(Optional.empty());
    assertThat(specialty.specialtyCode()).isEqualTo(Optional.empty());
    assertThat(specialty.vaCode()).isEqualTo(Optional.empty());
    assertThat(specialty.x12Code()).isEqualTo(Optional.empty());
  }
}
