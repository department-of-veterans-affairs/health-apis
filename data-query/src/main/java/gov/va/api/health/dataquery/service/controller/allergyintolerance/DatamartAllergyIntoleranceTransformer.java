package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static java.util.Arrays.asList;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;

import java.util.Optional;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import lombok.Builder;
import lombok.NonNull;

@Builder
// PETERTODO not public lol
public final class DatamartAllergyIntoleranceTransformer {
  @NonNull final DatamartAllergyIntolerance datamart;

  // PETERTODO not public lol
  public AllergyIntolerance toFhir() {
    //  .criticality(criticality(datamart.getCriticality()))
    //  .type(type(datamart.getType()))
    //  .category(category(datamart.getCategory()))
    //  .note(note(datamart.getNotes()))
    //  .reaction(reaction(datamart.getReactions()))

    return AllergyIntolerance.builder()
        .id(datamart.cdwId())
        .resourceType("AllergyIntolerance")
        .recordedDate(asDateTimeString(datamart.recordedDate()))
        .recorder(asReference(datamart.recorder()))
        .substance(substance(datamart.substance()))
        .patient(asReference(datamart.patient()))
        .status(status(datamart.status()))
        .build();
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
