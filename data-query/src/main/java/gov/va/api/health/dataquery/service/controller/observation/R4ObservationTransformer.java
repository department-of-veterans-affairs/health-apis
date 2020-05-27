package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asCoding;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.upperCase;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.uscorer4.api.resources.Observation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class R4ObservationTransformer {
  @NonNull private final DatamartObservation datamart;

  static List<CodeableConcept> category(DatamartObservation.Category category) {
    Coding coding = categoryCoding(category);
    if (coding == null) {
      return null;
    } else {
      return List.of(CodeableConcept.builder().coding(asList(coding)).build());
    }
  }

  static Coding categoryCoding(DatamartObservation.Category category) {
    if (category == null) {
      return null;
    }
    Coding.CodingBuilder coding =
        Coding.builder().system("http://hl7.org/fhir/R4/valueset-observation-category.html");
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

  static CodeableConcept codeableConcept(Optional<DatamartObservation.CodeableConcept> maybeCode) {
    if (maybeCode.isEmpty()) {
      return null;
    }
    DatamartObservation.CodeableConcept code = maybeCode.get();
    Coding coding = asCoding(code.coding());
    if (allBlank(coding, code.text())) {
      return null;
    } else {
      return CodeableConcept.builder().coding(asList(coding)).text(code.text()).build();
    }
  }

  static Observation.Component component(DatamartObservation.VitalsComponent component) {
    if (component == null) {
      return null;
    }
    Coding coding = asCoding(component.code());
    Quantity quantity = quantity(component.valueQuantity());
    if (allBlank(coding, quantity)) {
      return null;
    }
    return Observation.Component.builder()
        .code(CodeableConcept.builder().coding(asList(coding)).build())
        .valueQuantity(quantity)
        .build();
  }

  static Observation.Component component(DatamartObservation.AntibioticComponent component) {
    if (component == null) {
      return null;
    }
    CodeableConcept concept = codeableConcept(component.code());
    Coding valueCoding = asCoding(component.valueCodeableConcept());
    if (allBlank(concept, valueCoding)) {
      return null;
    }
    return Observation.Component.builder()
        .code(concept)
        .valueCodeableConcept(CodeableConcept.builder().coding(asList(valueCoding)).build())
        .build();
  }

  static Observation.Component component(DatamartObservation.BacteriologyComponent component) {
    if (component == null) {
      return null;
    }
    String codeText = component.code().isPresent() ? component.code().get().text() : null;
    String valueText =
        component.valueText().isPresent() ? component.valueText().get().text() : null;
    if (allBlank(codeText, valueText)) {
      return null;
    }
    return Observation.Component.builder()
        .code(CodeableConcept.builder().text(codeText).build())
        .valueString(valueText)
        .build();
  }

  static List<CodeableConcept> interpretation(String interpretation) {
    if (isBlank(interpretation)) {
      return null;
    }
    return List.of(
        CodeableConcept.builder()
            .coding(
                asList(
                    Coding.builder()
                        .system("http://hl7.org/fhir/R4/v3/ObservationInterpretation/vs.html")
                        .code(interpretation)
                        .display(interpretationDisplay(interpretation))
                        .build()))
            .text(interpretation)
            .build());
  }

  static String interpretationDisplay(String code) {
    switch (upperCase(trimToEmpty(code), Locale.US)) {
      case "_GENETICOBSERVATIONINTERPRETATION":
        return "GeneticObservationInterpretation";
      case "CAR":
        return "Carrier";
      case "_OBSERVATIONINTERPRETATIONCHANGE":
        return "ObservationInterpretationChange";
      case "_OBSERVATIONINTERPRETATIONEXCEPTIONS":
        return "ObservationInterpretationExceptions";
      case "_OBSERVATIONINTERPRETATIONSUSCEPTIBILITY":
        return "ObservationInterpretationSusceptibility";
      case "OBSERVATIONINTERPRETATIONDETECTION":
        return "ObservationInterpretationDetection";
      case "OBSERVATIONINTERPRETATIONEXPECTATION":
        return "ObservationInterpretationExpectation";
      case "REACTIVITYOBSERVATIONINTERPRETATION":
        return "ReactivityObservationInterpretation";
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
      case "E":
        return "Equivocal";
      case "EX":
        return "outside threshold";
      case "EXP":
        return "Expected";
      case "H":
        return "High";
      case "HH":
        return "Critically high";
      case "HU":
        return "Significantly high";
      case "HX":
        return "above high threshold";
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
        return "Significantly low";
      case "LX":
        return "below low threshold";
      case "MS":
        return "Moderately susceptible. Indicates for microbiology susceptibilities only.";
      case "N":
        return "Normal";
      case "NCL":
        return "No CLSI defined breakpoint";
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
      case "UNE":
        return "Unexpected";
      case "VS":
        return "Very susceptible. Indicates for microbiology susceptibilities only.";
      case "W":
        return "Worse";
      case "WR":
        return "Weakly reactive";
      default:
        log.error("No display value for interpretation code '{}'.", code);
        return null;
    }
  }

  static List<Reference> performers(List<DatamartReference> performers) {
    if (isEmpty(performers)) {
      return null;
    } else {
      List<Reference> results =
          performers.stream().map(p -> asReference(p)).collect(Collectors.toList());
      return emptyToNull(results);
    }
  }

  static Quantity quantity(Optional<DatamartObservation.Quantity> maybeQuantity) {
    if (maybeQuantity.isEmpty()) {
      return null;
    }
    DatamartObservation.Quantity quantity = maybeQuantity.get();
    if (allBlank(quantity.value(), quantity.unit(), quantity.system(), quantity.code())) {
      return null;
    }
    return Quantity.builder()
        .value(BigDecimal.valueOf(quantity.value()))
        .unit(quantity.unit())
        .system(quantity.system())
        .code(quantity.code())
        .build();
  }

  static List<Observation.ReferenceRange> referenceRanges(
      Optional<DatamartObservation.ReferenceRange> maybeRange) {
    if (maybeRange.isEmpty()) {
      return null;
    }
    DatamartObservation.ReferenceRange referenceRange = maybeRange.get();
    SimpleQuantity low = simpleQuantity(quantity(referenceRange.low()));
    SimpleQuantity high = simpleQuantity(quantity(referenceRange.high()));
    if (allBlank(low, high)) {
      return null;
    }
    return asList(Observation.ReferenceRange.builder().low(low).high(high).build());
  }

  private static SimpleQuantity simpleQuantity(Quantity quantity) {
    if (quantity == null) {
      return null;
    } else {
      return SimpleQuantity.builder()
          .value(quantity.value())
          .unit(quantity.unit())
          .system(quantity.system())
          .code(quantity.code())
          .build();
    }
  }

  static Observation.ObservationStatus status(DatamartObservation.Status status) {
    if (status == null) {
      return null;
    } else {
      return EnumSearcher.of(Observation.ObservationStatus.class).find(status.toString());
    }
  }

  private List<Observation.Component> components() {
    List<Observation.Component> results =
        new ArrayList<>(
            datamart.vitalsComponents().size() + datamart.antibioticComponents().size() + 2);
    List<Observation.Component> vitals =
        emptyToNull(
            datamart.vitalsComponents().stream()
                .map(v -> component(v))
                .collect(Collectors.toList()));
    if (vitals != null) {
      results.addAll(vitals);
    }
    List<Observation.Component> antibiotics =
        emptyToNull(
            datamart.antibioticComponents().stream()
                .map(v -> component(v))
                .collect(Collectors.toList()));
    if (antibiotics != null) {
      results.addAll(antibiotics);
    }
    results.add(component(datamart.mycobacteriologyComponents().orElse(null)));
    results.add(component(datamart.bacteriologyComponents().orElse(null)));
    return emptyToNull(results);
  }

  Observation toFhir() {
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
        .effectiveDateTime(asDateTimeString(datamart.effectiveDateTime()))
        .issued(asDateTimeString(datamart.issued()))
        .performer(performers(datamart.performer()))
        .valueQuantity(quantity(datamart.valueQuantity()))
        .valueCodeableConcept(codeableConcept(datamart.valueCodeableConcept()))
        .interpretation(interpretation(datamart.interpretation()))
        .referenceRange(referenceRanges(datamart.referenceRange()))
        .component(components())
        .build();
  }
}
