package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.stu3.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.stu3.api.bundle.AbstractEntry.Search;
import gov.va.api.health.stu3.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.stu3.api.bundle.BundleLink;
import gov.va.api.health.stu3.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.stu3.api.datatypes.Address;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.resources.Practitioner;
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
public class PractitionerSamples {

  @AllArgsConstructor(staticName = "create")
  static class Datamart {
    public DatamartPractitioner practitioner() {
      return practitioner("1234");
    }

    public DatamartPractitioner practitioner(String cdwId) {
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
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(practitioners.size())
          .link(Arrays.asList(links))
          .entry(
              practitioners
                  .stream()
                  .map(
                      c ->
                          gov.va.api.health.dstu2.api.resources.Practitioner.Entry.builder()
                              .fullUrl(baseUrl + "/Practitioner/" + c.id())
                              .resource(c)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
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
      return practitioner("1234");
    }

    public gov.va.api.health.dstu2.api.resources.Practitioner practitioner(String id) {
      return gov.va.api.health.dstu2.api.resources.Practitioner.builder()
          .resourceType("Practitioner")
          .id(id)
          .active(true)
          .name(HumanName.builder().family(List.of("Joe")).given(List.of("Johnson")).build())
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
              Collections.singletonList(
                  gov.va.api.health.dstu2.api.resources.Practitioner.PractitionerRole.builder()
                      .location(
                          Collections.singletonList(Reference.builder().display("test").build()))
                      .healthcareService(
                          Collections.singletonList(Reference.builder().display("medical").build()))
                      .managingOrganization(Reference.builder().display("test").build())
                      .role(
                          CodeableConcept.builder()
                              .coding(
                                  Collections.singletonList(
                                      Coding.builder()
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
  public static class Stu3 {

    static Practitioner.Bundle asBundle(
        String baseUrl, Collection<Practitioner> practitioners, BundleLink... links) {
      return Practitioner.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(practitioners.size())
          .link(Arrays.asList(links))
          .entry(
              practitioners
                  .stream()
                  .map(
                      c ->
                          Practitioner.Entry.builder()
                              .fullUrl(baseUrl + "/Practitioner/" + c.id())
                              .resource(c)
                              .search(Search.builder().mode(SearchMode.match).build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static BundleLink link(LinkRelation rel, String base, int page, int count) {
      return BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    public gov.va.api.health.stu3.api.resources.Practitioner practitioner() {
      return practitioner("1234");
    }

    public Practitioner practitioner(String id) {
      return Practitioner.builder()
          .resourceType("Practitioner")
          .id(id)
          .identifier(
              Collections.singletonList(
                  Practitioner.PractitionerIdentifier.builder()
                      .system("http://hl7.org/fhir/sid/us-npi")
                      .value("1234567")
                      .build()))
          .active(true)
          .name(
              Practitioner.PractitionerHumanName.builder()
                  .family("Joe")
                  .given(List.of("Johnson"))
                  .build())
          .gender(Practitioner.Gender.male)
          .birthDate("1970-11-14")
          .address(
              List.of(
                  Address.builder()
                      .line(List.of("111 MacGyver Viaduct"))
                      .city("Anchorage")
                      .state("Alaska")
                      .postalCode("99501")
                      .build()))
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .use(ContactPoint.ContactPointUse.mobile)
                      .value("123-456-1234")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build()))
          .build();
    }
  }
}
