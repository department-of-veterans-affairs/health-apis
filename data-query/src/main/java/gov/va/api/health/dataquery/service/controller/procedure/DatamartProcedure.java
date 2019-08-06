package gov.va.api.health.dataquery.service.controller.procedure;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartProcedure {

  @Builder.Default private String objectType = "Procedure";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  private String etlData;
}
