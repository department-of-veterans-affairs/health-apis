package gov.va.api.health.dataquery.service.controller.medicationdispense;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gov.va.api.health.dataquery.api.datatypes.CodeableConcept;
import gov.va.api.health.dataquery.api.datatypes.Coding;
import gov.va.api.health.dataquery.api.datatypes.Identifier;
import gov.va.api.health.dataquery.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.dataquery.api.datatypes.SimpleQuantity;
import gov.va.api.health.dataquery.api.datatypes.Timing;
import gov.va.api.health.dataquery.api.elements.Reference;
import gov.va.api.health.dataquery.api.resources.MedicationDispense;
import gov.va.api.health.dataquery.api.resources.MedicationDispense.DosageInstruction;
import gov.va.api.health.dataquery.api.resources.MedicationDispense.Status;
import gov.va.dvp.cdw.xsd.model.CdwCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwCoding;
import gov.va.dvp.cdw.xsd.model.CdwIdentifierUseCodes;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwAuthorizingPrescriptions;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwDosageInstructions;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwDosageInstructions.CdwDosageInstruction;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwDosageInstructions.CdwDosageInstruction.CdwRoute;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwDosageInstructions.CdwDosageInstruction.CdwTiming;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispense100Root.CdwMedicationDispenses.CdwMedicationDispense.CdwIdentifier;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispenseStatus;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispenseType;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispenseTypeCode;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispenseTypeCoding;
import gov.va.dvp.cdw.xsd.model.CdwMedicationDispenseTypeDisplay;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import gov.va.dvp.cdw.xsd.model.CdwSimpleQuantity;
import java.util.Collections;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Test;

public class MedicationDispenseTransformerTest {
  private final MedicationDispenseTransformer tx = new MedicationDispenseTransformer();

  private final CdwSampleData cdw = CdwSampleData.get();

  private final Expected expected = Expected.get();

  @Test
  public void authorizingPrescriptions() {
    assertThat(tx.authorizingPrescriptions(null)).isNull();
    assertThat(tx.authorizingPrescriptions(Collections.singletonList(null))).isNull();
  }

  @Test
  public void codeableConcept() {
    assertThat(tx.codeableConcept(null)).isNull();
    assertThat(tx.codeableConcept(new CdwCodeableConcept())).isNull();
  }

  @Test
  public void coding() {
    assertThat(tx.coding(null)).isNull();
    assertThat(tx.coding(new CdwCoding())).isNull();
  }

  @Test
  public void codings() {
    assertThat(tx.codings(null)).isNull();
    List<CdwCoding> nullList = Collections.singletonList(null);
    assertThat(tx.codings(nullList));
  }

  @Test
  public void dosageInstruction() {
    assertThat(tx.dosageInstruction(null)).isNull();
    CdwDosageInstruction testDose = new CdwDosageInstruction();
    testDose.setText("   ");
    assertThat(tx.dosageInstruction(testDose)).isNull();
  }

  @Test
  public void dosageInstructions() {
    assertThat(tx.dosageInstructions(null)).isNull();
    assertThat(tx.dosageInstructions(new CdwDosageInstructions())).isNull();
  }

  @Test
  public void identifier() {
    assertThat(tx.identifier(null)).isNull();
    assertThat(tx.identifier(singletonList(null))).isNull();
    assertThat(tx.identifier(singletonList(new CdwIdentifier()))).isNull();
  }

  @Test
  public void identifierUse() {
    assertThat(tx.identifierUse(null)).isNull();
    assertThat(tx.identifierUse(CdwIdentifierUseCodes.OFFICIAL)).isEqualTo(IdentifierUse.official);
    assertThat(tx.identifierUse(CdwIdentifierUseCodes.SECONDARY))
        .isEqualTo(IdentifierUse.secondary);
    assertThat(tx.identifierUse(CdwIdentifierUseCodes.TEMP)).isEqualTo(IdentifierUse.temp);
    assertThat(tx.identifierUse(CdwIdentifierUseCodes.USUAL)).isEqualTo(IdentifierUse.usual);
  }

  @Test
  public void medicationDispense() {
    assertThat(tx.apply(cdw.medicationDispense())).isEqualTo(expected.medicationDispense());
  }

  @Test
  public void quantityValue() {
    assertThat(tx.quantityValue(null)).isNull();
    assertThat(tx.quantityValue("   ")).isNull();
    assertThatThrownBy(
            () -> {
              tx.quantityValue("this is not a double");
            })
        .isInstanceOf(Exception.class)
        .hasMessageContaining("Cannot create double value from");
  }

  @Test
  public void reference() {
    assertThat(tx.reference(null)).isNull();
    assertThat(tx.reference(new CdwReference())).isNull();
    assertThat(
            tx.reference(
                cdw.reference(
                    "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290",
                    "VETERAN,JOHN Q")))
        .isEqualTo(
            expected.reference(
                "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290",
                "VETERAN,JOHN Q"));
  }

  @Test
  public void routeCodeableConcept() {
    assertThat(tx.routeCodeableConcept(null)).isNull();
    CdwRoute route = new CdwRoute();
    assertThat(tx.routeCodeableConcept(route)).isNull();
    route.setText("   ");
    assertThat(tx.routeCodeableConcept(route)).isNull();
  }

  @Test
  public void simpleQuantity() {
    CdwSimpleQuantity quantity = new CdwSimpleQuantity();
    assertThat(tx.simpleQuantity(quantity)).isNull();
  }

  @Test
  public void status() {
    assertThat(tx.status(null)).isEqualTo(null);
    assertThat(tx.status(CdwMedicationDispenseStatus.COMPLETED)).isEqualTo(Status.completed);
    assertThat(tx.status(CdwMedicationDispenseStatus.ENTERED_IN_ERROR))
        .isEqualTo(Status.entered_in_error);
    assertThat(tx.status(CdwMedicationDispenseStatus.IN_PROGRESS)).isEqualTo(Status.in_progress);
    assertThat(tx.status(CdwMedicationDispenseStatus.ON_HOLD)).isEqualTo(Status.on_hold);
    assertThat(tx.status(CdwMedicationDispenseStatus.STOPPED)).isEqualTo(Status.stopped);
  }

  @Test
  public void timing() {
    assertThat(tx.timing(null)).isNull();
    assertThat(tx.timing(new CdwTiming())).isNull();
  }

  @Test
  public void typeCodeableConcept() {
    assertThat(tx.typeCodeableConcept(null)).isNull();
    CdwMedicationDispenseType medDisp = new CdwMedicationDispenseType();
    medDisp.setText("   ");
    assertThat(tx.typeCodeableConcept(medDisp)).isNull();
  }

  @Test
  public void typeCoding() {
    assertThat(tx.typeCoding(null)).isNull();
    CdwMedicationDispenseTypeCoding codingTest = new CdwMedicationDispenseTypeCoding();
    assertThat(tx.typeCoding(codingTest)).isNull();
    /* Check that null values for code and display aren't going to throw NPEs */
    codingTest.setSystem("https://hellotest");
    assertThat(tx.typeCoding(codingTest))
        .isEqualTo(Coding.builder().system("https://hellotest").build());
  }

  @NoArgsConstructor(staticName = "get", access = AccessLevel.PUBLIC)
  static class CdwSampleData {
    private CdwAuthorizingPrescriptions authorizingPrescriptions() {
      CdwAuthorizingPrescriptions cdw = new CdwAuthorizingPrescriptions();
      cdw.getAuthorizingPrescription()
          .add(reference("MedicationOrder/1200738474346:O", "OUTPATIENT PHARMACY"));
      return cdw;
    }

    private CdwDosageInstruction cdwDosageInstruction() {
      CdwDosageInstruction cdw = new CdwDosageInstruction();
      cdw.setText("Take with water");
      cdw.setAdditionalInstructions(
          codeableConcept("Use bottled water", "BW", "https://things.com", "Bottled Water"));
      cdw.setAsNeededBoolean(true);
      cdw.setTiming(timing("Every 3 hours", "HR", "Hour"));
      cdw.setSiteCodeableConcept(
          codeableConcept("ORAL", "836005", "http://snomed.info/sct", "Oral region of face"));
      cdw.setDoseQuantity(simpleQuantity("1", null, null, null));
      cdw.setRoute(cdwRoute());
      return cdw;
    }

    private CdwDosageInstructions cdwDosageInstructions() {
      CdwDosageInstructions cdw = new CdwDosageInstructions();
      cdw.getDosageInstruction().add(cdwDosageInstruction());
      return cdw;
    }

    private CdwRoute cdwRoute() {
      CdwRoute cdw = new CdwRoute();
      cdw.setText("ORALLY");
      return cdw;
    }

    private CdwCodeableConcept codeableConcept(
        String text, String code, String system, String display) {
      CdwCodeableConcept codeableConcept = new CdwCodeableConcept();
      codeableConcept.setText(text);
      CdwCoding coding = new CdwCoding();
      coding.setCode(code);
      coding.setSystem(system);
      coding.setDisplay(display);
      codeableConcept.getCoding().add(coding);
      return codeableConcept;
    }

    @SneakyThrows
    private XMLGregorianCalendar dateTime(String timestamp) {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(timestamp);
    }

    private CdwSimpleQuantity daysSupply() {
      return simpleQuantity("30", "Day", "http://unitsofmeasure.org", "D");
    }

    private CdwReference dispenser() {
      return reference(
          "https://www.freedomstream.io/CDCArgonaut/api/Practitioner/5e27c469-82e4-5725-babb-49cf7eee948f",
          "BONES,ATTENDING C");
    }

    private CdwIdentifier identifier() {
      CdwIdentifier cdw = new CdwIdentifier();
      cdw.setUse(CdwIdentifierUseCodes.USUAL);
      cdw.setSystem("http://va.gov/cdw");
      cdw.setValue("185601V825290");
      return cdw;
    }

    private CdwReference medication() {
      return reference(
          "https://www.freedomstream.io/CDCArgonaut/api/Medication/2f773f73-ad7f-56ca-891e-8e364c913fe0",
          "ALBUTEROL 90MCG (CFC-F) 200D ORAL INHL");
    }

    CdwMedicationDispense medicationDispense() {
      CdwMedicationDispense cdw = new CdwMedicationDispense();
      cdw.setCdwId("1200738474343:R");
      cdw.setStatus(CdwMedicationDispenseStatus.COMPLETED);
      cdw.getIdentifier().add(identifier());
      cdw.getAuthorizingPrescriptions().add(authorizingPrescriptions());
      cdw.setPatient(patient());
      cdw.setDispenser(dispenser());
      cdw.setType(type());
      cdw.setQuantity(quantity());
      cdw.setDaysSupply(daysSupply());
      cdw.setMedicationReference(medication());
      cdw.setWhenPrepared(dateTime("2015-04-15T04:00:00Z"));
      cdw.setWhenHandedOver(dateTime("2015-04-22T16:00:00Z"));
      cdw.setNote("Take when breathing feels difficult.");
      cdw.setDosageInstructions(cdwDosageInstructions());
      return cdw;
    }

    private CdwReference patient() {
      return reference(
          "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290", "VETERAN,JOHN Q");
    }

    private CdwSimpleQuantity quantity() {
      return simpleQuantity("2", "EA", null, null);
    }

    private CdwReference reference(String ref, String display) {
      CdwReference cdw = new CdwReference();
      cdw.setReference(ref);
      cdw.setDisplay(display);
      return cdw;
    }

    private CdwSimpleQuantity simpleQuantity(
        String value, String unit, String system, String code) {
      CdwSimpleQuantity cdw = new CdwSimpleQuantity();
      cdw.setValue(value);
      cdw.setUnit(unit);
      cdw.setSystem(system);
      cdw.setCode(code);
      return cdw;
    }

    private CdwTiming timing(String text, String code, String display) {
      CdwTiming cdw = new CdwTiming();
      cdw.setCode(codeableConcept(text, code, "https://unitsofmeasure.com", display));
      return cdw;
    }

    private CdwMedicationDispenseType type() {
      CdwMedicationDispenseType type = new CdwMedicationDispenseType();
      CdwMedicationDispenseTypeCoding coding = new CdwMedicationDispenseTypeCoding();
      coding.setCode(CdwMedicationDispenseTypeCode.FF);
      coding.setDisplay(CdwMedicationDispenseTypeDisplay.FIRST_FILL);
      coding.setSystem("http://hl7.org/fhir/v3/ActCode");
      type.setCoding(coding);
      type.setText("First time filling.");
      return type;
    }
  }

  @NoArgsConstructor(staticName = "get")
  private static class Expected {
    private List<Reference> authorizingPrescriptions() {
      return Collections.singletonList(
          reference("MedicationOrder/1200738474346:O", "OUTPATIENT PHARMACY"));
    }

    private CodeableConcept codeableConcept(
        String text, String system, String code, String display) {
      if (system == null && code == null && display == null) {
        return CodeableConcept.builder().text(text).build();
      }
      return CodeableConcept.builder()
          .text(text)
          .coding(singletonList(coding(system, code, display)))
          .build();
    }

    private Coding coding(String system, String code, String display) {
      return Coding.builder().system(system).code(code).display(display).build();
    }

    private SimpleQuantity daysSupply() {
      return SimpleQuantity.builder()
          .value(Double.parseDouble("30"))
          .unit("Day")
          .system("http://unitsofmeasure.org")
          .code("D")
          .build();
    }

    private Reference dispenser() {
      return reference(
          "https://www.freedomstream.io/CDCArgonaut/api/Practitioner/5e27c469-82e4-5725-babb-49cf7eee948f",
          "BONES,ATTENDING C");
    }

    private DosageInstruction dosageInstruction() {
      return DosageInstruction.builder()
          .text("Take with water")
          .additionalInstructions(
              codeableConcept("Use bottled water", "https://things.com", "BW", "Bottled Water"))
          .asNeededBoolean(true)
          .timing(timing("Every 3 hours", "HR", "Hour"))
          .siteCodeableConcept(
              codeableConcept("ORAL", "http://snomed.info/sct", "836005", "Oral region of face"))
          .doseQuantity(SimpleQuantity.builder().value(Double.parseDouble("1")).build())
          .route(codeableConcept("ORALLY", null, null, null))
          .build();
    }

    private Identifier identifier() {
      return Identifier.builder()
          .use(Identifier.IdentifierUse.usual)
          .system("http://va.gov/cdw")
          .value("185601V825290")
          .build();
    }

    private Reference medication() {
      return reference(
          "https://www.freedomstream.io/CDCArgonaut/api/Medication/2f773f73-ad7f-56ca-891e-8e364c913fe0",
          "ALBUTEROL 90MCG (CFC-F) 200D ORAL INHL");
    }

    MedicationDispense medicationDispense() {
      return MedicationDispense.builder()
          .resourceType("MedicationDispense")
          .id("1200738474343:R")
          .identifier(identifier())
          .status(MedicationDispense.Status.completed)
          .authorizingPrescription(authorizingPrescriptions())
          .patient(patient())
          .dispenser(dispenser())
          .type(type())
          .quantity(quantity())
          .daysSupply(daysSupply())
          .medicationReference(medication())
          .whenPrepared("2015-04-15T04:00:00Z")
          .whenHandedOver("2015-04-22T16:00:00Z")
          .note("Take when breathing feels difficult.")
          .dosageInstruction(Collections.singletonList(dosageInstruction()))
          .build();
    }

    private Reference patient() {
      return reference(
          "https://www.freedomstream.io/CDCArgonaut/api/Patient/185601V825290", "VETERAN,JOHN Q");
    }

    private SimpleQuantity quantity() {
      return SimpleQuantity.builder().value(Double.parseDouble("2")).unit("EA").build();
    }

    private Reference reference(String ref, String display) {
      return Reference.builder().reference(ref).display(display).build();
    }

    private Timing timing(String text, String code, String display) {
      return Timing.builder()
          .code(codeableConcept(text, "https://unitsofmeasure.com", code, display))
          .build();
    }

    private CodeableConcept type() {
      return codeableConcept(
          "First time filling.", "http://hl7.org/fhir/v3/ActCode", "FF", "First Fill");
    }
  }
}
