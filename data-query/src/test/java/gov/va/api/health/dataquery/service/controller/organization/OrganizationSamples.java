package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

public class OrganizationSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {

    public DatamartOrganization organization() {
      return organization("1234");
    }

    DatamartOrganization organization(String id) {
      return DatamartOrganization.builder()
          .cdwId(id)
          .stationIdentifier(Optional.of("442"))
          .npi(Optional.of("1205983228"))
          .providerId(Optional.of("0040000000000"))
          .ediId(Optional.of("36273"))
          .agencyId(Optional.of("other"))
          .active(true)
          .type(
              Optional.of(
                  DatamartCoding.builder()
                      .system(Optional.of("institution"))
                      .code(Optional.of("CBOC"))
                      .display(Optional.of("COMMUNITY BASED OUTPATIENT CLINIC"))
                      .build()))
          .name("NEW AMSTERDAM CBOC")
          .telecom(
              asList(
                  DatamartOrganization.Telecom.builder()
                      .system(DatamartOrganization.Telecom.System.phone)
                      .value("(800) 555-7710")
                      .build(),
                  DatamartOrganization.Telecom.builder()
                      .system(DatamartOrganization.Telecom.System.fax)
                      .value("800-555-7720")
                      .build(),
                  DatamartOrganization.Telecom.builder()
                      .system(DatamartOrganization.Telecom.System.phone)
                      .value("800-555-7730")
                      .build()))
          .address(
              DatamartOrganization.Address.builder()
                  .line1("10 MONROE AVE, SUITE 6B")
                  .line2("PO BOX 4160")
                  .city("NEW AMSTERDAM")
                  .state("OH")
                  .postalCode("44444-4160")
                  .build())
          .partOf(
              Optional.of(
                  DatamartReference.builder()
                      .reference(Optional.of("568060:I"))
                      .display(Optional.of("NEW AMSTERDAM VAMC"))
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Dstu2 {

    static gov.va.api.health.dstu2.api.resources.Organization.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.dstu2.api.resources.Organization> organizations,
        gov.va.api.health.dstu2.api.bundle.BundleLink... links) {
      return gov.va.api.health.dstu2.api.resources.Organization.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType.searchset)
          .total(organizations.size())
          .link(Arrays.asList(links))
          .entry(
              organizations
                  .stream()
                  .map(
                      c ->
                          gov.va.api.health.dstu2.api.resources.Organization.Entry.builder()
                              .fullUrl(baseUrl + "/Organization/" + c.id())
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

    public gov.va.api.health.dstu2.api.resources.Organization organization() {
      return organization("1234");
    }

    gov.va.api.health.dstu2.api.resources.Organization organization(String id) {
      return gov.va.api.health.dstu2.api.resources.Organization.builder()
          .resourceType("Organization")
          .id(id)
          .active(true)
          .type(
              gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                  .coding(
                      asList(
                          gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                              .system("institution")
                              .code("CBOC")
                              .display("COMMUNITY BASED OUTPATIENT CLINIC")
                              .build()))
                  .build())
          .name("NEW AMSTERDAM CBOC")
          .telecom(
              asList(
                  gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .value("(800) 555-7710")
                      .build(),
                  gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem.fax)
                      .value("800-555-7720")
                      .build(),
                  gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .value("800-555-7730")
                      .build()))
          .address(
              Collections.singletonList(
                  gov.va.api.health.dstu2.api.datatypes.Address.builder()
                      .line(Arrays.asList("10 MONROE AVE, SUITE 6B", "PO BOX 4160"))
                      .city("NEW AMSTERDAM")
                      .state("OH")
                      .postalCode("44444-4160")
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Stu3 {

    static gov.va.api.health.stu3.api.resources.Organization.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.stu3.api.resources.Organization> organizations,
        gov.va.api.health.stu3.api.bundle.BundleLink... links) {
      return gov.va.api.health.stu3.api.resources.Organization.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.stu3.api.bundle.AbstractBundle.BundleType.searchset)
          .total(organizations.size())
          .link(Arrays.asList(links))
          .entry(
              organizations
                  .stream()
                  .map(
                      c ->
                          gov.va.api.health.stu3.api.resources.Organization.Entry.builder()
                              .fullUrl(baseUrl + "/Organization/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.stu3.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.stu3.api.bundle.AbstractEntry.SearchMode
                                              .match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static gov.va.api.health.stu3.api.bundle.BundleLink link(
        gov.va.api.health.stu3.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.stu3.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public gov.va.api.health.stu3.api.resources.Organization organization() {
      return organization("1234");
    }

    gov.va.api.health.stu3.api.resources.Organization organization(String id) {
      return gov.va.api.health.stu3.api.resources.Organization.builder()
          .resourceType("Organization")
          .id(id)
          .identifier(
              asList(
                  gov.va.api.health.stu3.api.resources.Organization.OrganizationIdentifier.builder()
                      .system("http://hl7.org/fhir/sid/us-npi")
                      .value("1205983228")
                      .build()))
          .active(true)
          .type(
              asList(
                  gov.va.api.health.stu3.api.datatypes.CodeableConcept.builder()
                      .coding(
                          asList(
                              gov.va.api.health.stu3.api.datatypes.Coding.builder()
                                  .system("institution")
                                  .code("CBOC")
                                  .display("COMMUNITY BASED OUTPATIENT CLINIC")
                                  .build()))
                      .build()))
          .name("NEW AMSTERDAM CBOC")
          .telecom(
              asList(
                  gov.va.api.health.stu3.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .value("(800) 555-7710")
                      .build(),
                  gov.va.api.health.stu3.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem.fax)
                      .value("800-555-7720")
                      .build(),
                  gov.va.api.health.stu3.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .value("800-555-7730")
                      .build()))
          .address(
              asList(
                  gov.va.api.health.stu3.api.resources.Organization.OrganizationAddress.builder()
                      .line(asList("10 MONROE AVE, SUITE 6B", "PO BOX 4160"))
                      .city("NEW AMSTERDAM")
                      .state("OH")
                      .postalCode("44444-4160")
                      .build()))
          .build();
    }
  }
}
