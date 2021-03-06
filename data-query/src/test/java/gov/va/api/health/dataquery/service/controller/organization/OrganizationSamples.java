package gov.va.api.health.dataquery.service.controller.organization;

import static java.util.Arrays.asList;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.FacilityId;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

public class OrganizationSamples {

  // Organization Search Params
  public static final String ORGANIZATION_NAME = "NEW AMSTERDAM CBOC";

  public static final String ORGANIZATION_ADDRESS_LINE_ONE = "10 MONROE AVE, SUITE 6B";

  public static final String ORGANIZATION_ADDRESS_LINE_TWO = "PO BOX 4160";

  public static final String ORGANIZATION_ADDRESS_CITY = "NEW AMSTERDAM";

  public static final String ORGANIZATION_ADDRESS_STATE = "OH";

  public static final String ORGANIZATION_ADDRESS_POSTAL_CODE = "44444-4160";

  public static ResourceIdentity id(String cdwId) {
    return ResourceIdentity.builder()
        .system("CDW")
        .resource("ORGANIZATION")
        .identifier(cdwId)
        .build();
  }

  @SneakyThrows
  static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public static Registration registration(String cdwId, String publicId) {
    return Registration.builder().uuid(publicId).resourceIdentities(List.of(id(cdwId))).build();
  }

  @AllArgsConstructor(staticName = "create")
  public static class Datamart {

    public OrganizationEntity entity(String id) {
      return OrganizationEntity.builder()
          .cdwId(id)
          .npi("1205983228")
          .name(ORGANIZATION_NAME)
          .street(ORGANIZATION_ADDRESS_LINE_ONE + " " + ORGANIZATION_ADDRESS_LINE_TWO)
          .city(ORGANIZATION_ADDRESS_CITY)
          .state(ORGANIZATION_ADDRESS_STATE)
          .postalCode(ORGANIZATION_ADDRESS_POSTAL_CODE)
          .facilityType("HEALTH")
          .stationNumber("123")
          .payload(json(organization(id)))
          .build();
    }

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
          .facilityId(
              Optional.of(
                  FacilityId.builder()
                      .type(FacilityId.FacilityType.HEALTH)
                      .stationNumber("123")
                      .build()))
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
                  .line1(ORGANIZATION_ADDRESS_LINE_ONE)
                  .line2(ORGANIZATION_ADDRESS_LINE_TWO)
                  .city(ORGANIZATION_ADDRESS_CITY)
                  .state(ORGANIZATION_ADDRESS_STATE)
                  .postalCode(ORGANIZATION_ADDRESS_POSTAL_CODE)
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
              organizations.stream()
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
          .name(ORGANIZATION_NAME)
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
                      .line(
                          Arrays.asList(
                              ORGANIZATION_ADDRESS_LINE_ONE, ORGANIZATION_ADDRESS_LINE_TWO))
                      .city(ORGANIZATION_ADDRESS_CITY)
                      .state(ORGANIZATION_ADDRESS_STATE)
                      .postalCode(ORGANIZATION_ADDRESS_POSTAL_CODE)
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static class R4 {

    static gov.va.api.health.r4.api.resources.Organization.Bundle asBundle(
        String baseUrl,
        Collection<Organization> organizations,
        int totalRecords,
        BundleLink... links) {
      return gov.va.api.health.r4.api.resources.Organization.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              organizations.stream()
                  .map(
                      c ->
                          gov.va.api.health.r4.api.resources.Organization.Entry.builder()
                              .fullUrl(baseUrl + "/Organization/" + c.id())
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

    public gov.va.api.health.r4.api.resources.Organization organization() {
      return organization("1234");
    }

    gov.va.api.health.r4.api.resources.Organization organization(String id) {
      return Organization.builder()
          .resourceType("Organization")
          .id(id)
          .identifier(
              asList(
                  Identifier.builder()
                      .system("http://hl7.org/fhir/sid/us-npi")
                      .value("1205983228")
                      .build(),
                  Identifier.builder()
                      .use(Identifier.IdentifierUse.usual)
                      .type(
                          CodeableConcept.builder()
                              .text("Facility ID")
                              .coding(
                                  asList(
                                      Coding.builder()
                                          .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                          .code("FI")
                                          .display("Facility ID")
                                          .build()))
                              .build())
                      .system(
                          "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-facility-identifier")
                      .value("vha_123")
                      .build()))
          .active(true)
          .name(ORGANIZATION_NAME)
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
              asList(
                  Address.builder()
                      .line(asList(ORGANIZATION_ADDRESS_LINE_ONE, ORGANIZATION_ADDRESS_LINE_TWO))
                      .text(
                          Stream.of(
                                  ORGANIZATION_ADDRESS_LINE_ONE,
                                  ORGANIZATION_ADDRESS_LINE_TWO,
                                  ORGANIZATION_ADDRESS_CITY,
                                  ORGANIZATION_ADDRESS_STATE,
                                  ORGANIZATION_ADDRESS_POSTAL_CODE)
                              .collect(Collectors.joining(" ")))
                      .city(ORGANIZATION_ADDRESS_CITY)
                      .state(ORGANIZATION_ADDRESS_STATE)
                      .postalCode(ORGANIZATION_ADDRESS_POSTAL_CODE)
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
              organizations.stream()
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
                  gov.va.api.health.stu3.api.datatypes.Identifier.builder()
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
          .name(ORGANIZATION_NAME)
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
                  gov.va.api.health.stu3.api.datatypes.Address.builder()
                      .line(asList(ORGANIZATION_ADDRESS_LINE_ONE, ORGANIZATION_ADDRESS_LINE_TWO))
                      .text(
                          Stream.of(
                                  ORGANIZATION_ADDRESS_LINE_ONE,
                                  ORGANIZATION_ADDRESS_LINE_TWO,
                                  ORGANIZATION_ADDRESS_CITY,
                                  ORGANIZATION_ADDRESS_STATE,
                                  ORGANIZATION_ADDRESS_POSTAL_CODE)
                              .collect(Collectors.joining(" ")))
                      .city(ORGANIZATION_ADDRESS_CITY)
                      .state(ORGANIZATION_ADDRESS_STATE)
                      .postalCode(ORGANIZATION_ADDRESS_POSTAL_CODE)
                      .build()))
          .build();
    }
  }
}
