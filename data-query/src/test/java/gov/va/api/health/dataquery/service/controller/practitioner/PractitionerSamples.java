package gov.va.api.health.dataquery.service.controller.practitioner;

import static com.google.common.base.Preconditions.checkArgument;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
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
    public PractitionerEntity entity(String cdwId, String orgCdwId, String locCdwId) {
      DatamartPractitioner dm = practitioner(cdwId, orgCdwId, locCdwId);
      return PractitionerEntity.builder()
          .cdwIdNumber(CompositeCdwId.fromCdwId(cdwId).cdwIdNumber())
          .cdwIdResourceCode(CompositeCdwId.fromCdwId(cdwId).cdwIdResourceCode())
          .npi(dm.npi().get())
          .familyName(dm.name().family())
          .givenName(dm.name().given())
          .payload(json(dm))
          .build();
    }

    public DatamartPractitioner practitioner() {
      return practitioner("111:S", "222:I", "333:L");
    }

    public DatamartPractitioner practitioner(String cdwId, String orgCdwId, String locCdwId) {
      checkArgument(cdwId.endsWith(":S"));
      checkArgument(orgCdwId.endsWith(":I"));
      checkArgument(locCdwId.endsWith(":L"));
      return DatamartPractitioner.builder()
          .cdwId(cdwId)
          .npi(Optional.of("1234567890"))
          .active(true)
          .name(DatamartPractitioner.Name.builder().family("NELSON").given("BOB").build())
          .telecom(
              List.of(
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.mobile)
                      .system(DatamartPractitioner.Telecom.System.phone)
                      .value("123-456-7890")
                      .build(),
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.mobile)
                      .system(DatamartPractitioner.Telecom.System.phone)
                      .value("111-222-3333")
                      .build(),
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.mobile)
                      .system(DatamartPractitioner.Telecom.System.pager)
                      .value("444-555-6666")
                      .build(),
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.mobile)
                      .system(DatamartPractitioner.Telecom.System.fax)
                      .value("777-888-9999")
                      .build(),
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.mobile)
                      .system(DatamartPractitioner.Telecom.System.email)
                      .value("bob.nelson@www.creedthoughts.gov.www/creedthoughts")
                      .build()))
          .address(
              List.of(
                  DatamartPractitioner.Address.builder()
                      .line1("111 MacGyver Viaduct")
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .gender(DatamartPractitioner.Gender.male)
          .birthDate(Optional.of(LocalDate.parse("1970-11-14")))
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
          .link(List.of(links))
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
      return practitioner("111:S", "222:I", "333:L");
    }

    public gov.va.api.health.dstu2.api.resources.Practitioner practitioner(
        String id, String orgId, String locId) {
      return gov.va.api.health.dstu2.api.resources.Practitioner.builder()
          .id(id)
          .active(true)
          .name(
              gov.va.api.health.dstu2.api.datatypes.HumanName.builder()
                  .family(List.of("NELSON"))
                  .given(List.of("BOB"))
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
                      .value("123-456-7890")
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .build(),
                  gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                      .use(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("111-222-3333")
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .build(),
                  gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                      .use(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("444-555-6666")
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem
                              .pager)
                      .build(),
                  gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                      .use(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("777-888-9999")
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem.fax)
                      .build(),
                  gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                      .use(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("bob.nelson@www.creedthoughts.gov.www/creedthoughts")
                      .system(
                          gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem
                              .email)
                      .build()))
          .practitionerRole(
              List.of(
                  gov.va.api.health.dstu2.api.resources.Practitioner.PractitionerRole.builder()
                      .location(
                          List.of(
                              gov.va.api.health.dstu2.api.elements.Reference.builder()
                                  .reference("Location/" + locId)
                                  .display(
                                      "VISUAL IMPAIRMENT SERVICES OUTPATIENT REHABILITATION (VISOR)")
                                  .build()))
                      .healthcareService(
                          List.of(
                              gov.va.api.health.dstu2.api.elements.Reference.builder()
                                  .display("MEDICAL SERVICE")
                                  .build()))
                      .managingOrganization(
                          gov.va.api.health.dstu2.api.elements.Reference.builder()
                              .reference("Organization/" + orgId)
                              .display("SOME VA MEDICAL CENTER")
                              .build())
                      .role(
                          gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                              .coding(
                                  List.of(
                                      gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                                          .code("1")
                                          .display("OPTOMETRIST")
                                          .system("rpcmm")
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
          .link(List.of(links))
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
          .active(true)
          .name(
              List.of(
                  gov.va.api.health.r4.api.datatypes.HumanName.builder()
                      .family("NELSON")
                      .given(List.of("BOB"))
                      .build()))
          .gender(gov.va.api.health.r4.api.resources.Practitioner.GenderCode.male)
          .birthDate("1970-11-14")
          .identifier(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Identifier.builder()
                      .system("http://hl7.org/fhir/sid/us-npi")
                      .value("1234567890")
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
                      .value("123-456-7890")
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone)
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("111-222-3333")
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone)
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("444-555-6666")
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.pager)
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("777-888-9999")
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.fax)
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("bob.nelson@www.creedthoughts.gov.www/creedthoughts")
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.email)
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
          .link(List.of(links))
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
              List.of(
                  gov.va.api.health.stu3.api.datatypes.Identifier.builder()
                      .system("http://hl7.org/fhir/sid/us-npi")
                      .value("1234567890")
                      .build()))
          .active(true)
          .name(
              List.of(
                  gov.va.api.health.stu3.api.datatypes.HumanName.builder()
                      .family("NELSON")
                      .given(List.of("BOB"))
                      .build()))
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
                      .value("123-456-7890")
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .build(),
                  gov.va.api.health.stu3.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("111-222-3333")
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem
                              .phone)
                      .build(),
                  gov.va.api.health.stu3.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("444-555-6666")
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem
                              .pager)
                      .build(),
                  gov.va.api.health.stu3.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("777-888-9999")
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem.fax)
                      .build(),
                  gov.va.api.health.stu3.api.datatypes.ContactPoint.builder()
                      .use(gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointUse.mobile)
                      .value("bob.nelson@www.creedthoughts.gov.www/creedthoughts")
                      .system(
                          gov.va.api.health.stu3.api.datatypes.ContactPoint.ContactPointSystem
                              .email)
                      .build()))
          .build();
    }
  }
}
