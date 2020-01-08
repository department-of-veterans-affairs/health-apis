package gov.va.api.health.dataquery.idsmapping;

import gov.va.api.health.ids.client.EncryptingIdEncoder.Codebook;
import gov.va.api.health.ids.client.EncryptingIdEncoder.Codebook.Mapping;
import gov.va.api.health.ids.client.EncryptingIdEncoder.CodebookSupplier;

/** Shared mapping to be used by both Data Query and Mr. Anderson */
public class DataQueryIdsCodebookSupplier implements CodebookSupplier {

  @Override
  public Codebook get() {
    return Codebook.builder()
        /* Systems */
        .map(Mapping.of("CDW", "C"))
        .map(Mapping.of("MVI", "M"))
        .map(Mapping.of("UNKNOWN", "U"))
        /* Data Query Resources */
        .map(Mapping.of("ALLERGY_INTOLERANCE", "AI"))
        .map(Mapping.of("APPOINTMENT", "AP"))
        .map(Mapping.of("CONDITION", "CO"))
        .map(Mapping.of("DIAGNOSTIC_REPORT", "DR"))
        .map(Mapping.of("ENCOUNTER", "EN"))
        .map(Mapping.of("IMMUNIZATION", "IM"))
        .map(Mapping.of("LOCATION", "LO"))
        .map(Mapping.of("MEDICATION", "ME"))
        .map(Mapping.of("MEDICATION_DISPENSE", "MD"))
        .map(Mapping.of("MEDICATION_ORDER", "MO"))
        .map(Mapping.of("MEDICATION_STATEMENT", "MS"))
        .map(Mapping.of("OBSERVATION", "OB"))
        .map(Mapping.of("ORGANIZATION", "OG"))
        .map(Mapping.of("PATIENT", "PA"))
        .map(Mapping.of("PRACTITIONER", "PC"))
        .map(Mapping.of("PRACTITIONER_ROLE", "RO"))
        .map(Mapping.of("PROCEDURE", "PR"))
        .build();
  }
}
