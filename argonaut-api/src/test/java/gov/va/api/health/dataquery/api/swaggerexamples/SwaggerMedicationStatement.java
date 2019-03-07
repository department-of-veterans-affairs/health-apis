package gov.va.api.health.dataquery.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.dataquery.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dataquery.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dataquery.api.bundle.BundleLink;
import gov.va.api.health.dataquery.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Timing;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.MedicationStatement;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerMedicationStatement {
  static final MedicationStatement SWAGGER_EXAMPLE_MEDICATION_STATEMENT =
      MedicationStatement.builder()
          .resourceType("MedicationStatement")
          .id("1f46363d-af9b-5ba5-acda-b384373a9af2")
          .patient(
              Reference.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                  .display("Mr. Aurelio227 Cruickshank494")
                  .build())
          .dateAsserted("2013-04-15T01:15:52Z")
          .status(MedicationStatement.Status.active)
          .medicationReference(
              Reference.builder()
                  .reference(
                      "https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944")
                  .display("Hydrochlorothiazide 25 MG")
                  .build())
          .dosage(
              asList(
                  MedicationStatement.Dosage.builder()
                      .text("Once per day.")
                      .timing(
                          Timing.builder()
                              .code(
                                  CodeableConcept.builder()
                                      .text("As directed by physician.")
                                      .build())
                              .build())
                      .route(CodeableConcept.builder().text("As directed by physician.").build())
                      .build()))
          .build();

  static final MedicationStatement.Bundle SWAGGER_EXAMPLE_MEDICATION_STATEMENT_BUNDLE =
      MedicationStatement.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationStatement?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationStatement?patient=1017283148V813263&page=1&_count=15")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationStatement?patient=1017283148V813263&page=1&_count=15")
                      .build()))
          .entry(
              asList(
                  MedicationStatement.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationStatement/1f46363d-af9b-5ba5-acda-b384373a9af2")
                      .resource(
                          MedicationStatement.builder()
                              .resourceType("MedicationStatement")
                              .id("1f46363d-af9b-5ba5-acda-b384373a9af2")
                              .patient(
                                  Reference.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Patient/2000163")
                                      .display("Mr. Aurelio227 Cruickshank494")
                                      .build())
                              .dateAsserted("2013-04-15T01:15:52Z")
                              .status(MedicationStatement.Status.active)
                              .medicationReference(
                                  Reference.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Medication/7b550d7f-2db8-5002-bc0c-150a70d02944")
                                      .display("Hydrochlorothiazide 25 MG")
                                      .build())
                              .dosage(
                                  asList(
                                      MedicationStatement.Dosage.builder()
                                          .text("Once per day.")
                                          .timing(
                                              Timing.builder()
                                                  .code(
                                                      CodeableConcept.builder()
                                                          .text("As directed by physician.")
                                                          .build())
                                                  .build())
                                          .route(
                                              CodeableConcept.builder()
                                                  .text("As directed by physician.")
                                                  .build())
                                          .build()))
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
