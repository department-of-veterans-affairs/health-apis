package gov.va.api.health.argonaut.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.argonaut.api.bundle.AbstractEntry.Search;
import gov.va.api.health.argonaut.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.argonaut.api.elements.Extension;
import gov.va.api.health.argonaut.api.elements.Reference;
import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerMedicationOrder {
  static final MedicationOrder SWAGGER_EXAMPLE_MEDICATION_ORDER =
      MedicationOrder.builder()
          .resourceType("MedicationOrder")
          .id("f07dd74e-844e-5463-99d4-0ca4d5cbeb41")
          .dateWritten("2013-04-14T06:00:00Z")
          .status(MedicationOrder.Status.active)
          .patient(
              Reference.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          ._prescriber(
              Extension.builder()
                  .extension(
                      asList(
                          Extension.builder()
                              .url("http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                              .valueCode("unsupported")
                              .build()))
                  .build())
          .medicationReference(
              Reference.builder()
                  .reference(
                      "https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944")
                  .display("Hydrochlorothiazide 25 MG")
                  .build())
          .build();

  static final MedicationOrder.Bundle SWAGGER_EXAMPLE_MEDICATION_ORDER_BUNDLE =
      MedicationOrder.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  MedicationOrder.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/f07dd74e-844e-5463-99d4-0ca4d5cbeb41")
                      .resource(
                          MedicationOrder.builder()
                              .resourceType("MedicationOrder")
                              .id("f07dd74e-844e-5463-99d4-0ca4d5cbeb41")
                              .dateWritten("2013-04-14T06:00:00Z")
                              .status(MedicationOrder.Status.active)
                              .patient(
                                  Reference.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                      .display("Mr. Aurelio227 Cruickshank494")
                                      .build())
                              ._prescriber(
                                  Extension.builder()
                                      .extension(
                                          asList(
                                              Extension.builder()
                                                  .url(
                                                      "http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                                                  .valueCode("unsupported")
                                                  .build()))
                                      .build())
                              .medicationReference(
                                  Reference.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944")
                                      .display("Hydrochlorothiazide 25 MG")
                                      .build())
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
