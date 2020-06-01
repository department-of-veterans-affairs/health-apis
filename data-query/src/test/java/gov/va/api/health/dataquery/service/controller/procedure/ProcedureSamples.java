package gov.va.api.health.dataquery.service.controller.procedure;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.argonaut.api.resources.Procedure.Bundle;
import gov.va.api.health.argonaut.api.resources.Procedure.Entry;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure.Status;
import gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcedureSamples {
  @AllArgsConstructor(staticName = "create")
  public static class Datamart {

    public DatamartProcedure procedure() {
      return procedure("1000000719261", "1004476237V111282", "2008-01-02T06:00:00Z");
    }

    DatamartProcedure procedure(String cdwId, String patientId, String performedOn) {
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
  public static class Dstu2 {
    static Bundle asBundle(
        String baseUrl,
        Collection<Procedure> resources,
        int totalRecords,
        gov.va.api.health.dstu2.api.bundle.BundleLink... links) {
      return Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources.stream()
                  .map(
                      c ->
                          Entry.builder()
                              .fullUrl(baseUrl + "/Procedure/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.dstu2.api.bundle.AbstractEntry
                                              .SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static gov.va.api.health.dstu2.api.bundle.BundleLink link(
        gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.dstu2.api.bundle.BundleLink.builder()
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
              gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                  .coding(
                      List.of(
                          gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                              .code("90870")
                              .system("http://www.ama-assn.org/go/cpt")
                              .display("ELECTROCONVULSIVE THERAPY")
                              .build()))
                  .build())
          .notPerformed(true)
          .reasonNotPerformed(
              List.of(
                  gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                      .text("CASE MOVED TO EARLIER DATE")
                      .build()))
          .performedDateTime(performedOn)
          .location(reference("ZZPSYCHIATRY", "Location/237281"))
          .build();
    }

    gov.va.api.health.dstu2.api.elements.Reference reference(String display, String ref) {
      return gov.va.api.health.dstu2.api.elements.Reference.builder()
          .display(display)
          .reference(ref)
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    static gov.va.api.health.uscorer4.api.resources.Procedure.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.uscorer4.api.resources.Procedure> resources,
        int totalRecords,
        BundleLink... links) {
      return gov.va.api.health.uscorer4.api.resources.Procedure.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources.stream()
                  .map(
                      c ->
                          gov.va.api.health.uscorer4.api.resources.Procedure.Entry.builder()
                              .fullUrl(baseUrl + "/Procedure/" + c.id())
                              .resource(c)
                              .search(AbstractEntry.Search.builder().mode(SearchMode.match).build())
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

    gov.va.api.health.uscorer4.api.resources.Procedure procedure() {
      return procedure("1000000719261", "1004476237V111282", "2008-01-02T06:00:00Z");
    }

    gov.va.api.health.uscorer4.api.resources.Procedure procedure(
        String id, String patientId, String performedOn) {
      return gov.va.api.health.uscorer4.api.resources.Procedure.builder()
          .resourceType("Procedure")
          .id(id)
          .subject(reference("VETERAN,GRAY PRO", "Patient/" + patientId))
          .status(gov.va.api.health.uscorer4.api.resources.Procedure.Status.completed)
          .code(
              gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                  .coding(
                      List.of(
                          gov.va.api.health.r4.api.datatypes.Coding.builder()
                              .code("90870")
                              .system("http://www.ama-assn.org/go/cpt")
                              .display("ELECTROCONVULSIVE THERAPY")
                              .build()))
                  .build())
          .statusReason(
              gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                  .text("CASE MOVED TO EARLIER DATE")
                  .build())
          .performedDateTime(performedOn)
          .location(reference("ZZPSYCHIATRY", "Location/237281"))
          .build();
    }

    gov.va.api.health.r4.api.elements.Reference reference(String display, String ref) {
      return gov.va.api.health.r4.api.elements.Reference.builder()
          .display(display)
          .reference(ref)
          .build();
    }
  }
}
