package gov.va.api.health.dataquery.service.controller.location;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LocationSamples {
  // Location Search Params
  public static final String LOCATION_NAME = "TEM MH PSO TRS IND93EH";

  public static final String LOCATION_ADDRESS_STREET = "1901 VETERANS MEMORIAL DRIVE";

  public static final String LOCATION_ADDRESS_CITY = "TEMPLE";

  public static final String LOCATION_ADDRESS_STATE = "TEXAS";

  public static final String LOCATION_ADDRESS_POSTAL_CODE = "76504";

  @AllArgsConstructor(staticName = "create")
  static final class Datamart {
    DatamartLocation location() {
      return location("123", "456");
    }

    DatamartLocation location(String id, String organizationId) {
      return DatamartLocation.builder()
          .cdwId(id)
          .status(DatamartLocation.Status.active)
          .name(LOCATION_NAME)
          .description(Optional.of("BLDG 146, RM W02"))
          .type(Optional.of("PSYCHIATRY CLINIC"))
          .telecom("254-743-2867")
          .address(
              DatamartLocation.Address.builder()
                  .line1(LOCATION_ADDRESS_STREET)
                  .city(LOCATION_ADDRESS_CITY)
                  .state(LOCATION_ADDRESS_STATE)
                  .postalCode(LOCATION_ADDRESS_POSTAL_CODE)
                  .build())
          .physicalType(Optional.of("BLDG 146, RM W02"))
          .managingOrganization(
              gov.va.api.health.dataquery.service.controller.datamart.DatamartReference.builder()
                  .type(Optional.of("Organization"))
                  .reference(Optional.of(organizationId))
                  .display(Optional.of("OLIN E. TEAGUE VET CENTER"))
                  .build())
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static final class Dstu2 {
    static gov.va.api.health.dstu2.api.resources.Location.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.dstu2.api.resources.Location> locations,
        gov.va.api.health.dstu2.api.bundle.BundleLink... links) {
      return gov.va.api.health.dstu2.api.resources.Location.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType.searchset)
          .total(locations.size())
          .link(Arrays.asList(links))
          .entry(
              locations.stream()
                  .map(
                      c ->
                          gov.va.api.health.dstu2.api.resources.Location.Entry.builder()
                              .fullUrl(baseUrl + "/Location/" + c.id())
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

    gov.va.api.health.dstu2.api.resources.Location location(String id, String organizationId) {
      return gov.va.api.health.dstu2.api.resources.Location.builder()
          .resourceType("Location")
          .id(id)
          .address(
              gov.va.api.health.dstu2.api.datatypes.Address.builder()
                  .line(asList(LOCATION_ADDRESS_STREET))
                  .city(LOCATION_ADDRESS_CITY)
                  .state(LOCATION_ADDRESS_STATE)
                  .postalCode(LOCATION_ADDRESS_POSTAL_CODE)
                  .build())
          .description("BLDG 146, RM W02")
          .managingOrganization(
              gov.va.api.health.dstu2.api.elements.Reference.builder()
                  .reference("Organization/" + organizationId)
                  .display("OLIN E. TEAGUE VET CENTER")
                  .build())
          .mode(gov.va.api.health.dstu2.api.resources.Location.Mode.instance)
          .name(LOCATION_NAME)
          .physicalType(
              gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                  .coding(
                      asList(
                          gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                              .display("BLDG 146, RM W02")
                              .build()))
                  .build())
          .status(gov.va.api.health.dstu2.api.resources.Location.Status.active)
          .telecom(
              asList(
                  gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .value("254-743-2867")
                      .build()))
          .type(
              gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                  .coding(
                      asList(
                          gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                              .display("PSYCHIATRY CLINIC")
                              .build()))
                  .build())
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static final class Stu3 {
    static gov.va.api.health.stu3.api.resources.Location.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.stu3.api.resources.Location> locations,
        gov.va.api.health.stu3.api.bundle.BundleLink... links) {
      return gov.va.api.health.stu3.api.resources.Location.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.stu3.api.bundle.AbstractBundle.BundleType.searchset)
          .total(locations.size())
          .link(Arrays.asList(links))
          .entry(
              locations.stream()
                  .map(
                      c ->
                          gov.va.api.health.stu3.api.resources.Location.Entry.builder()
                              .fullUrl(baseUrl + "/Location/" + c.id())
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

    gov.va.api.health.stu3.api.resources.Location location(String id, String organizationId) {
      return gov.va.api.health.stu3.api.resources.Location.builder()
          .resourceType("Location")
          .id(id)
          .address(
              gov.va.api.health.stu3.api.resources.Location.LocationAddress.builder()
                  .line(asList(LOCATION_ADDRESS_STREET))
                  .city(LOCATION_ADDRESS_CITY)
                  .state(LOCATION_ADDRESS_STATE)
                  .postalCode(LOCATION_ADDRESS_POSTAL_CODE)
                  .text(
                      Stream.of(
                              LOCATION_ADDRESS_STREET,
                              LOCATION_ADDRESS_CITY,
                              LOCATION_ADDRESS_STATE,
                              LOCATION_ADDRESS_POSTAL_CODE)
                          .collect(Collectors.joining(" ")))
                  .build())
          .description("BLDG 146, RM W02")
          .managingOrganization(
              gov.va.api.health.stu3.api.elements.Reference.builder()
                  .reference("Organization/" + organizationId)
                  .display("OLIN E. TEAGUE VET CENTER")
                  .build())
          .mode(gov.va.api.health.stu3.api.resources.Location.Mode.instance)
          .name(LOCATION_NAME)
          .physicalType(
              gov.va.api.health.stu3.api.datatypes.CodeableConcept.builder()
                  .coding(
                      asList(
                          gov.va.api.health.stu3.api.datatypes.Coding.builder()
                              .display("BLDG 146, RM W02")
                              .build()))
                  .build())
          .status(gov.va.api.health.stu3.api.resources.Location.Status.active)
          .telecom(
              asList(
                  gov.va.api.health.stu3.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .value("254-743-2867")
                      .build()))
          .type(
              gov.va.api.health.stu3.api.datatypes.CodeableConcept.builder()
                  .coding(
                      asList(
                          gov.va.api.health.stu3.api.datatypes.Coding.builder()
                              .display("PSYCHIATRY CLINIC")
                              .build()))
                  .build())
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static final class R4 {
    static gov.va.api.health.r4.api.resources.Location.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.r4.api.resources.Location> locations,
        int totalRecords,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return gov.va.api.health.r4.api.resources.Location.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              locations.stream()
                  .map(
                      c ->
                          gov.va.api.health.r4.api.resources.Location.Entry.builder()
                              .fullUrl(baseUrl + "/Location/" + c.id())
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

    static gov.va.api.health.r4.api.bundle.BundleLink link(
        gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.r4.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    gov.va.api.health.r4.api.resources.Location location() {
      return location("123");
    }

    gov.va.api.health.r4.api.resources.Location location(String id) {
      return gov.va.api.health.r4.api.resources.Location.builder()
          .resourceType("Location")
          .id(id)
          .address(
              gov.va.api.health.r4.api.datatypes.Address.builder()
                  .line(asList(LOCATION_ADDRESS_STREET))
                  .city(LOCATION_ADDRESS_CITY)
                  .state(LOCATION_ADDRESS_STATE)
                  .postalCode(LOCATION_ADDRESS_POSTAL_CODE)
                  .text(
                      Stream.of(
                              LOCATION_ADDRESS_STREET,
                              LOCATION_ADDRESS_CITY,
                              LOCATION_ADDRESS_STATE,
                              LOCATION_ADDRESS_POSTAL_CODE)
                          .collect(Collectors.joining(" ")))
                  .build())
          .description("BLDG 146, RM W02")
          .managingOrganization(
              gov.va.api.health.r4.api.elements.Reference.builder()
                  .reference("Organization/456")
                  .display("OLIN E. TEAGUE VET CENTER")
                  .build())
          .mode(gov.va.api.health.r4.api.resources.Location.Mode.instance)
          .name(LOCATION_NAME)
          .physicalType(
              gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                  .coding(
                      asList(
                          gov.va.api.health.r4.api.datatypes.Coding.builder()
                              .display("BLDG 146, RM W02")
                              .build()))
                  .build())
          .status(gov.va.api.health.r4.api.resources.Location.Status.active)
          .telecom(
              asList(
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone)
                      .value("254-743-2867")
                      .build()))
          .type(
              asList(
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          asList(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .display("PSYCHIATRY CLINIC")
                                  .build()))
                      .build()))
          .build();
    }
  }
}
