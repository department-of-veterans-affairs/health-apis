package gov.va.api.health.dataquery.service.controller.practitionerrole;

import java.io.Serializable;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpecialtyMapCompositeId implements Serializable {
  private BigInteger practitionerRoleIdNumber;
  private char practitionerRoleResourceCode;
  private String specialtyCode;
}
