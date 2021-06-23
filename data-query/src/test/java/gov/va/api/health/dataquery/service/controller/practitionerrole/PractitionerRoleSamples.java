package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PractitionerRoleSamples {
  @SneakyThrows
  private static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @AllArgsConstructor(staticName = "create")
  public static final class Datamart {
    public PractitionerRoleEntity entity(
        String cdwId, String pracCdwId, String orgCdwId, String locCdwId) {
      DatamartPractitionerRole dm = practitionerRole(cdwId, pracCdwId, orgCdwId, locCdwId);
      String name = dm.practitioner().get().display().get();
      return PractitionerRoleEntity.builder()
          .cdwIdNumber(CompositeCdwId.fromCdwId(cdwId).cdwIdNumber())
          .cdwIdResourceCode(CompositeCdwId.fromCdwId(cdwId).cdwIdResourceCode())
          .practitionerIdNumber(CompositeCdwId.fromCdwId(pracCdwId).cdwIdNumber())
          .practitionerResourceCode(CompositeCdwId.fromCdwId(pracCdwId).cdwIdResourceCode())
          .givenName(name.substring(name.indexOf(",") + 1))
          .familyName(name.substring(0, name.indexOf(",")))
          .npi("1234567890")
          .active(true)
          .lastUpdated(Instant.now())
          .payload(json(dm))
          .build();
    }

    public DatamartPractitionerRole practitionerRole() {
      return practitionerRole("111:P", "222:S", "333:I", "444:L");
    }

    public DatamartPractitionerRole practitionerRole(
        String cdwId, String pracCdwId, String orgCdwId, String locCdwId) {
      checkArgument(cdwId.endsWith(":P"));
      checkArgument(pracCdwId.endsWith(":S"));
      checkArgument(orgCdwId.endsWith(":I"));
      checkArgument(locCdwId.endsWith(":L"));
      return DatamartPractitionerRole.builder()
          .cdwId(cdwId)
          .managingOrganization(
              Optional.of(
                  DatamartReference.builder()
                      .type(Optional.of("Organization"))
                      .reference(Optional.of(orgCdwId))
                      .display(Optional.of("SOME VA MEDICAL CENTER"))
                      .build()))
          .practitioner(
              Optional.of(
                  DatamartReference.builder()
                      .type(Optional.of("Practitioner"))
                      .reference(Optional.of(pracCdwId))
                      .display(Optional.of("NELSON,BOB"))
                      .build()))
          .role(
              List.of(
                  DatamartCoding.builder()
                      .system(Optional.of("rpcmm"))
                      .code(Optional.of("1"))
                      .display(Optional.of("OPTOMETRIST"))
                      .build()))
          .specialty(
              List.of(
                  DatamartPractitionerRole.Specialty.builder()
                      .providerType(Optional.of("Physicians (M.D. and D.O.)"))
                      .classification(Optional.of("Physician/Osteopath"))
                      .areaOfSpecialization(Optional.of("Internal Medicine"))
                      .vaCode(Optional.of("V111500"))
                      .build(),
                  DatamartPractitionerRole.Specialty.builder()
                      .providerType(Optional.of("Physicians (M.D. and D.O.)"))
                      .classification(Optional.of("Physician/Osteopath"))
                      .areaOfSpecialization(Optional.of("General Practice"))
                      .vaCode(Optional.of("V111000"))
                      .build(),
                  DatamartPractitionerRole.Specialty.builder()
                      .providerType(Optional.of("Physicians (M.D. and D.O.)"))
                      .classification(Optional.of("Physician/Osteopath"))
                      .areaOfSpecialization(Optional.of("Family Practice"))
                      .vaCode(Optional.of("V110900"))
                      .build(),
                  DatamartPractitionerRole.Specialty.builder()
                      .providerType(Optional.of("Allopathic & Osteopathic Physicians"))
                      .classification(Optional.of("Family Medicine"))
                      .vaCode(Optional.of("V180700"))
                      .x12Code(Optional.of("207Q00000X"))
                      .build()))
          .location(
              List.of(
                  DatamartReference.builder()
                      .type(Optional.of("Location"))
                      .reference(Optional.of(locCdwId))
                      .display(
                          Optional.of(
                              "VISUAL IMPAIRMENT SERVICES OUTPATIENT REHABILITATION (VISOR)"))
                      .build()))
          .healthCareService(Optional.of("MEDICAL SERVICE"))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class Stu3 {
    static gov.va.api.health.stu3.api.resources.PractitionerRole.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.stu3.api.resources.PractitionerRole> roles,
        gov.va.api.health.stu3.api.bundle.BundleLink... links) {
      return gov.va.api.health.stu3.api.resources.PractitionerRole.Bundle.builder()
          .type(gov.va.api.health.stu3.api.bundle.AbstractBundle.BundleType.searchset)
          .total(roles.size())
          .link(List.of(links))
          .entry(
              roles.stream()
                  .map(
                      c ->
                          gov.va.api.health.stu3.api.resources.PractitionerRole.Entry.builder()
                              .fullUrl(baseUrl + "/PractitionerRole/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.stu3.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.stu3.api.bundle.AbstractEntry.SearchMode
                                              .match)
                                      .build())
                              .build())
                  .collect(toList()))
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

    public gov.va.api.health.stu3.api.resources.PractitionerRole practitionerRole(
        String pubId, String pracPubId, String orgPubId, String locPubId) {
      return gov.va.api.health.stu3.api.resources.PractitionerRole.builder()
          .id(pubId)
          .practitioner(
              gov.va.api.health.stu3.api.elements.Reference.builder()
                  .reference("Practitioner/" + pracPubId)
                  .display("NELSON,BOB")
                  .build())
          .organization(
              gov.va.api.health.stu3.api.elements.Reference.builder()
                  .reference("Organization/" + orgPubId)
                  .display("SOME VA MEDICAL CENTER")
                  .build())
          .code(
              List.of(
                  gov.va.api.health.stu3.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.stu3.api.datatypes.Coding.builder()
                                  .system("rpcmm")
                                  .code("1")
                                  .display("OPTOMETRIST")
                                  .build()))
                      .build()))
          .specialty(
              List.of(
                  gov.va.api.health.stu3.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.stu3.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V111500")
                                  .build()))
                      .build(),
                  gov.va.api.health.stu3.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.stu3.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V111000")
                                  .build()))
                      .build(),
                  gov.va.api.health.stu3.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.stu3.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V110900")
                                  .build()))
                      .build(),
                  gov.va.api.health.stu3.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.stu3.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("207Q00000X")
                                  .build()))
                      .build()))
          .location(
              List.of(
                  gov.va.api.health.stu3.api.elements.Reference.builder()
                      .reference("Location/" + locPubId)
                      .display("VISUAL IMPAIRMENT SERVICES OUTPATIENT REHABILITATION (VISOR)")
                      .build()))
          .healthcareService(
              List.of(
                  gov.va.api.health.stu3.api.elements.Reference.builder()
                      .display("MEDICAL SERVICE")
                      .build()))
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
          .type(gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(List.of(links))
          .entry(
              records.stream()
                  .map(
                      c ->
                          gov.va.api.health.r4.api.resources.PractitionerRole.Entry.builder()
                              .fullUrl(basePath + "/PractitionerRole/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.r4.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode
                                              .match)
                                      .build())
                              .build())
                  .collect(toList()))
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
        String pubId, String pracPubId, String orgPubId, String locPubId) {
      return gov.va.api.health.r4.api.resources.PractitionerRole.builder()
          .id(pubId)
          .practitioner(
              gov.va.api.health.r4.api.elements.Reference.builder()
                  .reference("Practitioner/" + pracPubId)
                  .build())
          .active(true)
          .organization(
              gov.va.api.health.r4.api.elements.Reference.builder()
                  .reference("Organization/" + orgPubId)
                  .display("SOME VA MEDICAL CENTER")
                  .build())
          .code(
              List.of(
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("rpcmm")
                                  .code("1")
                                  .display("OPTOMETRIST")
                                  .build()))
                      .build()))
          .specialty(
              List.of(
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V111500")
                                  .build()))
                      .build(),
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V111000")
                                  .build()))
                      .build(),
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("V110900")
                                  .build()))
                      .build(),
                  gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
                      .coding(
                          List.of(
                              gov.va.api.health.r4.api.datatypes.Coding.builder()
                                  .system("http://nucc.org/provider-taxonomy")
                                  .code("207Q00000X")
                                  .build()))
                      .build()))
          .location(
              List.of(
                  gov.va.api.health.r4.api.elements.Reference.builder()
                      .reference("Location/" + locPubId)
                      .display("VISUAL IMPAIRMENT SERVICES OUTPATIENT REHABILITATION (VISOR)")
                      .build()))
          .telecom(
              List.of(
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone)
                      .value("123-456-7890")
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone)
                      .value("111-222-3333")
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.pager)
                      .value("444-555-6666")
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.fax)
                      .value("777-888-9999")
                      .build(),
                  gov.va.api.health.r4.api.datatypes.ContactPoint.builder()
                      .system(
                          gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.email)
                      .value("bob.nelson@www.creedthoughts.gov.www/creedthoughts")
                      .build()))
          .healthcareService(
              List.of(
                  gov.va.api.health.r4.api.elements.Reference.builder()
                      .display("MEDICAL SERVICE")
                      .build()))
          .build();
    }
  }
}
