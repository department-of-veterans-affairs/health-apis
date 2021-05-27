package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import java.util.List;
import lombok.experimental.UtilityClass;

/** Utility class for interacting with Facilities API ids. */
@UtilityClass
public class FacilityTransformers {
  public static final String FAPI_IDENTIFIER_SYSTEM =
      "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier";

  public static final String FAPI_CLINIC_IDENTIFIER_SYSTEM =
      "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier";

  /** Convert facility ID to Identifier. */
  public static Identifier facilityIdentifier(FacilityId id) {
    String fapiId = fapiFacilityId(id);
    if (isBlank(fapiId)) {
      return null;
    }
    return Identifier.builder()
        .use(Identifier.IdentifierUse.usual)
        .type(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                            .code("FI")
                            .display("Facility ID")
                            .build()))
                .build())
        .system(FAPI_IDENTIFIER_SYSTEM)
        .value(fapiId)
        .build();
  }

  /** Convert to Facilities API clinic ID. */
  public static String fapiClinicId(FacilityId id, String clinicId) {
    if (id == null || isBlank(clinicId) || isBlank(id.stationNumber())) {
      return null;
    }
    if (id.type() != FacilityId.FacilityType.HEALTH) {
      return null;
    }
    return "vha_" + id.stationNumber() + "_" + clinicId;
  }

  /** Convert to Facilities API ID. */
  public static String fapiFacilityId(FacilityId id) {
    if (id == null || isBlank(id.stationNumber()) || isBlank(id.type())) {
      return null;
    }
    return id.toString();
  }
}
