package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static java.util.Collections.singletonList;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Bundle;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Entry;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Timing;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DatamartMedicationStatementSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {

    public DatamartMedicationStatement medicationStatement() {
      return medicationStatement("800008482786", "666V666");
    }

    public DatamartMedicationStatement medicationStatement(String cdwId, String patientId) {
      return DatamartMedicationStatement.builder()
          .cdwId(cdwId)
          .etlDate("2014-12-06T05:53:02Z")
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference(patientId)
                  .display("BARKER,BOBBIE LEE")
                  .build())
          .dateAsserted(Instant.parse("2017-11-03T01:39:21Z"))
          .status(DatamartMedicationStatement.Status.completed)
          .effectiveDateTime(Optional.of(Instant.parse("2017-11-03T01:39:21Z")))
          .note(Optional.of("NOTES NOTES NOTES"))
          .medication(
              DatamartReference.of()
                  .type("Medication")
                  .reference("123456789")
                  .display("SAW PALMETTO")
                  .build())
          .dosage(
              DatamartMedicationStatement.Dosage.builder()
                  .text(Optional.of("1"))
                  .timingCodeText(Optional.of("EVERYDAY"))
                  .routeText(Optional.of("MOUTH"))
                  .build())
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {

    static Bundle asBundle(
        String baseUrl, Collection<MedicationStatement> medicationStatements, BundleLink... links) {
      return Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(medicationStatements.size())
          .link(Arrays.asList(links))
          .entry(
              medicationStatements
                  .stream()
                  .map(
                      c ->
                          Entry.builder()
                              .fullUrl(baseUrl + "/MedicationStatement/" + c.id())
                              .resource(c)
                              .search(Search.builder().mode(SearchMode.match).build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static BundleLink link(LinkRelation rel, String base, int page, int count) {
      return BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    private List<MedicationStatement.Dosage> Dosage() {
      return singletonList(
          MedicationStatement.Dosage.builder().route(route()).text("1").timing(timing()).build());
    }

    public MedicationStatement medicationStatement() {
      return medicationStatement("800008482786");
    }

    public MedicationStatement medicationStatement(String id) {
      return medicationStatement(id, "666V666");
    }

    public MedicationStatement medicationStatement(String id, String patiendId) {
      return MedicationStatement.builder()
          .resourceType("MedicationStatement")
          .id(id)
          .patient(reference("Patient/" + patiendId, "BARKER,BOBBIE LEE"))
          .dateAsserted("2017-11-03T01:39:21Z")
          .note("NOTES NOTES NOTES")
          .status(MedicationStatement.Status.completed)
          .effectiveDateTime("2017-11-03T01:39:21Z")
          .medicationReference(reference("Medication/123456789", "SAW PALMETTO"))
          .dosage(Dosage())
          .build();
    }

    private Reference reference(String ref, String display) {
      return Reference.builder().reference(ref).display(display).build();
    }

    private CodeableConcept route() {
      return CodeableConcept.builder().text("MOUTH").build();
    }

    private Timing timing() {
      return Timing.builder().code(timingCode()).build();
    }

    private CodeableConcept timingCode() {
      return CodeableConcept.builder().text("EVERYDAY").build();
    }
  }
}
