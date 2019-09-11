package gov.va.api.health.dataquery.tools.minimart.transformers;

import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static gov.va.api.health.dataquery.tools.minimart.RevealSecretIdentity.toDatamartReferenceWithCdwId;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.Quantity;
import gov.va.api.health.dstu2.api.datatypes.SimpleQuantity;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class F2DObservationTransformer {

  private DatamartObservation.AntibioticComponent antibioticObservation(
      Observation.ObservationComponent component) {
    if (component == null) {
      return null;
    }
    String codeText = null;
    if (component.code() != null) {
      codeText = component.code().text();
    }
    return DatamartObservation.AntibioticComponent.builder()
        .code(codeableConcept(component.code()))
        .codeText(codeText)
        .id(component.id())
        .valueCodeableConcept(coding(component.valueCodeableConcept()))
        .build();
  }

  private List<DatamartObservation.AntibioticComponent> antibioticObservations(
      List<Observation.ObservationComponent> component) {
    return component
        .stream()
        .filter(c -> c != null)
        .map(c -> antibioticObservation(c))
        .collect(Collectors.toList());
  }

  public Optional<DatamartObservation.BacteriologyComponent> bacteriologyComponents(
      Observation.ObservationComponent component) {
    if (component == null) {
      return null;
    }
    if (component.valueString() == null
        && (component.code() == null || component.code().text() == null)) {
      return null;
    }
    Optional<DatamartObservation.Text> valueText =
        component.valueString() != null ? Optional.of(dmText(component.valueString())) : null;
    Optional<DatamartObservation.Text> code = null;
    if (component.code() != null) {
      code = component.code().text() != null ? Optional.of(dmText(component.code().text())) : null;
    }
    return Optional.ofNullable(
        DatamartObservation.BacteriologyComponent.builder()
            .code(code)
            .valueText(valueText)
            .build());
  }

  private DatamartObservation.Category category(CodeableConcept category) {
    if (category == null
        || category.coding() == null
        || category.coding().isEmpty()
        || category.coding().get(0) == null) {
      return null;
    }
    return EnumSearcher.of(DatamartObservation.Category.class)
        .find(category.coding().get(0).code());
  }

  private Optional<DatamartObservation.CodeableConcept> code(CodeableConcept code) {
    if (code == null) {
      return null;
    }
    return Optional.of(
        DatamartObservation.CodeableConcept.builder()
            .coding(coding(code))
            .text(code.text())
            .build());
  }

  private Optional<DatamartObservation.CodeableConcept> codeableConcept(
      CodeableConcept codeableConcept) {
    return Optional.of(
        DatamartObservation.CodeableConcept.builder()
            .coding(coding(codeableConcept.coding()))
            .text(codeableConcept.text())
            .build());
  }

  private Optional<DatamartCoding> coding(List<Coding> coding) {
    if (coding == null || coding.isEmpty() || coding.get(0) == null) {
      return null;
    }
    return Optional.of(
        DatamartCoding.builder()
            .display(Optional.of(coding.get(0).display()))
            .code(Optional.of(coding.get(0).code()))
            .system(Optional.of(coding.get(0).system()))
            .build());
  }

  private Optional<DatamartCoding> coding(CodeableConcept valueCodeableConcept) {
    if (valueCodeableConcept == null
        || valueCodeableConcept.coding() == null
        || valueCodeableConcept.coding().isEmpty()
        || valueCodeableConcept.coding().get(0) == null) {
      return null;
    }
    Coding coding = valueCodeableConcept.coding().get(0);
    return Optional.of(
        DatamartCoding.builder()
            .system(Optional.of(coding.system()))
            .code(Optional.of(coding.code()))
            .display(Optional.of(coding.display()))
            .build());
  }

  private DatamartObservation.Text dmText(String text) {
    return DatamartObservation.Text.builder().text(text).build();
  }

  public DatamartObservation fhirToDatamart(Observation observation) {
    DatamartObservation.DatamartObservationBuilder obsBuilder = DatamartObservation.builder();
    obsBuilder
        .cdwId(observation.id())
        .antibioticComponents(antibioticObservations(observation.component()))
        .valueQuantity(valueQuantity(observation.valueQuantity()))
        .issued(instant(observation.issued()))
        .subject(toDatamartReferenceWithCdwId(observation.subject()))
        .status(status(observation.status()))
        .specimen(toDatamartReferenceWithCdwId(observation.specimen()))
        .performer(performer(observation.performer()))
        .referenceRange(referenceRange(observation.referenceRange()))
        .interpretation(
            observation.interpretation() == null ? null : observation.interpretation().text())
        .comment(observation.comments())
        .code(code(observation.code()))
        .encounter(toDatamartReferenceWithCdwId(observation.encounter()))
        .effectiveDateTime(instant(observation.effectiveDateTime()))
        .valueCodeableConcept(valueCodeableConcept(observation.valueCodeableConcept()))
        .category(category(observation.category()));
    int componentSize = observation.component() != null ? observation.component().size() : 0;
    if (componentSize > 0) {
      obsBuilder.bacteriologyComponents(bacteriologyComponents(observation.component().get(0)));
      if (componentSize > 1) {
        obsBuilder.mycobacteriologyComponents(
            bacteriologyComponents(observation.component().get(1)));
      }
    }
    return obsBuilder.build();
  }

  private Optional<Instant> instant(String issued) {
    if (isBlank(issued)) {
      return null;
    }
    return Optional.of(Instant.parse(issued));
  }

  private List<DatamartReference> performer(List<Reference> performer) {
    if (performer == null || performer.isEmpty()) {
      return null;
    }
    return performer
        .stream()
        .filter(x -> x != null)
        .map(p -> toDatamartReferenceWithCdwId(p))
        .filter(x -> x.isPresent())
        .map(o -> o.get())
        .collect(Collectors.toList());
  }

  private Optional<DatamartObservation.Quantity> quantity(SimpleQuantity simpleQuantity) {
    if (simpleQuantity == null) {
      return null;
    }
    return Optional.of(
        DatamartObservation.Quantity.builder()
            .value(simpleQuantity.value())
            .unit(simpleQuantity.unit())
            .code(simpleQuantity.code())
            .build());
  }

  private Optional<DatamartObservation.ReferenceRange> referenceRange(
      List<Observation.ObservationReferenceRange> referenceRange) {
    if (referenceRange == null || referenceRange.isEmpty()) {
      return null;
    }
    return Optional.of(
        DatamartObservation.ReferenceRange.builder()
            .high(quantity(referenceRange.get(0).high()))
            .low(quantity(referenceRange.get(0).low()))
            .build());
  }

  private DatamartObservation.Status status(Observation.Status status) {
    if (status == null) {
      return null;
    }
    return EnumSearcher.of(DatamartObservation.Status.class).find(status.toString());
  }

  private Optional<DatamartObservation.CodeableConcept> valueCodeableConcept(
      CodeableConcept valueCodeableConcept) {
    if (valueCodeableConcept == null) {
      return null;
    }
    return Optional.of(
        DatamartObservation.CodeableConcept.builder()
            .coding(coding(valueCodeableConcept.coding()))
            .text(valueCodeableConcept.text())
            .build());
  }

  private Optional<DatamartObservation.Quantity> valueQuantity(Quantity valueQuantity) {
    return Optional.of(
        DatamartObservation.Quantity.builder()
            .code(valueQuantity.code())
            .system(valueQuantity.system())
            .unit(valueQuantity.unit())
            .value(valueQuantity.value())
            .build());
  }
}
