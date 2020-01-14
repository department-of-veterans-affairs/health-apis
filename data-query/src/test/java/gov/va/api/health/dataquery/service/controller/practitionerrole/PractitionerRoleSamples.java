package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.stu3.api.bundle.AbstractBundle;
import gov.va.api.health.stu3.api.bundle.AbstractEntry;
import gov.va.api.health.stu3.api.bundle.BundleLink;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.datatypes.Period;
import gov.va.api.health.stu3.api.elements.Reference;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PractitionerRoleSamples {
  @AllArgsConstructor(staticName = "create")
  static class Datamart {
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
              roles
                  .stream()
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
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("rpcmm")
                              .code("37")
                              .display("PSYCHOLOGIST")
                              .build()))
                  .build())
          .specialty(
              CodeableConcept.builder()
                  .coding(
                      List.of(
                          Coding.builder()
                              .system("http://nucc.org/provider-taxonomy")
                              .code("207Q00000X")
                              .build()))
                  .build())
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
}
