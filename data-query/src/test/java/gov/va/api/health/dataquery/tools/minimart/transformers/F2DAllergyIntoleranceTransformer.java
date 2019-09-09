package gov.va.api.health.dataquery.tools.minimart.transformers;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.tools.minimart.RevealSecretIdentity;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class F2DAllergyIntoleranceTransformer {

  RevealSecretIdentity fauxIds;

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
        .cdwId(fauxIds.unmask("AllergyIntolerance", allergyIntolerance.id()))
        .patient(fauxIds.toDatamartReferenceWithCdwId(allergyIntolerance.patient()).get())
        .recordedDate(dateTime(allergyIntolerance.recordedDate()))
        .recorder(fauxIds.toDatamartReferenceWithCdwId(allergyIntolerance.recorder()))
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
            .practitioner(fauxIds.toDatamartReferenceWithCdwId(note.authorReference()))
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

  private DatamartAllergyIntolerance.Status status(AllergyIntolerance.Status status) {
    if (status == null) {
      return null;
    }
    return EnumSearcher.of(DatamartAllergyIntolerance.Status.class).find(status.toString());
  }

  private Optional<DatamartAllergyIntolerance.Substance> substance(CodeableConcept substance) {
    if (substance == null) {
      return null;
    }
    return Optional.of(
        DatamartAllergyIntolerance.Substance.builder()
            .coding(
                coding(
                    substance.coding() == null || substance.coding().isEmpty()
                        ? null
                        : substance.coding().get(0)))
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
