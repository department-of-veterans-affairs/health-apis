package gov.va.api.health.dataquery.service.controller.etlstatus;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table(name = "Latest_Resource_ETL_Status", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LatestResourceEtlStatusEntity {
  @Id
  @Column(name = "ResourceName")
  @EqualsAndHashCode.Include
  private String resourceName;

  @Column(name = "EndDateTimeUTC")
  private Instant endDateTime;
}
