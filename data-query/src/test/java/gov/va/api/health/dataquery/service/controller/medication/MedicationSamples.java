package gov.va.api.health.dataquery.service.controller.medication;

import gov.va.api.health.argonaut.api.resources.Medication;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MedicationSamples {
  @AllArgsConstructor(staticName = "create")
  public static class Datamart {
    public DatamartMedication medication() {
      return medication("1000");
    }

    DatamartMedication medication(String cdwId) {
      return DatamartMedication.builder()
          .objectType("Medication")
          .objectVersion("1")
          .cdwId(cdwId)
          .localDrugName("Axert")
          .rxnorm(rxNorm())
          .product(product())
          .build();
    }

    Optional<DatamartMedication.Product> product() {
      return Optional.of(
          DatamartMedication.Product.builder().id("4015523").formText("TAB").build());
    }

    Optional<DatamartMedication.RxNorm> rxNorm() {
      return Optional.of(
          DatamartMedication.RxNorm.builder()
              .code("284205")
              .text("ALMOTRIPTAN MALATE 12.5MG TAB,UD")
              .build());
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class Dstu2 {
    static gov.va.api.health.argonaut.api.resources.Medication.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.argonaut.api.resources.Medication> medications,
        int totalRecords,
        gov.va.api.health.dstu2.api.bundle.BundleLink... links) {
      return Medication.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              medications.stream()
                  .map(
                      c ->
                          gov.va.api.health.argonaut.api.resources.Medication.Entry.builder()
                              .fullUrl(baseUrl + "/Medication/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.dstu2.api.bundle.AbstractEntry
                                              .SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static gov.va.api.health.dstu2.api.bundle.BundleLink link(
        gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.dstu2.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    gov.va.api.health.dstu2.api.datatypes.CodeableConcept codeLocalDrugNameOnly(
        String localDrugName) {
      return gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
          .text(localDrugName)
          .build();
    }

    gov.va.api.health.dstu2.api.datatypes.CodeableConcept codeLocalDrugNameWithProduct(
        String localDrugName) {
      return gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
          .text(localDrugName)
          .coding(
              List.of(
                  gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                      .code( // product.id
                          "4015523")
                      .display(localDrugName)
                      .system("urn:oid:2.16.840.1.113883.6.233")
                      .build()))
          .build();
    }

    gov.va.api.health.dstu2.api.datatypes.CodeableConcept codeRxNorm() {
      return gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
          .coding(
              List.of(
                  gov.va.api.health.dstu2.api.datatypes.Coding.builder()
                      .system("https://www.nlm.nih.gov/research/umls/rxnorm")
                      .code("284205")
                      .display("ALMOTRIPTAN MALATE 12.5MG TAB,UD")
                      .build()))
          .text("ALMOTRIPTAN MALATE 12.5MG TAB,UD")
          .build();
    }

    gov.va.api.health.argonaut.api.resources.Medication medication() {
      return medication("1000");
    }

    gov.va.api.health.argonaut.api.resources.Medication medication(String id) {
      return gov.va.api.health.argonaut.api.resources.Medication.builder()
          .resourceType(gov.va.api.health.argonaut.api.resources.Medication.class.getSimpleName())
          .id(id)
          .code(codeRxNorm())
          .product(product())
          .text(textRxNorm())
          .build();
    }

    gov.va.api.health.argonaut.api.resources.Medication.Product product() {
      return gov.va.api.health.argonaut.api.resources.Medication.Product.builder()
          .id("4015523")
          .form(gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder().text("TAB").build())
          .build();
    }

    gov.va.api.health.dstu2.api.elements.Narrative textLocalDrugName() {
      return gov.va.api.health.dstu2.api.elements.Narrative.builder()
          .status(gov.va.api.health.dstu2.api.elements.Narrative.NarrativeStatus.additional)
          .div("<div>Axert</div>")
          .build();
    }

    gov.va.api.health.dstu2.api.elements.Narrative textRxNorm() {
      return gov.va.api.health.dstu2.api.elements.Narrative.builder()
          .status(gov.va.api.health.dstu2.api.elements.Narrative.NarrativeStatus.additional)
          .div("<div>ALMOTRIPTAN MALATE 12.5MG TAB,UD</div>")
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class R4 {
    static gov.va.api.health.uscorer4.api.resources.Medication.Bundle asBundle(
        String baseUrl,
        Collection<gov.va.api.health.uscorer4.api.resources.Medication> resources,
        int totalRecords,
        gov.va.api.health.r4.api.bundle.BundleLink... links) {
      return gov.va.api.health.uscorer4.api.resources.Medication.Bundle.builder()
          .resourceType("Bundle")
          .type(gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources.stream()
                  .map(
                      c ->
                          gov.va.api.health.uscorer4.api.resources.Medication.Entry.builder()
                              .fullUrl(baseUrl + "/Medication/" + c.id())
                              .resource(c)
                              .search(
                                  gov.va.api.health.r4.api.bundle.AbstractEntry.Search.builder()
                                      .mode(
                                          gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode
                                              .match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    static gov.va.api.health.r4.api.bundle.BundleLink link(
        gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation rel,
        String base,
        int page,
        int count) {
      return gov.va.api.health.r4.api.bundle.BundleLink.builder()
          .relation(rel)
          .url(base + "&page=" + page + "&_count=" + count)
          .build();
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept codeLocalDrugNameOnly(String localDrugName) {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text(localDrugName)
          .build();
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept codeLocalDrugNameWithProduct(
        String localDrugName) {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text(localDrugName)
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .code("4015523")
                      .display(localDrugName)
                      .system("urn:oid:2.16.840.1.113883.6.233")
                      .build()))
          .build();
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept codeRxNorm() {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .system("https://www.nlm.nih.gov/research/umls/rxnorm")
                      .code("284205")
                      .display("ALMOTRIPTAN MALATE 12.5MG TAB,UD")
                      .build()))
          .text("ALMOTRIPTAN MALATE 12.5MG TAB,UD")
          .build();
    }

    gov.va.api.health.r4.api.datatypes.CodeableConcept formFromProduct() {
      return gov.va.api.health.r4.api.datatypes.CodeableConcept.builder()
          .text("TAB")
          .coding(
              List.of(
                  gov.va.api.health.r4.api.datatypes.Coding.builder()
                      .code("4015523")
                      .display("TAB")
                      .build()))
          .build();
    }

    List<gov.va.api.health.r4.api.datatypes.Identifier> identifierFromProduct() {
      return List.of(gov.va.api.health.r4.api.datatypes.Identifier.builder().id("4015523").build());
    }

    public gov.va.api.health.uscorer4.api.resources.Medication medication() {
      return medication("1000");
    }

    public gov.va.api.health.uscorer4.api.resources.Medication medication(String id) {
      return gov.va.api.health.uscorer4.api.resources.Medication.builder()
          .resourceType("Medication")
          .id(id)
          .code(codeRxNorm())
          .text(textRxNorm())
          .identifier(identifierFromProduct())
          .form(formFromProduct())
          .build();
    }

    gov.va.api.health.r4.api.elements.Narrative textLocalDrugName() {
      return gov.va.api.health.r4.api.elements.Narrative.builder()
          .status(gov.va.api.health.r4.api.elements.Narrative.NarrativeStatus.additional)
          .div("<div>Axert</div>")
          .build();
    }

    gov.va.api.health.r4.api.elements.Narrative textRxNorm() {
      return gov.va.api.health.r4.api.elements.Narrative.builder()
          .status(gov.va.api.health.r4.api.elements.Narrative.NarrativeStatus.additional)
          .div("<div>ALMOTRIPTAN MALATE 12.5MG TAB,UD</div>")
          .build();
    }
  }
}
