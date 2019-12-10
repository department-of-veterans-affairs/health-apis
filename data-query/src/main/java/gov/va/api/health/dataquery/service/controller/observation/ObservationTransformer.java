package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.ifPresent;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.argonaut.api.resources.Observation.ObservationComponent;
import gov.va.api.health.argonaut.api.resources.Observation.ObservationReferenceRange;
import gov.va.api.health.argonaut.api.resources.Observation.Status;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.Quantity;
import gov.va.api.health.dstu2.api.datatypes.SimpleQuantity;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwCategory;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwCode;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwComponents;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwComponents.CdwComponent;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwInterpretation;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwPerformers;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwReferenceRanges;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwReferenceRanges.CdwReferenceRange;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwValueCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root.CdwObservations.CdwObservation.CdwValueQuantity;
import gov.va.dvp.cdw.xsd.model.CdwObservationCategoryCode;
import gov.va.dvp.cdw.xsd.model.CdwObservationCategoryDisplay;
import gov.va.dvp.cdw.xsd.model.CdwObservationCategorySystem;
import gov.va.dvp.cdw.xsd.model.CdwObservationInterpretationSystem;
import gov.va.dvp.cdw.xsd.model.CdwObservationRefRangeQuantity;
import gov.va.dvp.cdw.xsd.model.CdwObservationStatus;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ObservationTransformer implements ObservationController.Transformer {
  @Override
  public Observation apply(CdwObservation cdw) {
    /*
     * Specimen reference is omitted since we do not support the a specimen resource and
     * do not want dead links
     */
    return Observation.builder()
        .resourceType("Observation")
        .id(cdw.getCdwId())
        .status(status(cdw.getStatus()))
        .category(category(cdw.getCategory()))
        .code(code(cdw.getCode()))
        .subject(reference(cdw.getSubject()))
        .encounter(reference(cdw.getEncounter()))
        .effectiveDateTime(asDateTimeString(cdw.getEffectiveDateTime()))
        .issued(asDateTimeString(cdw.getIssued()))
        .performer(performers(cdw.getPerformers()))
        .valueQuantity(valueQuantity(cdw.getValueQuantity()))
        .valueCodeableConcept(valueCodeableConcept(cdw.getValueCodeableConcept()))
        .interpretation(interpretation(cdw.getInterpretation()))
        .comments(cdw.getComments())
        .referenceRange(referenceRanges(cdw.getReferenceRanges(), cdw.getCategory()))
        .component(components(cdw.getComponents()))
        .build();
  }

  CodeableConcept category(CdwCategory maybeCdw) {
    if (maybeCdw == null || maybeCdw.getCoding().isEmpty()) {
      return null;
    }
    return CodeableConcept.builder().coding(categoryCodings(maybeCdw.getCoding())).build();
  }

  private Coding categoryCoding(CdwCategory.CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(ifPresent(cdw.getSystem(), CdwObservationCategorySystem::value))
        .code(ifPresent(cdw.getCode(), CdwObservationCategoryCode::value))
        .display(ifPresent(cdw.getDisplay(), CdwObservationCategoryDisplay::value))
        .build();
  }

  List<Coding> categoryCodings(List<CdwCategory.CdwCoding> source) {
    return convertAll(source, this::categoryCoding);
  }

  CodeableConcept code(CdwCode maybeCdw) {
    if (maybeCdw == null) {
      return null;
    }
    if (maybeCdw.getCoding().isEmpty() && isBlank(maybeCdw.getText())) {
      return null;
    }
    return CodeableConcept.builder()
        .text(maybeCdw.getText())
        .coding(codeCodings(maybeCdw.getCoding()))
        .build();
  }

  private Coding codeCoding(CdwCode.CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(cdw.getCode())
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> codeCodings(List<CdwCode.CdwCoding> source) {
    return convertAll(source, this::codeCoding);
  }

  ObservationComponent component(CdwComponent maybeCdw) {
    if (maybeCdw == null
        || allBlank(
            maybeCdw.getCode(),
            maybeCdw.getId(),
            maybeCdw.getValueCodeableConcept(),
            maybeCdw.getValueQuantity())) {
      return null;
    }
    return convert(
        maybeCdw,
        source ->
            ObservationComponent.builder()
                .id(source.getId())
                .code(componentCode(source.getCode()))
                .valueCodeableConcept(
                    componentValueCodeableConcept(source.getValueCodeableConcept()))
                .valueQuantity(componentValueQuantity(source.getValueQuantity()))
                .build());
  }

  CodeableConcept componentCode(CdwComponent.CdwCode maybeCdw) {
    if (maybeCdw == null) {
      return null;
    }
    if (maybeCdw.getCoding().isEmpty() && isBlank(maybeCdw.getText())) {
      return null;
    }
    return CodeableConcept.builder()
        .text(maybeCdw.getText())
        .coding(componentCodings(maybeCdw.getCoding()))
        .build();
  }

  private Coding componentCoding(CdwComponent.CdwCode.CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(cdw.getCode())
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> componentCodings(List<CdwComponent.CdwCode.CdwCoding> source) {
    return convertAll(source, this::componentCoding);
  }

  CodeableConcept componentValueCodeableConcept(CdwComponent.CdwValueCodeableConcept maybeCdw) {
    if (maybeCdw == null) {
      return null;
    }
    if (maybeCdw.getCoding().isEmpty() && isBlank(maybeCdw.getText())) {
      return null;
    }
    return CodeableConcept.builder()
        .text(maybeCdw.getText())
        .coding(componentValueCodings(maybeCdw.getCoding()))
        .build();
  }

  private Coding componentValueCoding(CdwComponent.CdwValueCodeableConcept.CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(cdw.getCode())
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> componentValueCodings(List<CdwComponent.CdwValueCodeableConcept.CdwCoding> source) {
    return convertAll(source, this::componentValueCoding);
  }

  Quantity componentValueQuantity(CdwComponent.CdwValueQuantity maybeCdw) {
    if (maybeCdw == null
        || allBlank(
            maybeCdw.getCode(),
            maybeCdw.getComparator(),
            maybeCdw.getSystem(),
            maybeCdw.getUnit(),
            maybeCdw.getValue())) {
      return null;
    }
    return Quantity.builder()
        .code(maybeCdw.getCode())
        .comparator(maybeCdw.getComparator())
        .system(maybeCdw.getSystem())
        .unit(maybeCdw.getUnit())
        .value(ifPresent(maybeCdw.getValue(), BigDecimal::doubleValue))
        .build();
  }

  List<ObservationComponent> components(CdwComponents maybeCdw) {
    return convertAll(ifPresent(maybeCdw, CdwComponents::getComponent), this::component);
  }

  private Optional<CdwObservationCategoryCode> firstCode(CdwCategory maybeCdw) {
    if (maybeCdw == null) {
      return Optional.empty();
    }
    List<CdwCategory.CdwCoding> categoryCoding = maybeCdw.getCoding();
    if (categoryCoding.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(categoryCoding.get(0).getCode());
  }

  CodeableConcept interpretation(CdwInterpretation maybeCdw) {
    if (maybeCdw == null) {
      return null;
    }
    if (maybeCdw.getCoding().isEmpty() && isBlank(maybeCdw.getText())) {
      return null;
    }
    return ifPresent(
        maybeCdw,
        source ->
            CodeableConcept.builder()
                .text(source.getText())
                .coding(interpretationCodings(source.getCoding()))
                .build());
  }

  private Coding interpretationCoding(CdwInterpretation.CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(ifPresent(cdw.getSystem(), CdwObservationInterpretationSystem::value))
        .code(cdw.getCode())
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> interpretationCodings(List<CdwInterpretation.CdwCoding> source) {
    return convertAll(source, this::interpretationCoding);
  }

  List<Reference> performers(CdwPerformers maybeCdw) {
    return convertAll(ifPresent(maybeCdw, CdwPerformers::getPerformer), this::reference);
  }

  Reference reference(CdwReference maybeCdw) {
    if (maybeCdw == null || allBlank(maybeCdw.getReference(), maybeCdw.getDisplay())) {
      return null;
    }
    return convert(
        maybeCdw,
        source ->
            Reference.builder()
                .reference(source.getReference())
                .display(source.getDisplay())
                .build());
  }

  ObservationReferenceRange referenceRange(CdwReferenceRange maybeCdw) {
    if (maybeCdw == null || allBlank(maybeCdw.getLow(), maybeCdw.getHigh())) {
      return null;
    }
    return ObservationReferenceRange.builder()
        .low(referenceRangeQuantity(maybeCdw.getLow()))
        .high(referenceRangeQuantity(maybeCdw.getHigh()))
        .build();
  }

  SimpleQuantity referenceRangeQuantity(CdwObservationRefRangeQuantity maybeCdw) {
    if (maybeCdw == null
        || allBlank(
            maybeCdw.getSystem(), maybeCdw.getCode(), maybeCdw.getUnit(), maybeCdw.getValue())) {
      return null;
    }
    return SimpleQuantity.builder()
        .system(StringUtils.isBlank(maybeCdw.getCode()) ? null : maybeCdw.getSystem())
        .value(ifPresent(maybeCdw.getValue(), BigDecimal::doubleValue))
        .unit(maybeCdw.getUnit())
        .code(maybeCdw.getCode())
        .build();
  }

  List<ObservationReferenceRange> referenceRanges(
      CdwReferenceRanges maybeCdw, CdwCategory cdwCategory) {
    Optional<CdwObservationCategoryCode> code = firstCode(cdwCategory);
    if (CdwObservationCategoryCode.VITAL_SIGNS.equals(code.orElse(null))) {
      return null;
    }
    return convertAll(
        ifPresent(maybeCdw, CdwReferenceRanges::getReferenceRange), this::referenceRange);
  }

  Status status(@NotNull CdwObservationStatus cdw) {
    return EnumSearcher.of(Status.class).find(cdw.value());
  }

  CodeableConcept valueCodeableConcept(CdwValueCodeableConcept maybeCdw) {
    if (maybeCdw == null) {
      return null;
    }
    if (maybeCdw.getCoding().isEmpty() && isBlank(maybeCdw.getText())) {
      return null;
    }
    return ifPresent(
        maybeCdw,
        source ->
            CodeableConcept.builder()
                .text(source.getText())
                .coding(valueCodings(source.getCoding()))
                .build());
  }

  private Coding valueCoding(CdwValueCodeableConcept.CdwCoding cdw) {
    if (cdw == null || allBlank(cdw.getCode(), cdw.getDisplay(), cdw.getSystem())) {
      return null;
    }
    return Coding.builder()
        .system(cdw.getSystem())
        .code(cdw.getCode())
        .display(cdw.getDisplay())
        .build();
  }

  List<Coding> valueCodings(List<CdwValueCodeableConcept.CdwCoding> source) {
    return convertAll(source, this::valueCoding);
  }

  Quantity valueQuantity(CdwValueQuantity maybeCdw) {
    if (maybeCdw == null
        || allBlank(
            maybeCdw.getCode(),
            maybeCdw.getComparator(),
            maybeCdw.getSystem(),
            maybeCdw.getUnit(),
            maybeCdw.getValue())) {
      return null;
    }
    return ifPresent(
        maybeCdw,
        cdw ->
            Quantity.builder()
                .system(StringUtils.isBlank(cdw.getCode()) ? null : cdw.getSystem())
                .value(cdw.getValue().doubleValue())
                .comparator(cdw.getComparator())
                .code(cdw.getCode())
                .unit(cdw.getUnit())
                .build());
  }
}
