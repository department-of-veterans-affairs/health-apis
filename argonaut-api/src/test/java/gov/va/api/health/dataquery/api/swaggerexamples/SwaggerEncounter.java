package gov.va.api.health.dataquery.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.Encounter;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerEncounter {
  static final Encounter SWAGGER_EXAMPLE_ENCOUNTER =
      Encounter.builder()
          .resourceType("Encounter")
          .id("dc7f6fcc-41f9-5be9-a364-0b0aa938917a")
          .status(Encounter.Status.finished)
          .encounterClass(Encounter.EncounterClass.emergency)
          .patient(
              Reference.builder()
                  .reference("https://www.freedomstream.io/Argonaut/api/Patient/185601V825290")
                  .display("VETERAN,JOHN Q")
                  .build())
          .participant(
              asList(
                  Encounter.Participant.builder()
                      .individual(
                          Reference.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Practitioner/d3f56fac-c7c7-547b-a1e9-de3173d4b628")
                              .display("CARTER,DOC F")
                              .build())
                      .build(),
                  Encounter.Participant.builder()
                      .individual(
                          Reference.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Practitioner/93e4e3c3-8d8c-5b53-996f-6047d0232231")
                              .display("JONES,DOC B")
                              .build())
                      .build(),
                  Encounter.Participant.builder()
                      .individual(
                          Reference.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Practitioner/5e27c469-82e4-5725-babb-49cf7eee948f")
                              .display("CARTER,DOC F")
                              .build())
                      .build(),
                  Encounter.Participant.builder()
                      .individual(
                          Reference.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Practitioner/1b489f04-19e7-5c7d-9140-ce773c158edd")
                              .display("SMITH,DOC A")
                              .build())
                      .build()))
          .appointment(
              Reference.builder()
                  .reference(
                      "https://www.freedomstream.io/Argonaut/api/Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0")
                  .build())
          .indication(
              asList(
                  Reference.builder()
                      .reference(
                          "https://www.freedomstream.io/Argonaut/api/Condition/37d89dc5-45f5-5a2e-9db9-2b17c0d7f318")
                      .display("Chronic asthmatic bronchitis (SNOMED CT 195949008)")
                      .build()))
          .location(
              asList(
                  Encounter.EncounterLocation.builder()
                      .location(
                          Reference.builder()
                              .reference(
                                  "https://www.freedomstream.io/Argonaut/api/Location/eb094a51-ad31-5b6b-b627-96aac4b02b1c")
                              .display("GNV ED")
                              .build())
                      .build()))
          .serviceProvider(
              Reference.builder()
                  .reference(
                      "https://www.freedomstream.io/Argonaut/api/Institution/ed3f9a41-397a-5100-b177-ae3815e5c370")
                  .display("N. FLORIDA/S. GEORGIA HCS")
                  .build())
          .build();

  static final Encounter.Bundle SWAGGER_EXAMPLE_ENCOUNTER_BUNDLE =
      Encounter.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(BundleLink.LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Encounter?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(BundleLink.LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Encounter?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(BundleLink.LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/Encounter?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  Encounter.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/Encounter/dc7f6fcc-41f9-5be9-a364-0b0aa938917a")
                      .resource(
                          Encounter.builder()
                              .resourceType("Encounter")
                              .id("dc7f6fcc-41f9-5be9-a364-0b0aa938917a")
                              .status(Encounter.Status.finished)
                              .encounterClass(Encounter.EncounterClass.emergency)
                              .patient(
                                  Reference.builder()
                                      .reference(
                                          "https://www.freedomstream.io/Argonaut/api/Patient/185601V825290")
                                      .display("VETERAN,JOHN Q")
                                      .build())
                              .participant(
                                  asList(
                                      Encounter.Participant.builder()
                                          .individual(
                                              Reference.builder()
                                                  .reference(
                                                      "https://www.freedomstream.io/Argonaut/api/Practitioner/d3f56fac-c7c7-547b-a1e9-de3173d4b628")
                                                  .display("CARTER,DOC F")
                                                  .build())
                                          .build(),
                                      Encounter.Participant.builder()
                                          .individual(
                                              Reference.builder()
                                                  .reference(
                                                      "https://www.freedomstream.io/Argonaut/api/Practitioner/93e4e3c3-8d8c-5b53-996f-6047d0232231")
                                                  .display("JONES,DOC B")
                                                  .build())
                                          .build(),
                                      Encounter.Participant.builder()
                                          .individual(
                                              Reference.builder()
                                                  .reference(
                                                      "https://www.freedomstream.io/Argonaut/api/Practitioner/5e27c469-82e4-5725-babb-49cf7eee948f")
                                                  .display("CARTER,DOC F")
                                                  .build())
                                          .build(),
                                      Encounter.Participant.builder()
                                          .individual(
                                              Reference.builder()
                                                  .reference(
                                                      "https://www.freedomstream.io/Argonaut/api/Practitioner/1b489f04-19e7-5c7d-9140-ce773c158edd")
                                                  .display("SMITH,DOC A")
                                                  .build())
                                          .build()))
                              .appointment(
                                  Reference.builder()
                                      .reference(
                                          "https://www.freedomstream.io/Argonaut/api/Appointment/615f31df-f0c7-5100-ac42-7fb952c630d0")
                                      .build())
                              .indication(
                                  asList(
                                      Reference.builder()
                                          .reference(
                                              "https://www.freedomstream.io/Argonaut/api/Condition/37d89dc5-45f5-5a2e-9db9-2b17c0d7f318")
                                          .display(
                                              "Chronic asthmatic bronchitis (SNOMED CT 195949008)")
                                          .build()))
                              .location(
                                  asList(
                                      Encounter.EncounterLocation.builder()
                                          .location(
                                              Reference.builder()
                                                  .reference(
                                                      "https://www.freedomstream.io/Argonaut/api/Location/eb094a51-ad31-5b6b-b627-96aac4b02b1c")
                                                  .display("GNV ED")
                                                  .build())
                                          .build()))
                              .serviceProvider(
                                  Reference.builder()
                                      .reference(
                                          "https://www.freedomstream.io/Argonaut/api/Institution/ed3f9a41-397a-5100-b177-ae3815e5c370")
                                      .display("N. FLORIDA/S. GEORGIA HCS")
                                      .build())
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
