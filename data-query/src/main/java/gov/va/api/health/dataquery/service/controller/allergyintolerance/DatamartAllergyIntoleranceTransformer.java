package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static java.util.Arrays.asList;

import java.util.List;

import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import lombok.Builder;
import lombok.NonNull;

@Builder
// PETERTODO not public lol
public final class DatamartAllergyIntoleranceTransformer {
  @NonNull final DatamartAllergyIntolerance datamart;

  // PETERTODO not public lol
  public AllergyIntolerance toFhir() {
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

  private List<AllergyIntolerance.Reaction> reactions(
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

  private List<CodeableConcept> manifestations(
      List<DatamartAllergyIntolerance.Coding> manifestations) {
    if (isEmpty(manifestations)) {
      return null;
    }

    return asList(
        CodeableConcept.builder()
            .coding(
                manifestations
                    .stream()
                    .map(m -> coding(Optional.ofNullable(m)))
                    .collect(Collectors.toList()))
            .build());
  }

  private AllergyIntolerance.Certainty certainty(DatamartAllergyIntolerance.Certainty certainty) {
    if (certainty == null) {
      return null;
    }
    return EnumSearcher.of(AllergyIntolerance.Certainty.class).find(certainty.toString());
  }

  private Annotation notes(List<DatamartAllergyIntolerance.Note> notes) {
    if (isEmpty(notes)) {
      return null;
    }
    DatamartAllergyIntolerance.Note note = notes.get(0);

    Reference authorReference = authorReference(note);
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

  private Reference authorReference(DatamartAllergyIntolerance.Note note) {
    if (note == null) {
      return null;
    }
    if (allBlank(note.referencePractitionerId(), note.referencePractitionerName())) {
      return null;
    }
    return Reference.builder()
        .reference("Practitioner/" + note.referencePractitionerId())
        .display(note.referencePractitionerName())
        .build();
  }

  private AllergyIntolerance.Category category(DatamartAllergyIntolerance.Category category) {
    if (category == null) {
      return null;
    }
    return EnumSearcher.of(AllergyIntolerance.Category.class).find(category.toString());
  }

  private AllergyIntolerance.Type type(DatamartAllergyIntolerance.Type type) {
    if (type == null) {
      return null;
    }
    return EnumSearcher.of(AllergyIntolerance.Type.class).find(type.toString());
  }

  private AllergyIntolerance.Status status(DatamartAllergyIntolerance.Status status) {
    if (status == null) {
      return null;
    }
    return EnumSearcher.of(AllergyIntolerance.Status.class).find(status.toString());
  }

  private CodeableConcept substance(Optional<DatamartAllergyIntolerance.Substance> maybeSubstance) {
    if (!maybeSubstance.isPresent()) {
      return null;
    }
    DatamartAllergyIntolerance.Substance substance = maybeSubstance.get();
    Coding coding = coding(substance.coding());
    if (allBlank(coding, substance.text())) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(emptyToNull(asList(coding)))
        .text(substance.text())
        .build();
  }

  private Coding coding(Optional<DatamartAllergyIntolerance.Coding> maybeCoding) {
    if (!maybeCoding.isPresent()) {
      return null;
    }
    DatamartAllergyIntolerance.Coding coding = maybeCoding.get();
    if (allBlank(coding.system(), coding.code(), coding.display())) {
      return null;
    }
    return Coding.builder()
        .system(coding.system())
        .code(coding.code())
        .display(coding.display())
        .build();
  }
}
