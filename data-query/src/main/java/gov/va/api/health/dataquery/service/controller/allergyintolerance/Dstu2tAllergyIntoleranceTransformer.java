package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asCoding;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static java.util.Arrays.asList;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance.Status;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;

@Builder
public final class Dstu2tAllergyIntoleranceTransformer {
  @NonNull final DatamartAllergyIntolerance datamart;

  AllergyIntolerance.Category category(DatamartAllergyIntolerance.Category category) {
    if (category == null) {
      return null;
    }
    return EnumSearcher.of(AllergyIntolerance.Category.class).find(category.toString());
  }

  AllergyIntolerance.Certainty certainty(DatamartAllergyIntolerance.Certainty certainty) {
    if (certainty == null) {
      return null;
    }
    return EnumSearcher.of(AllergyIntolerance.Certainty.class).find(certainty.toString());
  }

  List<CodeableConcept> manifestations(List<DatamartCoding> manifestations) {
    if (isEmpty(manifestations)) {
      return null;
    }
    List<Coding> codings =
        manifestations
            .stream()
            .map(m -> asCoding(m))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return emptyToNull(
        codings
            .stream()
            .map(coding -> CodeableConcept.builder().coding(asList(coding)).build())
            .collect(Collectors.toList()));
  }

  Annotation notes(List<DatamartAllergyIntolerance.Note> notes) {
    if (isEmpty(notes)) {
      return null;
    }
    DatamartAllergyIntolerance.Note note = notes.get(0);
    Reference authorReference = asReference(note.practitioner());
    String time = asDateTimeString(note.time());
    if (allBlank(authorReference, time, note.text())) {
      return null;
    }
    return Annotation.builder()
        .authorReference(authorReference)
        .time(time)
        .text(note.text())
        .build();
  }

  List<AllergyIntolerance.Reaction> reactions(
      Optional<DatamartAllergyIntolerance.Reaction> maybeReaction) {
    if (maybeReaction == null || !maybeReaction.isPresent()) {
      return null;
    }
    DatamartAllergyIntolerance.Reaction reaction = maybeReaction.get();
    AllergyIntolerance.Certainty certainty = certainty(reaction.certainty());
    List<CodeableConcept> manifestations = emptyToNull(manifestations(reaction.manifestations()));
    if (allBlank(certainty, manifestations)) {
      return null;
    }
    return asList(
        AllergyIntolerance.Reaction.builder()
            .certainty(certainty)
            .manifestation(manifestations)
            .build());
  }

  AllergyIntolerance.Status status(DatamartAllergyIntolerance.Status status) {
    if (status == null) {
      return null;
    }
    /*
     * To correct a KBS compliance issue with Datamart data, we will report all 'confirmed' status
     * values as `active`. From KBS ... "All allergy records should be marked active, excepting
     * those with entered-in-error-flag set to True, which should be marked entered-in-error."
     *
     * Our analysis of the ETL process indicates 'confirmed' values may be returned.
     */
    if (status == Status.confirmed) {
      return AllergyIntolerance.Status.active;
    }
    return EnumSearcher.of(AllergyIntolerance.Status.class).find(status.toString());
  }

  CodeableConcept substance(Optional<DatamartAllergyIntolerance.Substance> maybeSubstance) {
    if (!maybeSubstance.isPresent()) {
      return null;
    }
    DatamartAllergyIntolerance.Substance substance = maybeSubstance.get();
    Coding coding = asCoding(substance.coding());
    if (allBlank(coding, substance.text())) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(emptyToNull(asList(coding)))
        .text(substance.text())
        .build();
  }

  AllergyIntolerance toFhir() {
    return AllergyIntolerance.builder()
        .id(datamart.cdwId())
        .resourceType("AllergyIntolerance")
        .recordedDate(asDateTimeString(datamart.recordedDate()))
        .recorder(asReference(datamart.recorder()))
        .substance(substance(datamart.substance()))
        .patient(asReference(datamart.patient()))
        .status(status(datamart.status()))
        .type(type(datamart.type()))
        .category(category(datamart.category()))
        .note(notes(datamart.notes()))
        .reaction(reactions(datamart.reactions()))
        .build();
  }

  AllergyIntolerance.Type type(DatamartAllergyIntolerance.Type type) {
    if (type == null) {
      return null;
    }
    return EnumSearcher.of(AllergyIntolerance.Type.class).find(type.toString());
  }
}
