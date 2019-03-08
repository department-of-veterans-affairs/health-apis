package gov.va.api.health.dataquery.service.controller.medication;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.elements.Narrative;
import gov.va.api.health.dataquery.api.elements.Narrative.NarrativeStatus;
import gov.va.api.health.dataquery.api.resources.Medication;
import gov.va.api.health.dataquery.api.resources.Medication.Product;
import gov.va.dvp.cdw.xsd.model.CdwCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root.CdwMedications.CdwMedication;
import gov.va.dvp.cdw.xsd.model.CdwMedication101Root.CdwMedications.CdwMedication.CdwProduct;
import java.math.BigInteger;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Test;

public class MedicationTransformerTest {
  private final CdwSampleData cdw = CdwSampleData.get();
  private final Expected expected = Expected.get();
  private final MedicationTransformer tx = new MedicationTransformer();

  @Test
  public void code() {
    assertThat(tx.code(cdw.code())).isEqualTo(expected.code());
    assertThat(tx.code(null)).isNull();
    assertThat(tx.code(new CdwCodeableConcept())).isNull();
  }

  @Test
  public void codeCoding() {
    assertThat(tx.codeCodings(singletonList(cdw.codeCoding()))).isEqualTo(expected.codeCoding());

    assertThat(tx.codeCodings(null)).isNull();
    assertThat(tx.codeCodings(emptyList())).isNull();
    assertThat(tx.codeCodings(singletonList(new CdwCoding()))).isNull();

    CdwCoding allBlank = new CdwCoding();
    allBlank.setSystem(" ");
    allBlank.setCode(" ");
    allBlank.setDisplay(" ");
    assertThat(tx.codeCodings(singletonList(allBlank))).isNull();

    CdwCoding systemOnly = new CdwCoding();
    systemOnly.setSystem("s");
    systemOnly.setCode(" ");
    systemOnly.setDisplay(" ");
    assertThat(tx.codeCodings(singletonList(systemOnly)))
        .isEqualTo(singletonList(Coding.builder().system("s").build()));

    CdwCoding codeOnly = new CdwCoding();
    codeOnly.setSystem(" ");
    codeOnly.setCode("c");
    codeOnly.setDisplay(" ");
    assertThat(tx.codeCodings(singletonList(codeOnly)))
        .isEqualTo(singletonList(Coding.builder().code("c").build()));

    CdwCoding displayOnly = new CdwCoding();
    displayOnly.setSystem(" ");
    displayOnly.setCode(" ");
    displayOnly.setDisplay("d");
    assertThat(tx.codeCodings(singletonList(displayOnly)))
        .isEqualTo(singletonList(Coding.builder().display("d").build()));
  }

  @Test
  public void form() {
    assertThat(tx.form(cdw.form())).isEqualTo(expected.form());
    assertThat(tx.form(null)).isNull();
    assertThat(tx.form(new CdwCodeableConcept())).isNull();
  }

  @Test
  public void formCoding() {
    assertThat(tx.formCodings(singletonList(cdw.formCoding()))).isEqualTo(expected.formCoding());
    assertThat(tx.formCodings(null)).isNull();
    assertThat(tx.formCodings(emptyList())).isNull();
    assertThat(tx.formCodings(singletonList(new CdwCoding()))).isNull();
  }

  @Test
  public void medication() {
    assertThat(tx.apply(cdw.medication())).isEqualTo(expected.medication());
  }

  @Test
  public void product() {
    assertThat(tx.product(cdw.product())).isEqualTo(expected.product());
    assertThat(tx.product(null)).isNull();
    assertThat(tx.product(new CdwProduct())).isNull();
  }

  @Test
  public void text() {
    assertThat(tx.text("hello")).isEqualTo(expected.text());
    assertThat(tx.text(null)).isNull();
    assertThat(tx.text("")).isNull();
  }

  @NoArgsConstructor(staticName = "get")
  private static class Expected {
    CodeableConcept code() {
      return CodeableConcept.builder()
          .text("ATORVASTATIN CA 80MG TAB")
          .coding(codeCoding())
          .build();
    }

    List<Coding> codeCoding() {
      return singletonList(
          Coding.builder()
              .system("https://www.nlm.nih.gov/research/umls/rxnorm/")
              .code("259255")
              .display("ATORVASTATIN CA 80MG TAB")
              .build());
    }

    CodeableConcept form() {
      return CodeableConcept.builder().text("TAB").coding(formCoding()).build();
    }

    List<Coding> formCoding() {
      return singletonList(
          Coding.builder()
              .system("https://www.nlm.nih.gov/research/umls/rxnorm/")
              .code("259255")
              .display("TAB")
              .build());
    }

    Medication medication() {
      return Medication.builder()
          .resourceType("Medication")
          .id("123456789")
          .text(text())
          .code(code())
          .product(product())
          .build();
    }

    Product product() {
      return Product.builder().id("4014891").form(form()).build();
    }

    Narrative text() {
      return Narrative.builder().div("<div>hello</div>").status(NarrativeStatus.additional).build();
    }
  }

  @NoArgsConstructor(staticName = "get", access = AccessLevel.PUBLIC)
  private static class CdwSampleData {
    CdwCodeableConcept code() {
      CdwCodeableConcept code = new CdwCodeableConcept();
      code.setText("ATORVASTATIN CA 80MG TAB");
      code.getCoding().add(codeCoding());
      return code;
    }

    CdwCoding codeCoding() {
      CdwCoding coding = new CdwCoding();
      coding.setSystem("https://www.nlm.nih.gov/research/umls/rxnorm/");
      coding.setCode("259255");
      coding.setDisplay("ATORVASTATIN CA 80MG TAB");
      return coding;
    }

    CdwCodeableConcept form() {
      CdwCodeableConcept form = new CdwCodeableConcept();
      form.setText("TAB");
      form.getCoding().add(formCoding());
      return form;
    }

    CdwCoding formCoding() {
      CdwCoding formCoding = new CdwCoding();
      formCoding.setSystem("https://www.nlm.nih.gov/research/umls/rxnorm/");
      formCoding.setCode("259255");
      formCoding.setDisplay("TAB");
      return formCoding;
    }

    CdwMedication medication() {
      CdwMedication medication = new CdwMedication();
      medication.setCdwId("123456789");
      medication.setRowNumber(new BigInteger("1"));
      medication.setText("hello");
      medication.setCode(code());
      medication.setProduct(product());
      return medication;
    }

    CdwProduct product() {
      CdwProduct product = new CdwProduct();
      product.setId("4014891");
      product.setForm(form());
      return product;
    }
  }
}
