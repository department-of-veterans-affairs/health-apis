package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static java.util.Arrays.asList;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.stu3.api.bundle.AbstractBundle;
import gov.va.api.health.stu3.api.bundle.AbstractEntry;
import gov.va.api.health.stu3.api.bundle.BundleLink;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.datatypes.Period;
import gov.va.api.health.stu3.api.elements.Reference;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PractitionerRoleSamples {
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
  static class Datamart {
    @SneakyThrows
    public PractitionerEntity entity(String cdwId, String locCdwId, String orgCdwId) {
      DatamartPractitioner dm = practitioner(cdwId, locCdwId, orgCdwId);
      return PractitionerEntity.builder()
          .cdwId(cdwId)
          .familyName("Nelson")
          .givenName("Bob")
          .payload(json(dm))
          .build();
    }

    public DatamartPractitioner practitioner(String cdwId, String locCdwId, String orgCdwId) {
      return DatamartPractitioner.builder()
          .cdwId(cdwId)
          .npi(Optional.of("12345"))
          .active(true)
          .name(
              DatamartPractitioner.Name.builder()
                  .family("Nelson")
                  .given("Bob")
                  .prefix(Optional.of("Dr."))
                  .suffix(Optional.of("PhD"))
                  .build())
          .telecom(
              List.of(
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.mobile)
                      .system(DatamartPractitioner.Telecom.System.phone)
                      .value("123-456-7890")
                      .build(),
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.work)
                      .system(DatamartPractitioner.Telecom.System.phone)
                      .value("111-222-3333")
                      .build(),
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.home)
                      .system(DatamartPractitioner.Telecom.System.pager)
                      .value("444-555-6666")
                      .build(),
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.work)
                      .system(DatamartPractitioner.Telecom.System.fax)
                      .value("777-888-9999")
                      .build(),
                  DatamartPractitioner.Telecom.builder()
                      .use(DatamartPractitioner.Telecom.Use.work)
                      .system(DatamartPractitioner.Telecom.System.email)
                      .value("bob.nelson@www.creedthoughts.gov.www/creedthoughts")
                      .build()))
          .address(
              List.of(
                  DatamartPractitioner.Address.builder()
                      .temp(true)
                      .line1("111 MacGyver Viaduct")
                      .line3("Under the bridge")
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .gender(DatamartPractitioner.Gender.male)
          .birthDate(Optional.of(LocalDate.parse("1970-11-14")))
          .practitionerRole(
              Optional.of(
                  DatamartPractitioner.PractitionerRole.builder()
                      .managingOrganization(
                          Optional.of(
                              DatamartReference.builder()
                                  .type(Optional.of("Organization"))
                                  .reference(Optional.of(orgCdwId))
                                  .display(Optional.of("CHEYENNE VA MEDICAL"))
                                  .build()))
                      .role(
                          Optional.of(
                              DatamartCoding.builder()
                                  .system(Optional.of("rpcmm"))
                                  .display(Optional.of("PSYCHOLOGIST"))
                                  .code(Optional.of("37"))
                                  .build()))
                      .specialty(
                          asList(
                              DatamartPractitioner.PractitionerRole.Specialty.builder()
                                  .providerType(Optional.of("Physicians (M.D. and D.O.)"))
                                  .classification(Optional.of("Physician/Osteopath"))
                                  .areaOfSpecialization(Optional.of("Internal Medicine"))
                                  .vaCode(Optional.of("V111500"))
                                  .build(),
                              DatamartPractitioner.PractitionerRole.Specialty.builder()
                                  .providerType(Optional.of("Physicians (M.D. and D.O.)"))
                                  .classification(Optional.of("Physician/Osteopath"))
                                  .areaOfSpecialization(Optional.of("General Practice"))
                                  .vaCode(Optional.of("V111000"))
                                  .specialtyCode(Optional.of("207KI0005X"))
                                  .build(),
                              DatamartPractitioner.PractitionerRole.Specialty.builder()
                                  .providerType(Optional.of("Physicians (M.D. and D.O.)"))
                                  .classification(Optional.of("Physician/Osteopath"))
                                  .areaOfSpecialization(Optional.of("Family Practice"))
                                  .vaCode(Optional.of("V110900"))
                                  .build(),
                              DatamartPractitioner.PractitionerRole.Specialty.builder()
                                  .providerType(Optional.of("Allopathic & Osteopathic Physicians"))
                                  .classification(Optional.of("Family Medicine"))
                                  .vaCode(Optional.of("V180700"))
                                  .x12Code(Optional.of("207Q00000X"))
                                  .build()))
                      .period(
                          Optional.of(
                              DatamartPractitioner.PractitionerRole.Period.builder()
                                  .start(Optional.of(LocalDate.parse("1988-08-19")))
                                  .build()))
                      .location(
                          Collections.singletonList(
                              DatamartReference.builder()
                                  .type(Optional.of("Location"))
                                  .reference(Optional.of(locCdwId))
                                  .display(Optional.of("CHEY MEDICAL"))
                                  .build()))
                      .healthCareService(Optional.of("MEDICAL SERVICE"))
                      .build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class Stu3 {
    static PractitionerRole.Bundle asBundle(
        String baseUrl, Collection<PractitionerRole> roles, BundleLink... links) {
      return PractitionerRole.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(roles.size())
          .link(asList(links))
          .entry(
              roles.stream()
                  .map(
                      c ->
                          PractitionerRole.Entry.builder()
                              .fullUrl(baseUrl + "/PractitionerRole/" + c.id())
                              .resource(c)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static BundleLink link(BundleLink.LinkRelation rel, String base, int page, int count) {
      return BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public PractitionerRole practitionerRole(String pubId, String pubLocId, String pubOrgId) {
      return PractitionerRole.builder()
          .resourceType("PractitionerRole")
          .id(pubId)
          .period(Period.builder().start("1988-08-19").build())
          .practitioner(Reference.builder().reference("Practitioner/" + pubId).build())
          .organization(
              Reference.builder()
                  .reference("Organization/" + pubOrgId)
                  .display("CHEYENNE VA MEDICAL")
                  .build())
          .code(
              List.of(
                  CodeableConcept.builder()
                      .coding(
                          asList(
                              Coding.builder()
                                  .system("rpcmm")
                                  .code("37")
                                  .display("PSYCHOLOGIST")
                                  .build()))
                      .build()))
          .specialty(
              List.of(
                  CodeableConcept.builder()
                      .coding(
                          List.of(
                              Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V111500")
                                  .build()))
                      .build(),
                  CodeableConcept.builder()
                      .coding(
                          List.of(
                              Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V111000")
                                  .build()))
                      .build(),
                  CodeableConcept.builder()
                      .coding(
                          List.of(
                              Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V110900")
                                  .build()))
                      .build(),
                  CodeableConcept.builder()
                      .coding(
                          List.of(
                              Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("207Q00000X")
                                  .build()))
                      .build()))
          .location(
              asList(
                  Reference.builder()
                      .reference("Location/" + pubLocId)
                      .display("CHEY MEDICAL")
                      .build()))
          .healthcareService(asList(Reference.builder().display("MEDICAL SERVICE").build()))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    static gov.va.api.health.r4.api.resources.PractitionerRole.Bundle asBundle(
        String basePath,
        Collection<gov.va.api.health.r4.api.resources.PractitionerRole> records,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return asBundle(basePath, records, records.size(), links);
    }

    static gov.va.api.health.r4.api.resources.PractitionerRole.Bundle asBundle(
        String basePath,
        Collection<gov.va.api.health.r4.api.resources.PractitionerRole> records,
        int totalRecords,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return gov.va.api.health.r4.api.resources.PractitionerRole.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(totalRecords)
          .link(asList(links))
          .entry(
              records.stream()
                  .map(
                      c ->
                          gov.va.api.health.r4.api.resources.PractitionerRole.Entry.builder()
                              .fullUrl(basePath + "/PractitionerRole/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.r4.api.bundle.AbstractEntry.Search.builder()
                                      .mode(SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static gov.va.api.health.r4.api.bundle.BundleLink link(
        gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation relation,
        String base,
        int page,
        int count) {
      return gov.va.api.health.r4.api.bundle.BundleLink.builder()
          .relation(relation)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public gov.va.api.health.r4.api.resources.PractitionerRole practitionerRole(
        String pubId, String pubOrgId, String pubLocId) {
      return gov.va.api.health.r4.api.resources.PractitionerRole.builder()
          .resourceType("PractitionerRole")
          .id(pubId)
          .period(gov.va.api.health.r4.api.datatypes.Period.builder().start("1988-08-19").build())
          .practitioner(
              gov.va.api.health.r4.api.elements.Reference.builder()
                  .reference("Practitioner/" + pubId)
                  .build())
          .active(true)
          .organization(
              gov.va.api.health.r4.api.elements.Reference.builder()
                  .reference("Organization/" + pubOrgId)
                  .display("CHEYENNE VA MEDICAL")
                  .build())
          .code(
              asList(
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          asList(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("rpcmm")
                                  .code("37")
                                  .display("PSYCHOLOGIST")
                                  .build()))
                      .build()))
          .specialty(
              asList(
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          asList(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V111500")
                                  .build()))
                      .build(),
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          asList(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V111000")
                                  .build()))
                      .build(),
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          asList(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V110900")
                                  .build()))
                      .build(),
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          asList(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("207Q00000X")
                                  .build()))
                      .build()))
          .location(
              asList(
                  gov.va.api.health.r4.api.elements.Reference.builder()
                      .reference("Location/" + pubLocId)
                      .display("CHEY MEDICAL")
                      .build()))
          .telecom(
              asList(
                  ContactPoint.builder()
                      .system(ContactPointSystem.phone)
                      .value("123-456-7890")
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPointSystem.phone)
                      .value("111-222-3333")
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPointSystem.pager)
                      .value("444-555-6666")
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPointSystem.fax)
                      .value("777-888-9999")
                      .build(),
                  ContactPoint.builder()
                      .system(ContactPointSystem.email)
                      .value("bob.nelson@www.creedthoughts.gov.www/creedthoughts")
                      .build()))
          .healthcareService(
              asList(
                  gov.va.api.health.r4.api.elements.Reference.builder()
                      .display("MEDICAL SERVICE")
                      .build()))
          .build();
    }
  }
}
