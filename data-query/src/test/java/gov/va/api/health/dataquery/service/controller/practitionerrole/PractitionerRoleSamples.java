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
import gov.va.api.health.stu3.api.elements.Reference;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import java.time.LocalDate;
import java.util.Arrays;
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
                          Collections.singletonList(
                              DatamartReference.builder()
                                  .type(Optional.of("Location"))
                                  .reference(Optional.of(locCdwId))
                                  .display(Optional.of("test location"))
                                  .build()))
                      .managingOrganization(
                          Optional.of(
                              DatamartReference.builder()
                                  .type(Optional.of("Organization"))
                                  .reference(Optional.of(orgCdwId))
                                  .display(Optional.of("test organization"))
                                  .build()))
                      .role(
                          Optional.of(
                              DatamartCoding.builder()
                                  .system(Optional.of("test system"))
                                  .display(Optional.of("test role"))
                                  .code(Optional.of("test code"))
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
          .practitioner(Reference.builder().reference("Practitioner/" + pubId).build())
          .organization(
              Reference.builder()
                  .reference("Organization/" + pubOrgId)
                  .display("test organization")
                  .build())
          .code(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("test system")
                              .code("test code")
                              .display("test role")
                              .build()))
                  .build())
          .location(
              asList(Reference.builder().reference("Location/" + pubLocId).display("test location").build()))
          .healthcareService(asList(Reference.builder().display("medical").build()))
          .build();
    }
  }
}
