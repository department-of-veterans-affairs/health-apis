package gov.va.api.health.dataquery.tools.minimart.transformers;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class F2DAllergyIntoleranceTransformer {

  private DatamartAllergyIntolerance.Category category(AllergyIntolerance.Category category) {
    if (category == null) {
      return null;
    }
    return EnumSearcher.of(DatamartAllergyIntolerance.Category.class).find(category.toString());
  }

  private DatamartAllergyIntolerance.Certainty certainty(AllergyIntolerance.Certainty certainty) {
    if (certainty == null) {
      return null;
    }
    return EnumSearcher.of(DatamartAllergyIntolerance.Certainty.class).find(certainty.toString());
  }

  private Optional<DatamartCoding> coding(Coding coding) {
    if (coding == null) {
      return null;
    }
    return Optional.of(
        DatamartCoding.builder()
            .code(Optional.of(coding.code()))
            .display(Optional.of(coding.display()))
            .system(Optional.of(coding.system()))
            .build());
  }

  private List<DatamartCoding> codings(List<Coding> codings) {
    if (codings == null || codings.isEmpty()) {
      return null;
    }
    return codings
        .stream()
        .map(c -> coding(c))
        .filter(Optional::isPresent)
        .map(c -> c.get())
        .collect(Collectors.toList());
  }

  private Optional<Instant> dateTime(String date) {
    if (date == null) {
      return null;
    }
    return Optional.of(Instant.parse(date));
  }

  /** Transforms a Fhir compliant AllergyIntolerance model to a datamart model of data. */
  public DatamartAllergyIntolerance fhirToDatamart(AllergyIntolerance allergyIntolerance) {
    return DatamartAllergyIntolerance.builder()
        .objectType(allergyIntolerance.resourceType())
        .cdwId(allergyIntolerance.id())
        .patient(
            DatamartReference.builder()
                .display(Optional.of(allergyIntolerance.patient().display()))
                .reference(Optional.of(allergyIntolerance.patient().reference()))
                .type(Optional.of("Patient"))
                .build())
        .recordedDate(dateTime(allergyIntolerance.recordedDate()))
        .recorder(reference(allergyIntolerance.recorder(), "Practitioner"))
        .substance(substance(allergyIntolerance.substance()))
        .status(status(allergyIntolerance.status()))
        .type(type(allergyIntolerance.type()))
        .category(category(allergyIntolerance.category()))
        .notes(notes(allergyIntolerance.note()))
        .reactions(reactions(allergyIntolerance.reaction()))
        .build();
  }

  private List<DatamartCoding> manifestations(List<CodeableConcept> manifestations) {
    return manifestations
        .stream()
        .map(CodeableConcept::coding)
        .flatMap(codings -> codings(codings).stream())
        .collect(Collectors.toList());
  }

  private List<DatamartAllergyIntolerance.Note> notes(Annotation note) {
    return List.of(
        DatamartAllergyIntolerance.Note.builder()
            .practitioner(reference(note.authorReference(), "Practitioner"))
            .text(note.text())
            .time(dateTime(note.time()))
            .build());
  }

  private Optional<DatamartAllergyIntolerance.Reaction> reactions(
      List<AllergyIntolerance.Reaction> reactions) {
    if (reactions == null || reactions.isEmpty()) {
      return null;
    }
    AllergyIntolerance.Reaction reaction = reactions.get(0);
    List<DatamartCoding> manifestations = manifestations(reaction.manifestation());
    return Optional.of(
        DatamartAllergyIntolerance.Reaction.builder()
            .certainty(certainty(reaction.certainty()))
            .manifestations(manifestations)
            .build());
  }

  private Optional<DatamartReference> reference(Reference reference, String type) {
    if (reference == null || allBlank(reference.display(), reference.reference())) {
      return null;
    }
    return Optional.of(
        DatamartReference.builder()
            .display(Optional.of(reference.display()))
            .reference(Optional.of(reference.reference()))
            .type(Optional.of(type))
            .build());
  }

  private DatamartAllergyIntolerance.Status status(AllergyIntolerance.Status status) {
    if (status == null) {
      return null;
    }
    return EnumSearcher.of(DatamartAllergyIntolerance.Status.class).find(status.toString());
  }

  private Optional<DatamartAllergyIntolerance.Substance> substance(CodeableConcept substance) {
    if (substance == null || substance.coding() == null) {
      return null;
    }
    return Optional.of(
        DatamartAllergyIntolerance.Substance.builder()
            .coding(coding((substance.coding().isEmpty()) ? null : substance.coding().get(0)))
            .text(substance.text())
            .build());
  }

  private DatamartAllergyIntolerance.Type type(AllergyIntolerance.Type type) {
    if (type == null) {
      return null;
    }
    return EnumSearcher.of(DatamartAllergyIntolerance.Type.class).find(type.toString());
  }
}
