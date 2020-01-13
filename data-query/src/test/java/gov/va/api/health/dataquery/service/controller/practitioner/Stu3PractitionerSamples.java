package gov.va.api.health.dataquery.service.controller.practitioner;

import gov.va.api.health.stu3.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.stu3.api.bundle.AbstractEntry.Search;
import gov.va.api.health.stu3.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.stu3.api.bundle.BundleLink;
import gov.va.api.health.stu3.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.stu3.api.datatypes.Address;
import gov.va.api.health.stu3.api.datatypes.ContactPoint;
import gov.va.api.health.stu3.api.resources.Practitioner;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Stu3PractitionerSamples {

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
