package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.resources.Encounter;
import gov.va.api.health.dataquery.api.resources.Encounter.EncounterClass;
import gov.va.api.health.dataquery.api.resources.Encounter.EncounterLocation;
import gov.va.api.health.dataquery.api.resources.Encounter.Hospitalization;
import gov.va.api.health.dataquery.api.resources.Encounter.Participant;
import gov.va.api.health.dataquery.api.resources.Encounter.Status;
import gov.va.api.health.dataquery.api.resources.Encounter.StatusHistory;
import java.util.Arrays;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

/**
 * This class provides data structures that are populated with dummy values, suitable for testing
 * serialization.
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public class SampleEncounters {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public Encounter encounter() {
    return Encounter.builder()
        .id("1234")
        .resourceType("Encounter")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello Language")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(Arrays.asList(extension(), extension()))
        .modifierExtension(
            Arrays.asList(extension(), extensionWithQuantity(), extensionWithRatio()))
        .identifier(singletonList(identifier()))
        .status(Status.arrived)
        .statusHistory(singletonList(statusHistory()))
        .encounterClass(EncounterClass.ambulatory)
        .type(codeableConceptList())
        .priority(codeableConceptList())
        .patient(reference())
        .episodeOfCare(referenceList())
        .incomingReferral(referenceList())
        .participant(singletonList(participant()))
        .appointment(reference())
        .period(period())
        .length(duration())
        .reason(codeableConceptList())
        .indication(referenceList())
        .hospitalization(hospitalization())
        .location(singletonList(location()))
        .serviceProvider(reference())
        .partOf(reference())
        .build();
  }

  public Hospitalization hospitalization() {
    return Hospitalization.builder()
        .id("2222")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .preAdmissionIdentifier(identifier())
        .origin(reference())
        .admitSource(codeableConcept())
        .admittingDiagnosis(referenceList())
        .reAdmission(codeableConcept())
        .dietPreference(codeableConceptList())
        .specialCourtesy(codeableConceptList())
        .specialArrangement(codeableConceptList())
        .destination(reference())
        .dischargeDisposition(codeableConcept())
        .dischargeDiagnosis(referenceList())
        .build();
  }

  public EncounterLocation location() {
    return EncounterLocation.builder()
        .id("3333")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .location(reference())
        .status(EncounterLocation.Status.active)
        .period(period())
        .build();
  }

  public Participant participant() {
    return Participant.builder()
        .id("1111")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .type(codeableConceptList())
        .period(period())
        .individual(reference())
        .build();
  }

  public StatusHistory statusHistory() {
    return StatusHistory.builder()
        .id("0000")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .status(Status.planned)
        .period(period())
        .build();
  }
}
