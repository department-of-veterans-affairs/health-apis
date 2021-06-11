package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.dataquery.service.controller.DatamartSupport;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.CompositeIdDatamartEntity;
import gov.va.api.lighthouse.datamart.Payload;
import java.math.BigInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

/** Condition DB model. */
@Data
@Entity
@Builder
@Table(name = "Condition", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@IdClass(CompositeCdwId.class)
public class ConditionEntity implements CompositeIdDatamartEntity {
  @Id
  @EqualsAndHashCode.Include
  @Column(name = "CdwIdNumber")
  private BigInteger cdwIdNumber;

  @Id
  @EqualsAndHashCode.Include
  @Column(name = "CdwIdResourceCode")
  private char cdwIdResourceCode;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "Category")
  private String category;

  @Column(name = "ClinicalStatus")
  private String clinicalStatus;

  @Column(name = "Condition")
  @Basic(fetch = FetchType.EAGER)
  @Lob
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwIdNumber").ascending().and(Sort.by("cdwIdResourceCode").ascending());
  }

  DatamartCondition asDatamartCondition() {
    return toPayload().deserialize();
  }

  @Override
  public CompositeCdwId compositeCdwId() {
    return new CompositeCdwId(cdwIdNumber(), cdwIdResourceCode());
  }

  @Override
  public Payload<DatamartCondition> toPayload() {
    return Payload.ofType(DatamartCondition.class)
        .json(payload())
        .cdwId(cdwId())
        .mapper(DatamartSupport.mapper())
        .build();
  }
}
