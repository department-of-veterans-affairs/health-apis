package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class PractitionerRoleEntityTest {
  @Test
  void asDatamartPractitonerRole() {
    PractitionerRoleEntity entity =
        PractitionerRoleSamples.Datamart.create().entity("111:P", "222:S", "333:I", "444:L");
    assertThat(entity.asDatamartPractitionerRole())
        .isEqualTo(
            PractitionerRoleSamples.Datamart.create()
                .practitionerRole("111:P", "222:S", "333:I", "444:L"));
  }
}
