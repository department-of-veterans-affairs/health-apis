package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class DatamartObservationTransformer {
  @NonNull final DatamartObservation datamart;

  private static CodeableConcept category(DatamartObservation.Category category) {
    Coding coding = categoryCoding(category);
    if (coding == null) {
      return null;
    }
    return CodeableConcept.builder().coding(asList(coding)).build();
  }

  private static Coding categoryCoding(DatamartObservation.Category category) {
    if (category == null) {
      return null;
    }

    Coding.CodingBuilder coding =
        Coding.builder().system("http://hl7.org/fhir/observation-category");

    switch (category) {
      case exam:
        return coding.code("exam").display("Exam").build();
      case imaging:
        return coding.code("imaging").display("Imaging").build();
      case laboratory:
        return coding.code("laboratory").display("Laboratory").build();
      case procedure:
        return coding.code("procedure").display("Procedure").build();
      case social_history:
        return coding.code("social-history").display("Social History").build();
      case survey:
        return coding.code("survey").display("Survey").build();
      case therapy:
        return coding.code("therapy").display("Therapy").build();
      case vital_signs:
        return coding.code("vital-signs").display("Vital Signs").build();
      default:
        throw new IllegalArgumentException("Unknown category: " + category);
    }
  }

  private static Observation.Status status(DatamartObservation.Status status) {
    if (status == null) {
      return null;
    }
    return EnumSearcher.of(Observation.Status.class).find(status.toString());
  }

  Observation toFhir() {
    //    .code(code(datamart.Code()))
    //    .subject(reference(datamart.Subject()))
    //    .encounter(reference(datamart.Encounter()))
    //    .effectiveDateTime(asDateTimeString(datamart.EffectiveDateTime()))
    //    .issued(asDateTimeString(datamart.Issued()))
    //    .performer(performers(datamart.Performers()))
    //    .valueQuantity(valueQuantity(datamart.ValueQuantity()))
    //    .valueCodeableConcept(valueCodeableConcept(datamart.ValueCodeableConcept()))
    //    .interpretation(interpretation(datamart.Interpretation()))
    //    .comments(datamart.Comments())
    //    .referenceRange(referenceRanges(datamart.ReferenceRanges(), datamart.Category()))
    //    .component(components(datamart.Components()))

    /*
     * Specimen reference is omitted since we do not support the a specimen resource and
     * do not want dead links
     */
    return Observation.builder()
        .resourceType("Observation")
        .id(datamart.cdwId())
        .status(status(datamart.status()))
        .category(category(datamart.category()))
        .build();
  }
}
