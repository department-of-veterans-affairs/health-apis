package gov.va.api.health.dataquery.service.controller.medication;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartMedication {

  @Builder.Default private String objectType = "Medication";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

  String localDrugName;

  private Optional<RxNorm> rxnorm;

  private Optional<Product> product;

  /** Lazy getter. */
  public String localDrugName() {
    if (localDrugName == null) {
      return "**Unknown**";
    }
    return localDrugName;
  }

  /** Lazy initialization. */
  public Optional<RxNorm> rxnorm() {
    if (rxnorm == null) {
      return Optional.empty();
    }
    return rxnorm;
  }

  /** Lazy initialization. */
  public Optional<Product> product() {
    if (product == null) {
      return Optional.empty();
    }
    return product;
  }

  /** Backwards compatibility for etlDate. */
  private void setEtlDate(String unused) {
    /* no op */
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class RxNorm {
    private String code;

    private String text;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Product {
    private String id;

    private String formText;
  }
}
