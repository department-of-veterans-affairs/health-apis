package gov.va.api.health.argonaut.api.swaggerexamples;

import static java.util.Arrays.asList;

import gov.va.api.health.argonaut.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.argonaut.api.bundle.AbstractEntry.Search;
import gov.va.api.health.argonaut.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.argonaut.api.datatypes.CodeableConcept;
import gov.va.api.health.argonaut.api.datatypes.Coding;
import gov.va.api.health.argonaut.api.datatypes.Identifier;
import gov.va.api.health.argonaut.api.datatypes.SimpleQuantity;
import gov.va.api.health.argonaut.api.datatypes.Timing;
import gov.va.api.health.argonaut.api.elements.Reference;
import gov.va.api.health.argonaut.api.resources.MedicationDispense;
import lombok.experimental.UtilityClass;

@UtilityClass
class SwaggerMedicationDispense {
  static final MedicationDispense SWAGGER_EXAMPLE_MEDICATION_DISPENSE =
      MedicationDispense.builder()
          .resourceType("MedicationDispense")
          .id("3ba8c63c-bac2-5f98-b7cd-161792919216")
          .identifier(
              Identifier.builder()
                  .use(Identifier.IdentifierUse.usual)
                  .system("http://va.gov/cdw")
                  .value("185601V825290")
                  .build())
          .status(MedicationDispense.Status.completed)
          .patient(
              Reference.builder()
                  .reference("https://dev-api.va.gov/services/argonaut/v0/Patient/185601V825290")
                  .display("VETERAN,JOHN Q")
                  .build())
          .dispenser(
              Reference.builder()
                  .reference(
                      "https://dev-api.va.gov/services/argonaut/v0/Practitioner/a98d6c9c-c5bd-58a5-a5d0-0da31cafd37c")
                  .display("SMITH,ATTENDING C")
                  .build())
          .authorizingPrescription(
              asList(
                  Reference.builder()
                      .reference(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/8c95153c-817b-53f3-8166-55b1e01ac2a2")
                      .display("OUTPATIENT PHARMACY")
                      .build()))
          .type(
              CodeableConcept.builder()
                  .coding(
                      asList(
                          Coding.builder()
                              .system("http://hl7.org/fhir/v3/ActCode")
                              .code("FF")
                              .display("First Fill")
                              .build()))
                  .build())
          .quantity(SimpleQuantity.builder().value(240D).unit("ML").build())
          .daysSupply(
              SimpleQuantity.builder()
                  .value(15D)
                  .unit("Day")
                  .system("http://unitsofmeasure.org")
                  .code("D")
                  .build())
          .medicationReference(
              Reference.builder()
                  .reference(
                      "https://dev-api.va.gov/services/argonaut/v0/Medication/b07873cb-24cb-5aa7-a0be-78b7ac5aee22")
                  .display("CODEINE 10/GG 100MG/5ML (ALC-F/SF) LIQ")
                  .build())
          .whenPrepared("2015-04-15T04:00:00Z")
          .dosageInstruction(
              asList(
                  MedicationDispense.DosageInstruction.builder()
                      .text(
                          "TAKE 1 TEASPOONFUL BY MOUTH EVERY 6 HOURS AS NEEDED FOR COUGH CAN TAKE  2 TEASPOONS AT BEDTIME IF NEEDED. USE SPARINGLY")
                      .additionalInstructions(
                          CodeableConcept.builder()
                              .text(
                                  "FOR COUGH can take 2 teaspoons at bedtime if needed. Use sparingly")
                              .build())
                      .timing(
                          Timing.builder()
                              .code(CodeableConcept.builder().text("PRN").build())
                              .build())
                      .asNeededBoolean(true)
                      .route(CodeableConcept.builder().text("ORAL").build())
                      .doseQuantity(SimpleQuantity.builder().value(1D).build())
                      .build()))
          .build();

  static final MedicationDispense.Bundle SWAGGER_EXAMPLE_MEDICATION_DISPENSE_BUNDLE =
      MedicationDispense.Bundle.builder()
          .resourceType("Bundle")
          .type(BundleType.searchset)
          .total(1155)
          .link(
              asList(
                  BundleLink.builder()
                      .relation(LinkRelation.first)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=1&_count=1")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.self)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=1&_count=1")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.next)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=2&_count=1")
                      .build(),
                  BundleLink.builder()
                      .relation(LinkRelation.last)
                      .url(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense?patient=185601V825290&page=1155&_count=1")
                      .build()))
          .entry(
              asList(
                  MedicationDispense.Entry.builder()
                      .fullUrl(
                          "https://dev-api.va.gov/services/argonaut/v0/MedicationDispense/2f587c16-5182-59a5-bdcb-518c7c501f37")
                      .resource(
                          MedicationDispense.builder()
                              .resourceType("MedicationDispense")
                              .id("2f587c16-5182-59a5-bdcb-518c7c501f37")
                              .identifier(
                                  Identifier.builder()
                                      .use(Identifier.IdentifierUse.usual)
                                      .system("http://va.gov/cdw")
                                      .value("185601V825290")
                                      .build())
                              .status(MedicationDispense.Status.completed)
                              .patient(
                                  Reference.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Patient/185601V825290")
                                      .display("VETERAN,JOHN Q")
                                      .build())
                              .dispenser(
                                  Reference.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Practitioner/f3276418-063d-52d8-a7f9-5fcaadc2f22b")
                                      .display("BONES,ATTENDING C")
                                      .build())
                              .authorizingPrescription(
                                  asList(
                                      Reference.builder()
                                          .reference(
                                              "https://dev-api.va.gov/services/argonaut/v0/MedicationOrder/e4638ac7-7af1-587c-8f93-811abf8158c7")
                                          .display("OUTPATIENT PHARMACY")
                                          .build()))
                              .type(
                                  CodeableConcept.builder()
                                      .coding(
                                          asList(
                                              Coding.builder()
                                                  .system("http://hl7.org/fhir/v3/ActCode")
                                                  .code("FF")
                                                  .display("First Fill")
                                                  .build()))
                                      .build())
                              .quantity(SimpleQuantity.builder().value(2D).unit("EA").build())
                              .daysSupply(
                                  SimpleQuantity.builder()
                                      .value(30D)
                                      .unit("Day")
                                      .system("http://unitsofmeasure.org")
                                      .code("D")
                                      .build())
                              .medicationReference(
                                  Reference.builder()
                                      .reference(
                                          "https://dev-api.va.gov/services/argonaut/v0/Medication/8ab05080-af33-5724-a090-e9f76386ce30")
                                      .display("ALBUTEROL 90MCG (CFC-F) 200D ORAL INHL")
                                      .build())
                              .whenPrepared("2015-04-15T04:00:00Z")
                              .dosageInstruction(
                                  asList(
                                      MedicationDispense.DosageInstruction.builder()
                                          .text(
                                              "INHALE 2 PUFFS BY MOUTH EVERY 4 HOURS AS NEEDED FOR SHORTNESS OF  BREATH")
                                          .timing(
                                              Timing.builder()
                                                  .code(
                                                      CodeableConcept.builder()
                                                          .text("Q4H PRN")
                                                          .build())
                                                  .build())
                                          .asNeededBoolean(true)
                                          .route(
                                              CodeableConcept.builder()
                                                  .text("INHALATION ORAL")
                                                  .build())
                                          .doseQuantity(SimpleQuantity.builder().value(2D).build())
                                          .build()))
                              .build())
                      .search(Search.builder().mode(SearchMode.match).build())
                      .build()))
          .build();
}
