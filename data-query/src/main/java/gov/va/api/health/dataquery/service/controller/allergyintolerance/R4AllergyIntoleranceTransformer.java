package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static gov.va.api.health.dataquery.service.controller.R4Transformers.asCoding;
import static gov.va.api.health.dataquery.service.controller.R4Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.r4.api.datatypes.Annotation;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.uscorer4.api.resources.AllergyIntolerance;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
public final class R4AllergyIntoleranceTransformer {
  @NonNull private final DatamartAllergyIntolerance datamart;

  static List<AllergyIntolerance.Category> categories(
      DatamartAllergyIntolerance.Category category) {
    if (category == null) {
      return null;
    }
    return List.of(EnumSearcher.of(AllergyIntolerance.Category.class).find(category.toString()));
  }

  static CodeableConcept clinicalStatus(DatamartAllergyIntolerance.Status status) {
    String code = clinicalStatusCode(status);
    if (isBlank(code)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/allergyintolerance-clinical")
                    .code(code)
                    .build()))
        .build();
  }

  static String clinicalStatusCode(DatamartAllergyIntolerance.Status status) {
    // active | inactive | resolved
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
    switch (status) {
      case active:
        return "active";
      case inactive:
        return "inactive";
      case resolved:
        return "resolved";
      case confirmed:
        return "active";
      default:
        return null;
    }
  }

  static List<CodeableConcept> manifestations(
      Optional<DatamartAllergyIntolerance.Reaction> maybeReaction) {
    if (maybeReaction == null || !maybeReaction.isPresent()) {
      return null;
    }
    List<DatamartCoding> manifestations = maybeReaction.get().manifestations();
    if (isEmpty(manifestations)) {
      return null;
    }
    List<Coding> codings =
        manifestations.stream().map(m -> asCoding(m)).filter(Objects::nonNull).collect(toList());
    return emptyToNull(
        codings.stream()
            .map(coding -> CodeableConcept.builder().coding(asList(coding)).build())
            .collect(toList()));
  }

  static Annotation note(DatamartAllergyIntolerance.Note note) {
    if (note == null) {
      return null;
    }
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

  static List<Annotation> notes(List<DatamartAllergyIntolerance.Note> notes) {
    if (isEmpty(notes)) {
      return null;
    }
    return emptyToNull(notes.stream().map(n -> note(n)).collect(toList()));
  }

  static CodeableConcept substance(Optional<DatamartAllergyIntolerance.Substance> maybeSubstance) {
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

  static AllergyIntolerance.Type type(DatamartAllergyIntolerance.Type type) {
    if (type == null) {
      return null;
    }
    return EnumSearcher.of(AllergyIntolerance.Type.class).find(type.toString());
  }

  static CodeableConcept verificationStatus(DatamartAllergyIntolerance.Status status) {
    String code = verificationStatusCode(status);
    if (isBlank(code)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/allergyintolerance-verification")
                    .code(code)
                    .build()))
        .build();
  }

  static String verificationStatusCode(DatamartAllergyIntolerance.Status status) {
    if (status == null) {
      return null;
    }
    // unconfirmed | confirmed | refuted | entered-in-error
    switch (status) {
      case unconfirmed:
        return "unconfirmed";
      case confirmed:
        // confirmed is handled as clinical status
        return null;
      case refuted:
        return "refuted";
      case entered_in_error:
        return "entered-in-error";
      default:
        return null;
    }
  }

  List<AllergyIntolerance.Reaction> reactions(
      Optional<DatamartAllergyIntolerance.Reaction> maybeReaction) {
    CodeableConcept substance = substance(datamart.substance());
    List<CodeableConcept> manifestations = emptyToNull(manifestations(maybeReaction));
    if (allBlank(substance, manifestations)) {
      return null;
    }
    return asList(
        AllergyIntolerance.Reaction.builder()
            .substance(substance)
            .manifestation(manifestations)
            .build());
  }

  AllergyIntolerance toFhir() {
    return AllergyIntolerance.builder()
        .resourceType("AllergyIntolerance")
        .id(datamart.cdwId())
        .clinicalStatus(clinicalStatus(datamart.status()))
        .verificationStatus(verificationStatus(datamart.status()))
        .type(type(datamart.type()))
        .category(categories(datamart.category()))
        .patient(asReference(datamart.patient()))
        .recordedDate(asDateTimeString(datamart.recordedDate()))
        .recorder(asReference(datamart.recorder()))
        .note(notes(datamart.notes()))
        .reaction(reactions(datamart.reactions()))
        .build();
  }
}
