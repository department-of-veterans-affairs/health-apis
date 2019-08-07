package gov.va.api.health.dataquery.service.controller.procedure;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.argonaut.api.resources.Procedure.Bundle;
import gov.va.api.health.argonaut.api.resources.Procedure.Entry;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure.Status;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
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
class DatamartProcedureSamples {
  @AllArgsConstructor(staticName = "create")
  static class Datamart {

    DatamartProcedure procedure() {
      return procedure("1000000719261", "1004476237V111282", "2008-01-02T06:00:00Z");
    }

    private DatamartProcedure procedure(String cdwId, String patientId, String performedOn) {
      return DatamartProcedure.builder()
          .cdwId(cdwId)
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference(patientId)
                  .display("VETERAN,GRAY PRO")
                  .build())
          .status(Status.completed)
          .coding(
              DatamartCoding.of()
                  .system("http://www.ama-assn.org/go/cpt")
                  .code("90870")
                  .display("ELECTROCONVULSIVE THERAPY")
                  .build())
          .notPerformed(true)
          .reasonNotPerformed(Optional.of("CASE MOVED TO EARLIER DATE"))
          .performedDateTime(Optional.of(Instant.parse(performedOn)))
          .encounter(
              Optional.of(
                  DatamartReference.of()
                      .type("Encounter")
                      .reference("1000124525706")
                      .display("1000124525706")
                      .build()))
          .location(
              Optional.of(
                  DatamartReference.of()
                      .type("Location")
                      .reference("237281")
                      .display("ZZPSYCHIATRY")
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {
    static Bundle asBundle(String baseUrl, Collection<Procedure> resources, BundleLink... links) {
      return Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(resources.size())
          .link(Arrays.asList(links))
          .entry(
              resources
                  .stream()
                  .map(
                      c ->
                          Entry.builder()
                              .fullUrl(baseUrl + "/Procedure/" + c.id())
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

    Procedure procedure() {
      return procedure("1000000719261", "1004476237V111282", "2008-01-02T06:00:00Z");
    }

    Procedure procedure(String id, String patientId, String performedOn) {
      return Procedure.builder()
          .resourceType("Procedure")
          .id(id)
          .subject(reference("VETERAN,GRAY PRO", "Patient/" + patientId))
          .status(Procedure.Status.completed)
          .code(
              CodeableConcept.builder()
                  .coding(
                      List.of(
                          Coding.builder()
                              .code("90870")
                              .system("http://www.ama-assn.org/go/cpt")
                              .display("ELECTROCONVULSIVE THERAPY")
                              .build()))
                  .build())
          .notPerformed(true)
          .reasonNotPerformed(
              List.of(CodeableConcept.builder().text("CASE MOVED TO EARLIER DATE").build()))
          .performedDateTime(performedOn)
          .encounter(reference("1000124525706", "Encounter/1000124525706"))
          .location(reference("ZZPSYCHIATRY", "Location/237281"))
          .build();
    }

    Reference reference(String display, String ref) {
      return Reference.builder().display(display).reference(ref).build();
    }
  }
}
