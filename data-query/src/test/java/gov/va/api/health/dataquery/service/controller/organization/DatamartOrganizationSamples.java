package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.resources.Organization;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

public class DatamartOrganizationSamples {

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
  static class Fhir {

    static Organization.Bundle asBundle(
        String baseUrl, Collection<Organization> organizations, BundleLink... links) {
      return Organization.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(organizations.size())
          .link(Arrays.asList(links))
          .entry(
              organizations
                  .stream()
                  .map(
                      c ->
                          Organization.Entry.builder()
                              .fullUrl(baseUrl + "/Organization/" + c.id())
                              .resource(c)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static BundleLink link(BundleLink.LinkRelation rel, String base, int page, int count) {
      return BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public Organization organization() {
      return organization("1234");
    }

    Organization organization(String id) {
      return Organization.builder()
          .resourceType("Organization")
          .id(id)
          .active(true)
          .type(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("institution")
                              .code("CBOC")
                              .display("COMMUNITY BASED OUTPATIENT CLINIC")
                              .build()))
                  .build())
          .name("NEW AMSTERDAM CBOC")
          .telecom(
              asList(
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("(800) 555-7710")
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.fax)
                      .value("800-555-7720")
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("800-555-7730")
                      .build()))
          .address(
              Collections.singletonList(
                  Address.builder()
                      .line(Arrays.asList("10 MONROE AVE, SUITE 6B", "PO BOX 4160"))
                      .city("NEW AMSTERDAM")
                      .state("OH")
                      .postalCode("44444-4160")
                      .build()))
          .build();
    }
  }
}
