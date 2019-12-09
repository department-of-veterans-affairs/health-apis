package gov.va.api.health.dataquery.service.controller.medicationorder;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.argonaut.api.resources.MedicationOrder.Status;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Duration;
import gov.va.api.health.dstu2.api.datatypes.SimpleQuantity;
import gov.va.api.health.dstu2.api.datatypes.Timing;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MedicationOrderSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {

    DatamartMedicationOrder.DispenseRequest dispenseRequest() {
      return DatamartMedicationOrder.DispenseRequest.builder()
          .numberOfRepeatsAllowed(Optional.of(1))
          .quantity(Optional.of(42.0))
          .unit(Optional.of("TAB"))
          .expectedSupplyDuration(Optional.of(21))
          .supplyDurationUnits(Optional.of("days"))
          .build();
    }

    List<DatamartMedicationOrder.DosageInstruction> dosageInstructions() {
      return asList(
          DatamartMedicationOrder.DosageInstruction.builder()
              .dosageText(
                  Optional.of(
                      "TAKE ONE TABLET BY MOUTH TWICE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS"))
              .timingText(Optional.of("BID"))
              .additionalInstructions(Optional.of("DO NOT TAKE NSAIDS WITH THIS MEDICATION"))
              .asNeeded(false)
              .routeText(Optional.of("ORAL"))
              .doseQuantityValue(Optional.of(1.0))
              .doseQuantityUnit(Optional.of("TAB"))
              .build(),
          DatamartMedicationOrder.DosageInstruction.builder()
              .dosageText(
                  Optional.of(
                      "THEN TAKE ONE TABLET BY MOUTH ONCE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS"))
              .timingText(Optional.of("QDAILY"))
              .additionalInstructions(Optional.of("DO NOT TAKE NSAIDS WITH THIS MEDICATION"))
              .asNeeded(false)
              .routeText(Optional.of("ORAL"))
              .doseQuantityValue(Optional.of(1.0))
              .doseQuantityUnit(Optional.of("TAB"))
              .build());
    }

    DatamartMedicationOrder medicationOrder() {
      return medicationOrder("1400181354458:O", "666V666");
    }

    DatamartMedicationOrder medicationOrder(String cdwId, String icn) {
      return DatamartMedicationOrder.builder()
          .cdwId(cdwId)
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference(icn)
                  .display("VETERAN,FARM ACY")
                  .build())
          .dateWritten(Instant.parse("2016-11-17T18:02:04Z"))
          .status("DISCONTINUED")
          .dateEnded(Optional.of(Instant.parse("2017-02-15T05:00:00Z")))
          .prescriber(
              DatamartReference.of()
                  .type("Practitioner")
                  .reference("1404497883")
                  .display("HIPPOCRATES,OATH J")
                  .build())
          .medication(
              DatamartReference.of()
                  .type("Medication")
                  .reference("1400021372")
                  .display("RIVAROXABAN 15MG TAB")
                  .build())
          .dosageInstruction(dosageInstructions())
          .dispenseRequest(Optional.of(dispenseRequest()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Dstu2 {

    static MedicationOrder.Bundle asBundle(
        String basePath, Collection<MedicationOrder> records, BundleLink... links) {
      return MedicationOrder.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(records.size())
          .link(asList(links))
          .entry(
              records
                  .stream()
                  .map(
                      c ->
                          MedicationOrder.Entry.builder()
                              .fullUrl(basePath + "/MedicationOrder/" + c.id())
                              .resource(c)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static BundleLink link(BundleLink.LinkRelation relation, String base, int page, int count) {
      return BundleLink.builder()
          .relation(relation)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    MedicationOrder.DispenseRequest dispenseRequest() {
      return MedicationOrder.DispenseRequest.builder()
          .numberOfRepeatsAllowed(1)
          .quantity(SimpleQuantity.builder().value(42.0).unit("TAB").build())
          .expectedSupplyDuration(
              Duration.builder()
                  .value((double) 21)
                  .unit("days")
                  .code("d")
                  .system("http://unitsofmeasure.org")
                  .build())
          .build();
    }

    List<MedicationOrder.DosageInstruction> dosageInstructions() {
      return asList(
          MedicationOrder.DosageInstruction.builder()
              .text("TAKE ONE TABLET BY MOUTH TWICE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS")
              .timing(Timing.builder().code(CodeableConcept.builder().text("BID").build()).build())
              .additionalInstructions(
                  CodeableConcept.builder().text("DO NOT TAKE NSAIDS WITH THIS MEDICATION").build())
              .asNeededBoolean(false)
              .route(CodeableConcept.builder().text("ORAL").build())
              .doseQuantity(SimpleQuantity.builder().value(1.0).unit("TAB").build())
              .build(),
          MedicationOrder.DosageInstruction.builder()
              .text("THEN TAKE ONE TABLET BY MOUTH ONCE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS")
              .timing(
                  Timing.builder().code(CodeableConcept.builder().text("QDAILY").build()).build())
              .additionalInstructions(
                  CodeableConcept.builder().text("DO NOT TAKE NSAIDS WITH THIS MEDICATION").build())
              .asNeededBoolean(false)
              .route(CodeableConcept.builder().text("ORAL").build())
              .doseQuantity(SimpleQuantity.builder().value(1.0).unit("TAB").build())
              .build());
    }

    public MedicationOrder medicationOrder(String id) {
      return medicationOrder(id, "666V666");
    }

    MedicationOrder medicationOrder(String cdwId, String icn) {
      return MedicationOrder.builder()
          .resourceType("MedicationOrder")
          .id(cdwId)
          .patient(
              Reference.builder().reference("Patient/" + icn).display("VETERAN,FARM ACY").build())
          .dateWritten("2016-11-17T18:02:04Z")
          .status(Status.stopped)
          .dateEnded("2017-02-15T05:00:00Z")
          .prescriber(
              Reference.builder()
                  .reference("Practitioner/1404497883")
                  .display("HIPPOCRATES,OATH J")
                  .build())
          .medicationReference(
              Reference.builder()
                  .reference("Medication/1400021372")
                  .display("RIVAROXABAN 15MG TAB")
                  .build())
          .dosageInstruction(dosageInstructions())
          .dispenseRequest(dispenseRequest())
          .build();
    }
  }
}
