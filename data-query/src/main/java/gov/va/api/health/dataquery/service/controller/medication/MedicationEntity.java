package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.*;

import javax.persistence.*;

/**
 *
 *
 * <pre>
 *  CREATE TABLE [app].[Medication](
 *         [CDWId] [varchar](50) NOT NULL,
 *         [Medication] [varchar](max) NULL,
 *         [ETLBatchId] [int] NULL,
 *         [ETLCreateDate] [datetime2](0) NULL,
 *         [ETLEditDate] [datetime2](0) NULL,
 * PRIMARY KEY CLUSTERED
 * </pre>
 */
@Data
@Entity
@Builder
@Table(name = "app.Medication")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationEntity {

    @Id
    @Column(name = "CDWId")
    @EqualsAndHashCode.Include
    private String cdwId;

    @Column(name = "Medication")
    @Basic(fetch = FetchType.LAZY)
    @Lob
    private String payload;

    @SneakyThrows
    DatamartMedication asDatamartMedication() {
        return JacksonConfig.createMapper().readValue(payload, DatamartMedication.class);
    }
}
