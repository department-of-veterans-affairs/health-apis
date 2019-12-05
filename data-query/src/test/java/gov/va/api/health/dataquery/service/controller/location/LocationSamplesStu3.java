package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.stu3.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.stu3.api.bundle.AbstractEntry.Search;
import gov.va.api.health.stu3.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.stu3.api.bundle.BundleLink;
import gov.va.api.health.stu3.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.elements.Reference;
import gov.va.api.health.stu3.api.resources.Location;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
final class LocationSamplesStu3 {
  @AllArgsConstructor(staticName = "create")
  static class Datamart {
    DatamartLocation location(String id, String organizationId) {
      return DatamartLocation.builder()
          .cdwId(id)
          .status(DatamartLocation.Status.active)
          .name("TEM MH PSO TRS IND93EH")
          .description(Optional.of("BLDG 146, RM W02"))
          .type(Optional.of("PSYCHIATRY CLINIC"))
          .telecom("254-743-2867")
          .address(
              DatamartLocation.Address.builder()
                  .line1("1901 VETERANS MEMORIAL DRIVE")
                  .city("TEMPLE")
                  .state("TEXAS")
                  .postalCode("76504")
                  .build())
          .physicalType(Optional.of("BLDG 146, RM W02"))
          .managingOrganization(
              DatamartReference.builder()
                  .reference(Optional.of(organizationId))
                  .display(Optional.of("OLIN E. TEAGUE VET CENTER"))
                  .build())
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class Fhir {
    static Location.Bundle asBundle(
        String baseUrl, Collection<Location> locations, BundleLink... links) {
      return Location.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(locations.size())
          .link(Arrays.asList(links))
          .entry(
              locations
                  .stream()
                  .map(
                      c ->
                          Location.Entry.builder()
                              .fullUrl(baseUrl + "/Location/" + c.id())
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

    Location location(String id, String organizationId) {
      return Location.builder()
          .resourceType("Location")
          .id(id)
          .address(
              Location.LocationAddress.builder()
                  .line(asList("1901 VETERANS MEMORIAL DRIVE"))
                  .city("TEMPLE")
                  .state("TEXAS")
                  .postalCode("76504")
                  .text("1901 VETERANS MEMORIAL DRIVE TEMPLE TEXAS 76504")
                  .build())
          .description("BLDG 146, RM W02")
          .managingOrganization(
              Reference.builder()
                  .reference("Organization/" + organizationId)
                  .display("OLIN E. TEAGUE VET CENTER")
                  .build())
          .mode(Location.Mode.instance)
          .name("TEM MH PSO TRS IND93EH")
          .physicalType(
              CodeableConcept.builder()
                  .coding(asList(Coding.builder().display("BLDG 146, RM W02").build()))
                  .build())
          .status(Location.Status.active)
          .telecom(
              asList(
                  ContactPoint.builder()
                      .system(ContactPoint.ContactPointSystem.phone)
                      .value("254-743-2867")
                      .build()))
          .type(
              CodeableConcept.builder()
                  .coding(asList(Coding.builder().display("PSYCHIATRY CLINIC").build()))
                  .build())
          .build();
    }
  }
}
