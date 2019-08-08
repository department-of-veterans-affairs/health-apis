package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Locale;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.lowerCase;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;

import static gov.va.api.health.dataquery.service.controller.Transformers.asCoding;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.Quantity;
import gov.va.api.health.dstu2.api.elements.Reference;
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

  private static List<Reference> performers(List<DatamartReference> performers) {
    if (isEmpty(performers)) {
      return null;
    }

    List<Reference> results =
        performers.stream().map(p -> asReference(p)).collect(Collectors.toList());
    return emptyToNull(results);
  }

  private static Quantity valueQuantity(Optional<DatamartObservation.Quantity> maybeQuantity) {
    if (maybeQuantity == null || maybeQuantity.isEmpty()) {
      return null;
    }

    DatamartObservation.Quantity quantity = maybeQuantity.get();
    if (allBlank(quantity.value(), quantity.unit(), quantity.system(), quantity.code())) {
      return null;
    }

    return Quantity.builder()
        .value(quantity.value())
        .unit(quantity.unit())
        .system(quantity.system())
        .code(quantity.code())
        .build();
  }

  private static CodeableConcept codeableConcept(
      Optional<DatamartObservation.CodeableConcept> maybeCode) {
    if (maybeCode == null || maybeCode.isEmpty()) {
      return null;
    }

    DatamartObservation.CodeableConcept dmCode = maybeCode.get();
    Coding coding = asCoding(dmCode.coding());
    if (allBlank(coding, dmCode.text())) {
      return null;
    }

    return CodeableConcept.builder().coding(asList(coding)).text(dmCode.text()).build();
  }

  private static String interpretationDisplay(String interpretation) {
    switch (lowerCase(trimToEmpty(interpretation), Locale.US)) {
      case "<":
        return "Off scale low";
      case ">":
        return "Off scale high";
      case "A":
        return "Abnormal";
      case "AA":
        return "Critically abnormal";
      case "B":
        return "Better";
      case "D":
        return "Significant change down";
      case "DET":
        return "Detected";
      case "H":
        return "High";
      case "HH":
        return "Critically high";
      case "HU":
        return "Very high";
      case "I":
        return "Intermediate";
      case "IE":
        return "Insufficient evidence";
      case "IND":
        return "Indeterminate";
      case "L":
        return "Low";
      case "LL":
        return "Critically low";
      case "LU":
        return "Very low";
      case "MS":
        return "Moderately susceptible. Indicates for microbiology susceptibilities only.";
      case "N":
        return "Normal";
      case "ND":
        return "Not Detected";
      case "NEG":
        return "Negative";
      case "NR":
        return "Non-reactive";
      case "NS":
        return "Non-susceptible";
      case "POS":
        return "Positive";
      case "R":
        return "Resistant";
      case "RR":
        return "Reactive";
      case "S":
        return "Susceptible";
      case "SDD":
        return "Susceptible-dose dependent";
      case "SYN-R":
        return "Synergy - resistant";
      case "SYN-S":
        return "Synergy - susceptible";
      case "U":
        return "Significant change up";
      case "VS":
        return "Very susceptible. Indicates for microbiology susceptibilities only.";
      case "W":
        return "Worse";
      case "WR":
        return "Weakly reactive";
      default:
        return null;
    }
  }

  private static CodeableConcept interpretation(String interpretation) {
    if (isBlank(interpretation)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            asList(
                Coding.builder()
                    .system("http://hl7.org/fhir/v2/0078")
                    .code(interpretation)
                    .display(interpretationDisplay(interpretation))
                    .build()))
        .text(interpretation)
        .build();
  }

  Observation toFhir() {
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
        .code(codeableConcept(datamart.code()))
        .subject(asReference(datamart.subject()))
        .encounter(asReference(datamart.encounter()))
        .effectiveDateTime(asDateTimeString(datamart.effectiveDateTime()))
        .issued(asDateTimeString(datamart.issued()))
        .performer(performers(datamart.performer()))
        .valueQuantity(valueQuantity(datamart.valueQuantity()))
        .valueCodeableConcept(codeableConcept(datamart.valueCodeableConcept()))
        .interpretation(interpretation(datamart.interpretation()))
        .comments(datamart.comment())
        .build();
  }
}
