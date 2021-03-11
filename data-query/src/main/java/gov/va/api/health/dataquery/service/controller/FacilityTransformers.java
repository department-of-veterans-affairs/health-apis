package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FacilityTransformers {
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
        .system("https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier")
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
    switch (id.type()) {
      case HEALTH:
        return "vha_" + id.stationNumber();
      case BENEFITS:
        return "vba_" + id.stationNumber();
      case VET_CENTER:
        return "vc_" + id.stationNumber();
      case CEMETERY:
        return "nca_" + id.stationNumber();
      case NONNATIONAL_CEMETERY:
        return "nca_s" + id.stationNumber();
      default:
        throw new IllegalStateException("Unsupported facility type: " + id.type());
    }
  }
}
