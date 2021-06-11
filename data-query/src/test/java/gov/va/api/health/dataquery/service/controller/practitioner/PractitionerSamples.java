package gov.va.api.health.dataquery.service.controller.practitioner;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PractitionerSamples {
  public static ResourceIdentity id(String cdwId) {
    return ResourceIdentity.builder()
        .system("CDW")
        .resource("PRACTITIONER")
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
    @SneakyThrows
    public PractitionerEntity entity(String cdwId, String locCdwId, String orgCdwId) {
      DatamartPractitioner dm = practitioner(cdwId);
      return PractitionerEntity.builder()
          .cdwIdNumber(CompositeCdwId.fromCdwId(cdwId).cdwIdNumber())
          .cdwIdResourceCode(CompositeCdwId.fromCdwId(cdwId).cdwIdResourceCode())
          .npi("1234567")
          .familyName("Joe")
          .givenName("Johnson")
          .payload(json(dm))
          .build();
    }

    public DatamartPractitioner practitioner() {
      return practitioner("123:S");
    }

    public DatamartPractitioner practitioner(String cdwId) {
      checkArgument(cdwId.endsWith(":S"));
      return DatamartPractitioner.builder()
          .cdwId(cdwId)
          .active(true)
          .address(
              List.of(
                  DatamartPractitioner.Address.builder()
                      .line1("111 MacGyver Viaduct")
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .name(DatamartPractitioner.Name.builder().family("Joe").given("Johnson").build())
          .birthDate(Optional.of(LocalDate.parse("1970-11-14")))
          .gender(DatamartPractitioner.Gender.male)
          .npi(Optional.of("1234567"))
          .practitionerRole(
              Optional.of(
                  DatamartPractitioner.PractitionerRole.builder()
                      .healthCareService(Optional.of("medical"))
                      .location(
                          singletonList(
                              DatamartReference.builder()
                                  .display(Optional.of("test"))
                                  .type(Optional.of("Location"))
                                  .build()))
                      .managingOrganization(
                          Optional.of(
                              DatamartReference.builder()
                                  .display(Optional.of("test"))
                                  .type(Optional.of("Organization"))
                                  .build()))
                      .role(
                          Optional.of(
                              DatamartCoding.builder()
                                  .system(Optional.of("test"))
                                  .display(Optional.of("test"))
                                  .code(Optional.of("test"))
                                  .build()))
                      .build()))
          .telecom(
              List.of(
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.mobile)
                      .system(DatamartPractitioner.Telecom.System.phone)
                      .value("123-456-1234")
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class Dstu2 {
    static gov.va.api.health.dstu2.api.resources.Practitioner.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.dstu2.api.resources.Practitioner> practitioners,
        gov.va.api.health.dstu2.api.bundle.BundleLink... links) {
      return gov.va.api.health.dstu2.api.resources.Practitioner.Bundle.builder()
          .type(gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType.searchset)
          .total(practitioners.size())
          .link(asList(links))
          .entry(
              practitioners.stream()
                  .map(
                      c ->
                          gov.va.api.health.dstu2.api.resources.Practitioner.Entry.builder()
                              .fullUrl(baseUrl + "/Practitioner/" + c.id())
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

    public static gov.va.api.health.dstu2.api.bundle.BundleLink link(
        gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.dstu2.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public gov.va.api.health.dstu2.api.resources.Practitioner practitioner() {
      return practitioner("123:S");
    }

    public gov.va.api.health.dstu2.api.resources.Practitioner practitioner(String id) {
      return gov.va.api.health.dstu2.api.resources.Practitioner.builder()
          .id(id)
          .active(true)
          .name(
              gov.va.api.health.dstu2.api.datatypes.HumanName.builder()
                  .family(List.of("Joe"))
                  .given(List.of("Johnson"))
                  .build())
          .gender(gov.va.api.health.dstu2.api.resources.Practitioner.Gender.male)
          .birthDate("1970-11-14")
          .address(
              List.of(
                  gov.va.api.health.dstu2.api.datatypes.Address.builder()
                      .line(List.of("111 MacGyver Viaduct"))
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .telecom(
              List.of(
                  gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                      .use(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("123-456-1234")
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .build()))
          .practitionerRole(
              singletonList(
                  gov.va.api.health.dstu2.api.resources.Practitioner.PractitionerRole.builder()
                      .location(
                          singletonList(
                              gov.va.api.health.dstu2.api.elements.Reference.builder()
                                  .display("test")
                                  .build()))
                      .healthcareService(
                          singletonList(
                              gov.va.api.health.dstu2.api.elements.Reference.builder()
                                  .display("medical")
                                  .build()))
                      .managingOrganization(
                          gov.va.api.health.dstu2.api.elements.Reference.builder()
                              .display("test")
                              .build())
                      .role(
                          gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                              .coding(
                                  singletonList(
                                      gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                                          .code("test")
                                          .display("test")
                                          .system("test")
                                          .build()))
                              .build())
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    static gov.va.api.health.r4.api.resources.Practitioner.Bundle asBundle(
        String basePath,
        Collection<gov.va.api.health.r4.api.resources.Practitioner> practitioners,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return asBundle(basePath, practitioners, practitioners.size(), links);
    }

    static gov.va.api.health.r4.api.resources.Practitioner.Bundle asBundle(
        String basePath,
        Collection<gov.va.api.health.r4.api.resources.Practitioner> practitioners,
        int totalRecords,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return gov.va.api.health.r4.api.resources.Practitioner.Bundle.builder()
          .type(gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(asList(links))
          .entry(
              practitioners.stream()
                  .map(
                      c ->
                          gov.va.api.health.r4.api.resources.Practitioner.Entry.builder()
                              .fullUrl(basePath + "/Practitioner/" + c.id())
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

    public gov.va.api.health.r4.api.resources.Practitioner practitioner() {
      return practitioner("123:S");
    }

    public gov.va.api.health.r4.api.resources.Practitioner practitioner(String id) {
      return gov.va.api.health.r4.api.resources.Practitioner.builder()
          .id(id)
          .identifier(null)
          .active(true)
          .name(
              List.of(
                  gov.va.api.health.r4.api.datatypes.HumanName.builder()
                      .family("Joe")
                      .given(List.of("Johnson"))
                      .build()))
          .gender(gov.va.api.health.r4.api.resources.Practitioner.GenderCode.male)
          .birthDate("1970-11-14")
          .identifier(
              List.of(
                  Identifier.builder()
                      .system("http://hl7.org/fhir/sid/us-npi")
                      .value("1234567")
                      .build()))
          .address(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Address.builder()
                      .line(List.of("111 MacGyver Viaduct"))
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .telecom(
              List.of(
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("123-456-1234")
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone)
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class Stu3 {
    static gov.va.api.health.stu3.api.resources.Practitioner.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.stu3.api.resources.Practitioner> practitioners,
        gov.va.api.health.stu3.api.bundle.BundleLink... links) {
      return gov.va.api.health.stu3.api.resources.Practitioner.Bundle.builder()
          .type(gov.va.api.health.stu3.api.bundle.AbstractBundle.BundleType.searchset)
          .total(practitioners.size())
          .link(asList(links))
          .entry(
              practitioners.stream()
                  .map(
                      c ->
                          gov.va.api.health.stu3.api.resources.Practitioner.Entry.builder()
                              .fullUrl(baseUrl + "/Practitioner/" + c.id())
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

    public static gov.va.api.health.stu3.api.bundle.BundleLink link(
        gov.va.api.health.stu3.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.stu3.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public gov.va.api.health.stu3.api.resources.Practitioner practitioner() {
      return practitioner("123:S");
    }

    public gov.va.api.health.stu3.api.resources.Practitioner practitioner(String id) {
      return gov.va.api.health.stu3.api.resources.Practitioner.builder()
          .id(id)
          .identifier(
              singletonList(
                  gov.va.api.health.stu3.api.resources.Practitioner.PractitionerIdentifier.builder()
                      .system("http://hl7.org/fhir/sid/us-npi")
                      .value("1234567")
                      .build()))
          .active(true)
          .name(
              gov.va.api.health.stu3.api.resources.Practitioner.PractitionerHumanName.builder()
                  .family("Joe")
                  .given(List.of("Johnson"))
                  .build())
          .gender(gov.va.api.health.stu3.api.resources.Practitioner.Gender.male)
          .birthDate("1970-11-14")
          .address(
              List.of(
                  gov.va.api.health.stu3.api.datatypes.Address.builder()
                      .line(List.of("111 MacGyver Viaduct"))
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .telecom(
              List.of(
                  gov.va.api.health.stu3.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("123-456-1234")
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .build()))
          .build();
    }
  }
}
