package gov.va.api.health.dataquery.service.controller.immunization;

import gov.va.api.health.argonaut.api.resources.Immunization;
import gov.va.api.health.argonaut.api.resources.Immunization.Bundle;
import gov.va.api.health.argonaut.api.resources.Immunization.Entry;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
class DatamartImmunizationSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {
    DatamartImmunization immunization() {
      return immunization("1000000030337", "1011549983V753765");
    }

    DatamartImmunization immunization(String cdwId, String patientId) {
      return DatamartImmunization.builder()
          .cdwId(cdwId)
          .status(DatamartImmunization.Status.completed)
          .etlDate("1997-04-03T21:02:15Z")
          .vaccineCode(
              DatamartImmunization.VaccineCode.builder()
                  .code("112")
                  .text("TETANUS TOXOID, UNSPECIFIED FORMULATION")
                  .build())
          .patient(
              DatamartReference.of()
                  .type("Patient")
                  .reference(patientId)
                  .display("ZZTESTPATIENT,THOMAS THE")
                  .build())
          .wasNotGiven(false)
          .performer(
              Optional.of(
                  DatamartReference.of()
                      .type("Practitioner")
                      .reference("3868169")
                      .display("ZHIVAGO,YURI ANDREYEVICH")
                      .build()))
          .requester(
              Optional.of(
                  DatamartReference.of()
                      .type("Practitioner")
                      .reference("1702436")
                      .display("SHINE,DOC RAINER")
                      .build()))
          .encounter(
              Optional.of(
                  DatamartReference.of()
                      .type("Encounter")
                      .reference("1000589847194")
                      .display("1000589847194")
                      .build()))
          .location(
              Optional.of(
                  DatamartReference.of()
                      .type("Location")
                      .reference("358359")
                      .display("ZZGOLD PRIMARY CARE")
                      .build()))
          .note(Optional.of("PATIENT CALM AFTER VACCINATION"))
          .reaction(
              Optional.of(
                  DatamartReference.of()
                      .type("Observation")
                      .reference(null)
                      .display("Other")
                      .build()))
          .vaccinationProtocols(
              Optional.of(
                  DatamartImmunization.VaccinationProtocols.builder()
                      .series("Booster")
                      .seriesDoses(1)
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {
    static Bundle asBundle(
        String baseUrl, Collection<Immunization> conditions, BundleLink... links) {
      return Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(conditions.size())
          .link(Arrays.asList(links))
          .entry(
              conditions
                  .stream()
                  .map(
                      c ->
                          Entry.builder()
                              .fullUrl(baseUrl + "/Immunization/" + c.id())
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
    // TODO these will likely come from Evans transformer work
    Immunization immunization(String id, String patientId) {
      return Immunization.builder()
          .resourceType(Immunization.class.getSimpleName())
          .id(id)
          .patient(Reference.builder().reference("Patient/" + patientId).display(patientId).build())
          .build();
    }
  }
}
