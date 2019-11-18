package gov.va.api.health.dataquery.service.controller.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import java.time.Instant;
import java.util.Optional;
import javax.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatamartLocation implements HasReplaceableId {
  @Builder.Default private String objectType = "Location";

  @Builder.Default private String objectVersion = "1";

  private String cdwId;

//  private Optional<DatamartReference> performer;
//
//  /** Lazy initialization with empty. */
//  public Optional<DatamartReference> encounter() {
//    if (encounter == null) {
//      encounter = Optional.empty();
//    }
//    return encounter;
//  }
//
//  @Data
//  @Builder
//  @NoArgsConstructor(access = AccessLevel.PRIVATE)
//  @AllArgsConstructor(access = AccessLevel.PRIVATE)
//  public static class VaccineCode {
//
//    private String text;
//
//    private String code;
//  }
}
