package gov.va.api.health.dataquery.service.controller.medicationrequest;

import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.Annotation;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Duration;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.datatypes.Timing;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.uscorer4.api.resources.MedicationRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MedicationRequestSamples {
  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    static MedicationRequest.Bundle asBundle(
        String baseUrl,
        Collection<MedicationRequest> resources,
        int totalRecords,
        BundleLink... links) {
      return MedicationRequest.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources.stream()
                  .map(
                      c ->
                          MedicationRequest.Entry.builder()
                              .fullUrl(baseUrl + "/MedicationRequest/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.r4.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode
                                              .match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static List<Dosage> dosageInstructionFromMedicationOrder() {
      return List.of(
          Dosage.builder()
              .text("TAKE ONE TABLET BY MOUTH TWICE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS")
              .timing(
                  Timing.builder()
                      .code(CodeableConcept.builder().text("BID").build())
                      .repeat(
                          Timing.Repeat.builder()
                              .boundsPeriod(
                                  Period.builder()
                                      .start("2016-11-17T18:02:04Z")
                                      .end("2017-02-15T05:00:00Z")
                                      .build())
                              .build())
                      .build())
              .additionalInstruction(
                  List.of(
                      CodeableConcept.builder()
                          .text("DO NOT TAKE NSAIDS WITH THIS MEDICATION")
                          .build()))
              .asNeededBoolean(false)
              .route(CodeableConcept.builder().text("ORAL").build())
              .doseAndRate(
                  List.of(
                      Dosage.DoseAndRate.builder()
                          .doseQuantity(
                              SimpleQuantity.builder()
                                  .value(BigDecimal.valueOf(1.0))
                                  .unit("TAB")
                                  .build())
                          .build()))
              .build(),
          Dosage.builder()
              .text("THEN TAKE ONE TABLET BY MOUTH ONCE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS")
              .timing(
                  Timing.builder()
                      .code(CodeableConcept.builder().text("QDAILY").build())
                      .repeat(
                          Timing.Repeat.builder()
                              .boundsPeriod(
                                  Period.builder()
                                      .start("2016-11-17T18:02:04Z")
                                      .end("2017-02-15T05:00:00Z")
                                      .build())
                              .build())
                      .build())
              .additionalInstruction(
                  List.of(
                      CodeableConcept.builder()
                          .text("DO NOT TAKE NSAIDS WITH THIS MEDICATION")
                          .build()))
              .asNeededBoolean(false)
              .route(CodeableConcept.builder().text("ORAL").build())
              .doseAndRate(
                  List.of(
                      Dosage.DoseAndRate.builder()
                          .doseQuantity(
                              SimpleQuantity.builder()
                                  .value(BigDecimal.valueOf(1.0))
                                  .unit("TAB")
                                  .build())
                          .build()))
              .build());
    }

    static List<Dosage> dosageInstructionFromMedicationOrderNoAdditionalInfo() {
      return List.of(
          Dosage.builder()
              .text("TAKE ONE TABLET BY MOUTH TWICE A DAY FOR 7 DAYS TO PREVENT BLOOD CLOTS")
              .timing(
                  Timing.builder()
                      .code(CodeableConcept.builder().text("BID").build())
                      .repeat(
                          Timing.Repeat.builder()
                              .boundsPeriod(
                                  Period.builder()
                                      .start("2016-11-17T18:02:04Z")
                                      .end("2017-02-15T05:00:00Z")
                                      .build())
                              .build())
                      .build())
              .additionalInstruction(null)
              .asNeededBoolean(false)
              .route(CodeableConcept.builder().text("ORAL").build())
              .doseAndRate(
                  List.of(
                      Dosage.DoseAndRate.builder()
                          .doseQuantity(
                              SimpleQuantity.builder()
                                  .value(BigDecimal.valueOf(1.0))
                                  .unit("TAB")
                                  .build())
                          .build()))
              .build());
    }

    static BundleLink link(
        gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    private MedicationRequest.DispenseRequest dispenseRequestFromMedicationOrder() {
      return MedicationRequest.DispenseRequest.builder()
          .numberOfRepeatsAllowed(1)
          .quantity(SimpleQuantity.builder().value(BigDecimal.valueOf(42.0)).unit("TAB").build())
          .expectedSupplyDuration(
              Duration.builder()
                  .value(BigDecimal.valueOf(21))
                  .unit("days")
                  .code("d")
                  .system("http://unitsofmeasure.org")
                  .build())
          .build();
    }

    private List<Dosage> dosageInstructionFromMedicationStatement() {
      return List.of(
          Dosage.builder()
              .text("1")
              .timing(
                  Timing.builder()
                      .code(CodeableConcept.builder().text("EVERYDAY").build())
                      .repeat(
                          Timing.Repeat.builder()
                              .boundsPeriod(Period.builder().start("2017-11-03T01:39:21Z").build())
                              .build())
                      .build())
              .route(CodeableConcept.builder().text("MOUTH").build())
              .build());
    }

    public MedicationRequest medicationRequestFromMedicationOrder() {
      return medicationRequestFromMedicationOrder("1400181354458:O", "666V666");
    }

    public MedicationRequest medicationRequestFromMedicationOrder(String id) {
      return medicationRequestFromMedicationOrder(id, "666V666");
    }

    public MedicationRequest medicationRequestFromMedicationOrder(String id, String patientId) {
      return MedicationRequest.builder()
          .resourceType("MedicationRequest")
          .id(id)
          .subject(reference("Patient/" + patientId, "VETERAN,FARM ACY"))
          .authoredOn("2016-11-17T18:02:04Z")
          .status(MedicationRequest.Status.stopped)
          .intent(MedicationRequest.Intent.order)
          .requester(reference("Practitioner/1404497883", "HIPPOCRATES,OATH J"))
          .medicationReference(reference("Medication/1400021372", "RIVAROXABAN 15MG TAB"))
          .dosageInstruction(dosageInstructionFromMedicationOrder())
          .dispenseRequest(dispenseRequestFromMedicationOrder())
          .build();
    }

    public MedicationRequest medicationRequestFromMedicationStatement() {
      return medicationRequestFromMedicationStatement("800008482786");
    }

    public MedicationRequest medicationRequestFromMedicationStatement(String id) {
      return medicationRequestFromMedicationStatement(id, "666V666");
    }

    public MedicationRequest medicationRequestFromMedicationStatement(String id, String patientId) {
      return MedicationRequest.builder()
          .resourceType("MedicationRequest")
          .id(id)
          .subject(reference("Patient/" + patientId, "BARKER,BOBBIE LEE"))
          .authoredOn("2017-11-03T01:39:21Z")
          .status(MedicationRequest.Status.completed)
          .note(List.of(Annotation.builder().text("NOTES NOTES NOTES").build()))
          .medicationReference(reference("Medication/123456789", "SAW PALMETTO"))
          .dosageInstruction(dosageInstructionFromMedicationStatement())
          .reportedBoolean(true)
          .intent(MedicationRequest.Intent.plan)
          ._requester(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
          .build();
    }

    private Reference reference(String ref, String display) {
      return Reference.builder().reference(ref).display(display).build();
    }
  }
}
