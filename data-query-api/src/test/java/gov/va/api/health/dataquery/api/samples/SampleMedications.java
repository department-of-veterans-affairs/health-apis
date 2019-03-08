package gov.va.api.health.dataquery.api.samples;

import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.resources.Medication;
import java.util.Arrays;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(staticName = "get")
public class SampleMedications {
  @Delegate SampleDataTypes dataTypes = SampleDataTypes.get();

  public Medication.Batch batch() {
    return Medication.Batch.builder()
        .id("8888")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .lotNumber("10")
        .expirationDate("2000-01-01T00:00:00-00:00")
        .build();
  }

  public CodeableConcept code() {
    return CodeableConcept.builder().coding(singletonList(coding())).text("HelloText").build();
  }

  public CodeableConcept container() {
    return CodeableConcept.builder().coding(singletonList(coding())).text("HelloText").build();
  }

  public Medication.Content content() {
    return Medication.Content.builder()
        .id("8888")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .item(reference())
        .amount(simpleQuantity())
        .build();
  }

  public CodeableConcept form() {
    return CodeableConcept.builder().coding(singletonList(coding())).text("HelloText").build();
  }

  public Medication.Ingredient ingredient() {
    return Medication.Ingredient.builder()
        .id("8888")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .item(reference())
        .amount(ratio())
        .build();
  }

  public Medication medication() {
    return Medication.builder()
        .id("1234")
        .resourceType("Medication")
        .meta(meta())
        .implicitRules("http://HelloRules.com")
        .language("Hello Language")
        .text(narrative())
        .contained(singletonList(resource()))
        .extension(Arrays.asList(extension(), extension()))
        .modifierExtension(
            Arrays.asList(extension(), extensionWithQuantity(), extensionWithRatio()))
        .code(code())
        .isBrand(true)
        .manufacturer(reference())
        .product(product())
        .medicationPackage(medicationPackage())
        .build();
  }

  public Medication.MedicationPackage medicationPackage() {
    return Medication.MedicationPackage.builder()
        .id("8888")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .container(container())
        .content(singletonList(content()))
        .build();
  }

  public Medication.Product product() {
    return Medication.Product.builder()
        .id("8888")
        .extension(singletonList(extension()))
        .modifierExtension(singletonList(extension()))
        .form(form())
        .ingredient(ingredient())
        .batch(singletonList(batch()))
        .build();
  }
}
