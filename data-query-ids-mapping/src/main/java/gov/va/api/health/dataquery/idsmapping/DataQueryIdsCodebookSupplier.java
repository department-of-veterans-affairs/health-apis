package gov.va.api.health.dataquery.idsmapping;

import gov.va.api.health.ids.client.EncryptingIdEncoder.Codebook;
import gov.va.api.health.ids.client.EncryptingIdEncoder.Codebook.Mapping;
import gov.va.api.health.ids.client.EncryptingIdEncoder.CodebookSupplier;
import java.util.List;

/** Shared mapping to be used by Data Query. */
public class DataQueryIdsCodebookSupplier implements CodebookSupplier {

  @Override
  public Codebook get() {
    return Codebook.builder()
        .map(
            List.of(
                /* Systems */
                Mapping.of("CDW", "C"),
                Mapping.of("MVI", "M"),
                Mapping.of("UNKNOWN", "U"),
                /* Data Query Resources */
                Mapping.of("ALLERGY_INTOLERANCE", "AI"),
                Mapping.of("CONDITION", "CO"),
                Mapping.of("DIAGNOSTIC_REPORT", "DR"),
                Mapping.of("IMMUNIZATION", "IM"),
                Mapping.of("LOCATION", "LO"),
                Mapping.of("MEDICATION", "ME"),
                Mapping.of("MEDICATION_ORDER", "MO"),
                Mapping.of("MEDICATION_STATEMENT", "MS"),
                Mapping.of("OBSERVATION", "OB"),
                Mapping.of("ORGANIZATION", "OG"),
                Mapping.of("PATIENT", "PA"),
                Mapping.of("PRACTITIONER", "PC"),
                Mapping.of("PROCEDURE", "PR")))
        .build();
  }
}
