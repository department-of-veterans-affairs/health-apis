package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

public class PractitionerRoleEntityTest {

  @Test
  void practitionerRoleEntity() {
    var cdwIdNumber = BigInteger.valueOf(111);
    var cdwIdResourceCode = 'S';
    var idNumber = BigInteger.valueOf(1234);
    var given = "John";
    var family = "Smith";
    PractitionerRoleEntity entity =
        PractitionerRoleSamples.Datamart.create()
            .roleEntity(cdwIdNumber, cdwIdResourceCode, idNumber, given, family);
    assertThat(entity).isNotNull();
    assertThat(entity.cdwIdNumber()).isEqualTo(cdwIdNumber);
    assertThat(entity.cdwIdResourceCode()).isEqualTo(cdwIdResourceCode);
    assertThat(entity.idNumber()).isEqualTo(idNumber);
    assertThat(entity.givenName()).isEqualTo(given);
    assertThat(entity.familyName()).isEqualTo(family);
    assertThat(entity.active()).isTrue();
    assertThat(entity.lastUpdated()).isNotNull();
    assertThat(entity.payload()).isNotNull();
    assertThat(entity.toPayload()).isNotNull();
  }
}
