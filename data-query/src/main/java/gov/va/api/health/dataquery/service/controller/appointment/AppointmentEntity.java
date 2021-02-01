package gov.va.api.health.dataquery.service.controller.appointment;

import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.CompositeIdDatamartEntity;
import java.math.BigInteger;
import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@Entity
@Builder
@Table(name = "Appointment", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@IdClass(CompositeCdwId.class)
public class AppointmentEntity implements CompositeIdDatamartEntity {

  @Transient
  private final CompositeCdwId compositeCdwId =
      new CompositeCdwId(cdwIdNumber(), cdwIdResourceCode());

  @Id
  @Column(name = "CdwIdNumber")
  @EqualsAndHashCode.Include
  private BigInteger cdwIdNumber;

  @Id
  @Column(name = "CdwIdResourceCode")
  @EqualsAndHashCode.Include
  private char cdwIdResourceCode;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "LocationSID")
  private String locationSid;

  @Column(name = "DateUTC")
  private Instant dateUtc;

  @Column(name = "LastUpdated")
  private Instant lastUpdated;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "Appointment")
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwIdNumber").ascending().and(Sort.by("cdwIdResourceCode").ascending());
  }

  DatamartAppointment asDatamartAppointment() {
    return deserializeDatamart(payload, DatamartAppointment.class);
  }
}
