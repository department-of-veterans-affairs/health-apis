package gov.va.api.health.dataquery.service.controller.medication;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartMedication {

  @Builder.Default private String objectType = "Medication";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  private String etlDate;

  private RxNorm rxnorm;

  private Product product;

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class RxNorm {
    String code;

    String text;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Product {
    String id;

    String formText;
  }
}
